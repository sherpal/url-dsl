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

    pathWithParams.matchRawUrl(
      "http://localhost:8080/hello/2019/january?age=10&drinks=orange%20juice&drinks=water"
    ) should be(
      Right(UrlMatching((2019, "january"), (10, List("orange juice", "water"))))
    )

    path.matchSegments(
      List(Segment("hello"), Segment("2019"), Segment("january"), Segment("16"))
    ) should be(Left(urldsl.errors.SimplePathMatchingError.EndOfSegmentRequired(List(Segment("16")))))

    path.matchPath("/hello/2019/january") should be(
      Right((2019, "january"))
    )

    params.matchQueryString("age=22&drinks=orange%20juice&drinks=water") should be(
      Right((22, List("orange juice", "water")))
    )
    // commutativity is there for query params with different names
    params.matchQueryString("drinks=orange%20juice&drinks=water&age=22") should be(
      Right((22, List("orange juice", "water")))
    )
    // extra parameters are ignored
    params.matchQueryString("drinks=orange%20juice&drinks=water&age=22&unused=(something,else)") should be(
      Right((22, List("orange juice", "water")))
    )

  }

}
