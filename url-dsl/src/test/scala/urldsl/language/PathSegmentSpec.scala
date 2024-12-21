package urldsl.language

import urldsl.errors.DummyError
import urldsl.vocabulary.{PathMatchOutput, Segment}

class PathSegmentSpec extends munit.FunSuite {

  val psi: PathSegmentImpl[DummyError] = PathSegmentImpl[DummyError]
  import psi._

  val $ : PathSegment[Unit, DummyError] = root

  test("Empty path with end of segments should match the empty path") {
    assertEquals($.matchPath("/"), Right(()))
    assertEquals($.matchPath(""), Right(()))
  }

  test("PathSegment should match the following segment lists") {

    assertEquals(
      ($ / segment[Int] / segment[String]).matchSegments(List(Segment("17"), Segment("Hello"))).map(_.output),
      Right((17, "Hello"))
    )

    assertEquals(
      ($ / "hello" / true).matchSegments(List("hello", "true", "something")),
      Right(PathMatchOutput((), List(Segment("something"))))
    )

  }

  test("remainingSegments should consumes all segments") {

    val list = List("one", "two", "three")

    assertEquals(($ / remainingSegments).matchSegments(list.map(Segment(_))), Right(PathMatchOutput(list, Nil)))

    assertEquals(
      ($ / segment[String] / remainingSegments).matchSegments(list.map(Segment(_))),
      Right(PathMatchOutput(("one", list.tail), Nil))
    )

    assertEquals(
      ($ / list.head / list.tail.head / list.tail.tail.head).matchSegments(list.map(Segment(_))),
      Right(PathMatchOutput((), Nil))
    )

  }

  test("Matching a simple raw string should work") {

    val url = "http://localhost:8080/hello/32/true"
    val path = $ / segment[String] / segment[Int] / true

    assertEquals(path.matchRawUrl(url), Right(("hello", 32)))
  }

  test("noMatch segment generates empty string") {
    assertEquals(noMatch.createPath(), "")
  }

}
