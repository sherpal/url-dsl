package urldsl.language

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import urldsl.errors.DummyError
import urldsl.parsers.JavaNetUrlStringParser
import urldsl.vocabulary.{PathMatchOutput, Segment}

class PathSegmentSpec extends AnyFlatSpec with Matchers {

  val psi: PathSegmentImpl[DummyError] = PathSegmentImpl[DummyError]
  import psi._

  val $ : PathSegment[Unit, DummyError] = root

  "PathSegment" should "match the following segment lists" in {

    ($ / segment[Int] / segment[String]).matchSegments(List(Segment("17"), Segment("Hello"))).map(_.output) should be(
      Right((17, "Hello"))
    )

    ($ / "hello" / true).matchSegments(List("hello", "true", "something")) should be(
      Right(PathMatchOutput((), List(Segment("something"))))
    )

  }

  "remainingSegments" should "consumes all segments" in {

    val list = List("one", "two", "three")

    ($ / remainingSegments).matchSegments(list.map(Segment(_))) should be(
      Right(PathMatchOutput(list, Nil))
    )

    ($ / segment[String] / remainingSegments).matchSegments(list.map(Segment(_))) should be(
      Right(PathMatchOutput(("one", list.tail), Nil))
    )

    ($ / list.head / list.tail.head / list.tail.tail.head).matchSegments(list.map(Segment(_))) should be(
      Right(PathMatchOutput((), Nil))
    )

  }

  "Matching a simple raw string" should "work" in {

    val url = "http://localhost:8080/hello/32/true"
    val path = $ / segment[String] / segment[Int] / true

    path.matchRawUrl[JavaNetUrlStringParser](url) should be(
      Right(PathMatchOutput(("hello", 32), Nil))
    )

  }

}
