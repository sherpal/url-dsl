package urldsl.language

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import urldsl.examples.{intTupleFromDashedString, intTupleDashedPrinter}

class PathWithQueryParametersSpec extends munit.FunSuite {

  import urldsl.language.PathSegment.simplePathErrorImpl._
  import urldsl.language.QueryParameters.simpleParamErrorImpl._
  import urldsl.vocabulary.Segment
  import urldsl.vocabulary.Param
  import urldsl.vocabulary.UrlMatching

  private val sampleUrl =
    "http://localhost:8080/hello/2019/january?age=10&tuple=44-55&drinks=orange%20juice&drinks=water"

  test("Readme example should work") {
    val path = root / "hello" / segment[Int] / segment[String]
    val params = param[Int]("age") & listParam[String]("drinks")

    val pathWithParams = path ? params

    assertEquals(
      pathWithParams.matchUrl(
        List(Segment("hello"), Segment("2019"), Segment("january")),
        Map("age" -> Param(List("10")), "drinks" -> Param(List("Orange juice", "Water")))
      ),
      Right(UrlMatching((2019, "january"), (10, List("Orange juice", "Water"))))
    )
    assertEquals(
      pathWithParams.matchRawUrl(
        sampleUrl
      ),
      Right(UrlMatching((2019, "january"), (10, List("orange juice", "water"))))
    )
    assertEquals(
      (pathWithParams & param[(Int, Int)]("tuple")).matchRawUrl(
        sampleUrl
      ),
      Right(UrlMatching((2019, "january"), (10, List("orange juice", "water"), 44, 55)))
    )
    assertEquals(
      path.matchFullSegments(
        List(Segment("hello"), Segment("2019"), Segment("january"), Segment("16"))
      ),
      Left(urldsl.errors.SimplePathMatchingError.EndOfSegmentRequired(List(Segment("16"))))
    )

    assertEquals(path.matchPath("/hello/2019/january"), Right((2019, "january")))

    assertEquals(
      params.matchQueryString("age=22&drinks=orange%20juice&drinks=water"),
      Right((22, List("orange juice", "water")))
    )
    // commutativity is there for query params with different names
    assertEquals(
      params.matchQueryString("drinks=orange%20juice&drinks=water&age=22"),
      Right((22, List("orange juice", "water")))
    )
    // extra parameters are ignored
    assertEquals(
      params.matchQueryString("drinks=orange%20juice&drinks=water&age=22&unused=(something,else)"),
      Right((22, List("orange juice", "water")))
    )

  }

}
