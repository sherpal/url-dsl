package urldsl.language

import org.scalacheck._
import org.scalacheck.Prop._
import urldsl.errors.{ErrorFromThrowable, PathMatchingError}
import urldsl.url.UrlStringGenerator
import urldsl.vocabulary._

import scala.util.Try

abstract class PathSegmentProperties[E](val impl: PathSegmentImpl[E], val error: PathMatchingError[E], name: String)(
    implicit errorFromThrowable: ErrorFromThrowable[E]
) extends Properties(name) {

  import Prop.forAll

  import impl._

  val $ : PathSegment[Unit, E] = root

  val segmentGen: Gen[Segment] = Gen.asciiStr.filter(_.nonEmpty).map(Segment(_))
  val nonIntSegmentGen: Gen[Segment] =
    segmentGen.map(_.content).map(s => Try(s.toInt).failed.map(_ => s).getOrElse("")).map(Segment(_))

  property("RootConsumesNothing") = forAll(Gen.listOf[String](Gen.asciiStr)) { (ls: List[String]) =>
    $.matchSegments(ls.map(Segment(_))) == Right(PathMatchOutput((), ls.map(Segment(_))))
  }

  property("IntWithString") = forAll { (x: Int, s: String) =>
    ($ / segment[Int] / segment[String]).matchSegments(List(Segment(x.toString), Segment(s))).map(_.output) == Right(
      (x, s)
    )
  }

  property("generic unary path segment can be called") = forAll { (x: Int, s: String) =>
    implicit def - : PathMatchingError[E] = error
    val path = PathSegment.unaryPathSegment[Int, E](x) / PathSegment.unaryPathSegment[String, E](s)
    Prop(path.matchSegments(List(
      Segment(x.toString), Segment(s)
    )).map(_.output) == Right(())) && (Prop(s.nonEmpty) ==> Prop(
      path.createPath() == s"$x/${UrlStringGenerator.default.encode(s)}"
    )) :|
      s"""Expected: $x/${UrlStringGenerator.default.encode(s)}
         |Obtained: ${path.createPath()}
         |""".stripMargin
  }

  property("remainingSegmentsAndEndOfSegments") = forAll(Gen.listOf[String](Gen.asciiStr)) { (ls: List[String]) =>
    ($ / remainingSegments / endOfSegments).matchSegments(ls.map(Segment(_))) == Right(PathMatchOutput(ls, Nil))
  }

  property("ExactMatchingString") = forAll(Gen.listOfN[String](5, Gen.asciiStr)) { (ls: List[String]) =>
    ($ / ls.head / ls.tail.head).matchSegments(ls.map(Segment(_))) == Right(
      PathMatchOutput((), ls.tail.tail.map(Segment(_)))
    )
  }

  val lsIntBoolGen: Gen[(List[Segment], Int, Boolean)] = for {
    ls <- Gen.listOf(segmentGen)
    x <- Gen.chooseNum(-5, 10)
    b <- Arbitrary.arbBool.arbitrary
  } yield (ls, x, b)

  property("ExactMatchingBooleanAndInts") = forAll(lsIntBoolGen) { case (ls: List[Segment], x: Int, bool: Boolean) =>
    ($ / bool / x).matchSegments(List(bool.toString, x.toString).map(Segment(_)) ++ ls) == Right(
      PathMatchOutput((), ls)
    )
  }

  property("IntSegmentMatcher") = forAll(Gen.listOf(Arbitrary.arbInt.arbitrary)) { (ls: List[Int]) =>
    val segments = ls.map(_.toString).map(Segment(_))
    ls match {
      case Nil => true
      case head :: Nil =>
        ($ / segment[Int] / endOfSegments).matchSegments(segments) == Right(
          PathMatchOutput(
            head,
            Nil
          )
        )
      case head :: second :: Nil =>
        ($ / segment[Int] / segment[Int] / endOfSegments).matchSegments(segments) == Right(
          PathMatchOutput(
            (head, second),
            Nil
          )
        )
      case head :: second :: rest =>
        ($ / segment[Int] / segment[Int]).matchSegments(segments) == Right(
          PathMatchOutput(
            (head, second),
            rest.map(_.toString).map(Segment(_))
          )
        )
    }
  }

  property("EndOfSegmentComplains") = forAll(Gen.nonEmptyListOf(segmentGen)) { (ls: List[Segment]) =>
    ($ / endOfSegments).matchSegments(ls) == Left(error.endOfSegmentRequired(ls))
  }

  property("IntSegmentComplains") = forAllNoShrink(Gen.nonEmptyListOf(nonIntSegmentGen)) { (ls: List[Segment]) =>
    val matchResult = ($ / segment[Int]).matchSegments(ls)
    val expectedResult = Left(error.malformed(s"""For input string: "${ls.head.content}""""))
    Prop(matchResult == expectedResult) :| s"Match result was $matchResult but I needed $expectedResult"
  }

  property("oneOfValuesMatches") = forAllNoShrink(Gen.nonEmptyListOf(segmentGen)) { (ls: List[Segment]) =>
    val path = $ / oneOf[String](ls.head.content, ls.tail.map(_.content): _*)

    ls.forall(s => path.matchSegments(List(s)).isRight) && (Prop(path.createPath() == UrlStringGenerator.default.encode(ls.head.content)) :|
      s"""Obtained: ${path.createPath()}
         |Expected: ${UrlStringGenerator.default.encode(ls.head.content)}
         |""".stripMargin)
  }

  property("oneOfValuesNotMatch") = forAll(for {
    strings <- Gen.nonEmptyListOf(Gen.asciiStr)
    str <- Gen.asciiStr
    if str.nonEmpty
  } yield (strings, str)) { case (choices, str) => // there will most likely be no match but it's still possible
    val path = $ / oneOf[String](choices.head, choices.tail: _*)

    path.matchSegments(List(Segment(str))).isRight == choices.contains(str)
  }

  property("noMatch always fails") = forAllNoShrink(Gen.listOf(segmentGen)) { segments =>
    noMatch.matchSegments(segments) == Left(error.unit)
  }

  property("Unit sugar method to create segments can be called") = forAll { (x: Int) =>
    ($ / x).createSegments() == List(Segment(x.toString))
  }

  property("dummy encoder can be used to override") = forAll { (x: Int) =>
    ($ / x).createPath((_: String, _: String) => "dummy") == "dummy"
  }

  property("filtering has no impact on generation") = forAll(Gen.choose(0, 1000)) { (x: Int) =>
    segment[Int].filter(_ < 0, _ => error.malformed(s"$x should be negative")).createPath(x) == x.toString
  }

  property("Or operator can target both types") = forAll(Gen.choose(-100, 100), Gen.alphaNumStr) { (x: Int, s: String) =>
    val path = segment[Int] || segment[String]

    Prop(Try(s.toInt).isFailure) ==> {
      // noinspection ComparingUnrelatedTypes
      Prop(path.matchSegments(List(x.toString)) == Right(PathMatchOutput(Left(x), Nil))) && Prop(
        path.matchSegments(List(s)) == Right(PathMatchOutput(Right(s), Nil))
      ) && Prop(path.createPath(Left(x)) == x.toString) && Prop(path.createPath(Right(s)) == s)
    }
  }

  property("Bijection works with container") = forAll { (x: Int) =>
    case class Container(value: Int)
    implicit def codec: Codec[Int, Container] = Codec.factory(Container.apply, _.value)

    val path = segment[Int].as[Container]

    Prop(
      path.matchSegments(List(Segment(x.toString), Segment("hello"))) == Right(
        PathMatchOutput(Container(x), List(Segment("hello")))
      )
    ) && Prop(
      path.createPath(Container(73)) == "73"
    )
  }

  property("ignore methods generates default") = forAll { (x: Int) =>
    segment[Int].ignore(x).createPath() == x.toString
  }

  property("provide method decodes and encodes x") = forAll { (x: Int) =>
    val path = segment[Int].provide(x)(error, Printer[Int])
    Prop(path.matchSegments(List(Segment(x.toString))) == Right(PathMatchOutput((), Nil))) && Prop(
      path.createPath() == x.toString
    )
  }

  property("provide method fails when decoding wrong value") = forAll { (x: Int, y: Int) =>
    val path = segment[Int].provide(x)(error, Printer[Int])
    Prop(x == y) || Prop(
      path.matchSegments(List(Segment(y.toString))) == Left(error.wrongValue(x.toString, y.toString))
    )
  }

  property("string and int segment are aliases for generic method") = forAll { (x: Int, s: String) =>
    val intPath1 = PathSegment.intSegment(error, ErrorFromThrowable[E])
    val intPath2 = segment[Int]

    val stringPath1 = PathSegment.stringSegment(error)
    val stringPath2 = segment[String]
    Prop(intPath1.createPath(x) == intPath2.createPath(x)) && Prop(
      stringPath1.createPath(s) == stringPath2.createPath(s)
    )
  }

}
