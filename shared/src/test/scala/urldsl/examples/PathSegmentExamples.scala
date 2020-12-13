package urldsl.examples

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import urldsl.errors.SimplePathMatchingError
import urldsl.vocabulary.{Codec, Segment}

/**
  * This class exposes some example of usage of the [[urldsl.language.PathSegment]] class.
  *
  * The `sampleUrl` used throughout this example class is defined in the [[urldsl.examples]] package object.
  */
final class PathSegmentExamples extends AnyFlatSpec with Matchers {

  /** The following import brings everything you need for [[urldsl.language.PathSegment]] usage. */
  import urldsl.language.PathSegment.simplePathErrorImpl._

  "Some matching examples" should "work" in {

    /** `root` is a "dummy" matcher which matches anything. It returns [[Unit]] when matching. */
    root.matchRawUrl(sampleUrl) should be(Right(()))

    /**
      * Appending a specific segment value to the root in order to match the first segment.
      * It returns Unit when matching, since we assume that the information is already known (it's "foo").
      */
    (root / "foo").matchRawUrl(sampleUrl) should be(Right(()))

    /**
      * Appending a [[String]] segmennt in order to retrieve the information contained in the first segment.
      */
    (root / segment[String]).matchRawUrl(sampleUrl) should be(Right("foo"))

    /** You can also directly match other types than [[String]] (if you have the right implicits in scope). */
    (root / "foo" / segment[Int]).matchRawUrl(sampleUrl) should be(Right(23))
    (root / "foo" / 23 / segment[Boolean]).matchRawUrl(sampleUrl) should be(Right(true))

    /** Note that a segment of [[String]] will always manage to match, but of course you get a ... [[String]]. */
    (root / "foo" / segment[String]).matchRawUrl(sampleUrl) should be(Right("23"))

    /** You can of course match several things at once, in which case outputs are "tupled". */
    (root / "foo" / segment[Int] / segment[Boolean]).matchRawUrl(sampleUrl) should be(Right((23, true)))
    (root / segment[String] / segment[Int] / segment[Boolean]).matchRawUrl(sampleUrl) should be(
      Right(("foo", 23, true))
    )

    /**
      * You can compose things the way you like, "pre-computing" segments.
      *
      * The `/` operator is *associative*, which means that the "place where you put parenthesis" doesn't matter. In
      * the same way that (3+4)+5 = 3+(4+5), you have that `(s1 / s2) / s3 = s1 / (s2 / s3)`. Note that they won't be
      * equal as Scala objects, but they will be for any (relevant) observable behaviour. (Technical note: it's not
      * entirely true if you go crazy in tupling things.)
      */
    val s1 = segment[String]
    val s2 = segment[Int]
    val s3 = segment[Boolean]
    (s1 / (s2 / s3)).matchRawUrl(sampleUrl) should be(((s1 / s2) / s3).matchRawUrl(sampleUrl))

    /** You can also "group" several segments into a more meaningful class than a pair or a triplet. */
    case class Stuff(str: String, j: Int)

    (s1 / s2).as((Stuff.apply _).tupled, Stuff.unapply(_: Stuff).get).matchRawUrl(sampleUrl) should be(
      Right(Stuff("foo", 23))
    )

    /** Or conveniently with an implicit [[urldsl.vocabulary.Codec]] */
    implicit val stuffCodec: Codec[(String, Int), Stuff] =
      Codec.factory((Stuff.apply _).tupled, Stuff.unapply(_: Stuff).get)

    (s1 / s2).as[Stuff].matchRawUrl(sampleUrl) should be(Right(Stuff("foo", 23)))

    /** [[urldsl.language.PathSegment]] can also be filtered to add some more restriction on the matching. */
    s1.filter(_.length > 2, _ => SimplePathMatchingError.SimpleError("too short")).matchRawUrl(sampleUrl) should be(
      Right("foo")
    )
    s1.filter(_.length > 3, _ => SimplePathMatchingError.SimpleError("too short")).matchRawUrl(sampleUrl) should be(
      Left(
        SimplePathMatchingError.SimpleError("too short")
      )
    )

    /**
      * As you probably noticed, [[urldsl.language.PathSegment]] are immutable objects and hence, can be reused freely.
      */
    (s1 / s1).matchRawUrl(sampleUrl) should be(Right("foo", "23"))

    /**
      * You also have the ability to compose [[urldsl.language.PathSegment]] "horizontally", by "branching" several
      * possibilities
      */
    (root / (segment[Int] || segment[String])).matchRawUrl(sampleUrl) should be(Right(Right("foo")))

    /**
      * The [[urldsl.language.PathSegment]] trait is also very general, and while part of its power comes from its
      * great "composability", it also comes from its ability to make quite generic things.
      * One example is imposing that the path has ended.
      */
    (root / "foo" / 23 / true / endOfSegments).matchRawUrl(sampleUrl) should be(Right(()))
    (root / "foo" / endOfSegments).matchRawUrl(sampleUrl) should be(
      Left(
        SimplePathMatchingError.EndOfSegmentRequired(
          // this is the list of segments that were still present when reaching the `endOfSegments`
          List(
            Segment("23"),
            Segment("true")
          )
        )
      )
    )

  }

  "Some matching examples" should "fail" in {

    /** When trying to match a type that can not be created from the segment [[String]], it fails. */
    (root / segment[Int]).matchRawUrl(sampleUrl) should be(
      Left(
        SimplePathMatchingError.SimpleError("""For input string: "foo"""")
      )
    )

    /** It also fails if you want to consume more segments than there is. */
    (root / "foo" / 23 / true / "other").matchRawUrl(sampleUrl) should be(
      Left(SimplePathMatchingError.MissingSegment)
    )

    /** Asking for the wrong segment will fail as well. */
    (root / "not-good").matchRawUrl(sampleUrl) should be(
      Left(
        SimplePathMatchingError.WrongValue("not-good", "foo")
      )
    )

  }

  "Some generating examples" should "work" in {

    /**
      * You can also use [[urldsl.language.PathSegment]]s for generating paths.
      * Note that the strength of urldsl is that the `createPart` method is typesafe and knows what the path expects
      * as information in order to generate the path string.
      */
    (root / segment[String] / segment[Int] / "hey").createPart(("hello", 22)) should be("hello/22/hey")

  }

}
