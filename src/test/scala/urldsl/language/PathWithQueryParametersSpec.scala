package urldsl.language

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PathWithQueryParametersSpec extends AnyFlatSpec with Matchers {

  import urldsl.language.PathSegment.simplePathErrorImpl._
  import urldsl.language.QueryParameters.simpleParamErrorImpl._
  import urldsl.vocabulary.Segment
  import urldsl.vocabulary.Param
  import urldsl.vocabulary.UrlMatching

  "Readme example" should "work" in {
    val path = root / "hello" / segment[Int] / segment[String] / endOfSegments
    val params = param[Int]("age") & listParam[String]("drinks")

    val pathWithParams = path ? params

    pathWithParams.matchUrl(
      List(Segment("hello"), Segment("2019"), Segment("january")),
      Map("age" -> Param(List("10")), "drinks" -> Param(List("Orange juice", "Water")))
    ) should be(Right(UrlMatching((2019, "january"), (10, List("Orange juice", "Water")))))

    path.matchSegments(
      List(Segment("hello"), Segment("2019"), Segment("january"), Segment("16"))
    ) should be(Left(urldsl.errors.SimplePathMatchingError.EndOfSegmentRequired(List(Segment("16")))))
  }

}
