package urldsl.language

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import urldsl.errors.DummyError
import urldsl.language.PathSegment.dummyErrorImpl._

final class PathSegmentRawUrlSpec extends AnyFlatSpec with Matchers {

  "Path segments" should "correctly generate urls" in {

    root.createPath() should be("")

    val hello = "hi"
    val number = 3
    val withSpace = "hello John"

    (root / segment[String]).createPath(hello) should be(hello)
    (root / segment[Int] / segment[String]).createPath((number, hello)) should be(s"$number/$hello")

    (root / segment[String] / "bold" / segment[Int] / endOfSegments).createPath((hello, number)) should be(
      s"$hello/bold/$number"
    )

    val ls = List("car", "motorcycle")

    (root / segment[String] / "bold" / remainingSegments).createPath((hello, ls)) should be(
      s"$hello/bold/" + ls.mkString("/")
    )
//
//    (root / segment[String] / "energy").path(withSpace) should be(
//      withSpace.map {
//        case ' ' => '+'
//        case c   => c
//      } + "/energy"
//    )

  }

}
