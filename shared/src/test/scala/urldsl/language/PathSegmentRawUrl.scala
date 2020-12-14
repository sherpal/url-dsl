package urldsl.language

import org.scalacheck.{Gen, Prop, Properties}
import urldsl.errors.DummyError
import urldsl.language.PathSegment.dummyErrorImpl._
import urldsl.url.UrlStringGenerator
import urldsl.vocabulary.Segment

final class PathSegmentRawUrl extends Properties("PathSegmentRawUrl") {
  import Prop.forAll

  val $ : PathSegment[Unit, DummyError] = root

  val segmentGen: Gen[Segment] = Gen.asciiStr.map(Segment(_))

  def pathSegmentFromList(segments: List[Segment]): PathSegment[Unit, DummyError] =
    segments.foldLeft(root)(_ / _.content)

  property("StaticGeneration") = forAll(Gen.nonEmptyListOf(segmentGen)) { (segments: List[Segment]) =>
    val path = pathSegmentFromList(segments)

    path.createPath() == UrlStringGenerator.default.makePath(segments)
  }

}
