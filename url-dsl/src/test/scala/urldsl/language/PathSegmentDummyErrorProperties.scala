package urldsl.language


import org.scalacheck.Gen
import org.scalacheck.Prop.forAll
import urldsl.errors.DummyError.dummyErrorIsPathMatchingError
final class PathSegmentDummyErrorProperties extends PathSegmentProperties(dummyErrorImpl, dummyErrorIsPathMatchingError, "PathSegmentDummyError") {

  import dummyErrorImpl._

  property("filtering has no impact on generation with sugar") = forAll(Gen.choose(0, 1000)) { (x: Int) =>
    segment[Int].filter(_ < 0).createPath(x) == x.toString
  }


}
