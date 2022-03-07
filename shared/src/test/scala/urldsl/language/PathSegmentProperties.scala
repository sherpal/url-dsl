package urldsl.language

import org.scalacheck._
import urldsl.errors.DummyError
import urldsl.errors.DummyError.{dummyErrorIsPathMatchingError => e}
import urldsl.vocabulary.{PathMatchOutput, Segment}

import scala.util.Try

final class PathSegmentProperties extends Properties("PathSegment") {
  import Prop.forAll

  import PathSegment.dummyErrorImpl._

  val $ : PathSegment[Unit, DummyError] = root

  val segmentGen: Gen[Segment] = Gen.asciiStr.map(Segment(_))
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

  property("ExactMatchingBooleanAndInts") = forAll(lsIntBoolGen) {
    case (ls: List[Segment], x: Int, bool: Boolean) =>
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
            head.toInt,
            Nil
          )
        )
      case head :: second :: Nil =>
        ($ / segment[Int] / segment[Int] / endOfSegments).matchSegments(segments) == Right(
          PathMatchOutput(
            (head.toInt, second.toInt),
            Nil
          )
        )
      case head :: second :: rest =>
        ($ / segment[Int] / segment[Int]).matchSegments(segments) == Right(
          PathMatchOutput(
            (head.toInt, second.toInt),
            rest.map(_.toString).map(Segment(_))
          )
        )
    }
  }

  property("EndOfSegmentComplains") = forAll(Gen.nonEmptyListOf(segmentGen)) { (ls: List[Segment]) =>
    ($ / endOfSegments).matchSegments(ls) == Left(e.endOfSegmentRequired(ls))
  }

  property("IntSegmentComplains") = forAll(Gen.nonEmptyListOf(nonIntSegmentGen)) { (ls: List[Segment]) =>
    val matchResult = ($ / segment[Int]).matchSegments(ls)
    val expectedResult = Left(e.malformed(ls.head.content))
    Prop(matchResult == expectedResult) :| s"Match result was $matchResult but I needed $expectedResult"
  }

  property("oneOfValuesMatches") = forAll(Gen.nonEmptyListOf(segmentGen)) { (ls: List[Segment]) =>
    val path = $ / oneOf[String](ls.head.content, ls.tail.map(_.content): _*)

    ls.forall(s => path.matchSegments(List(s)).isRight)
  }

  property("oneOfValuesNotMatch") = forAll(for {
    strings <- Gen.nonEmptyListOf(Gen.asciiStr)
    str <- Gen.asciiStr
    if str.nonEmpty
  } yield (strings, str)) {
    case (choices, str) => // there will most likely be no match but it's still possible
      val path = $ / oneOf[String](choices.head, choices.tail: _*)

      path.matchSegments(List(Segment(str))).isRight == choices.contains(str)
  }

  property("uuids can roundtrip") = forAll(Arbitrary.arbUuid.arbitrary) { (uuid: java.util.UUID) =>
    val path = $ / segment[java.util.UUID] / "hello"

    path.matchRawUrl("http://some-domain.com/" ++ path.createPath(uuid)) == Right(uuid)
  }

}
