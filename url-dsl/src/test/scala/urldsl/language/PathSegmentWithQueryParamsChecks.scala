package urldsl.language

import org.scalacheck._
import org.scalacheck.Prop._
import urldsl.errors.{ErrorFromThrowable, PathMatchingError}
import urldsl.url.{UrlStringGenerator, UrlStringParserGenerator}
import urldsl.vocabulary.{Param, Segment, UrlMatching}

abstract class PathSegmentWithQueryParamsChecks[P, Q, F](
    val impl: AllImpl[P, Q, F],
    name: String
)(implicit
    pErrorFromThrowable: ErrorFromThrowable[P],
    qErrorFromThrowable: ErrorFromThrowable[Q],
    fErrorFromThrowable: ErrorFromThrowable[F],
    pPathMatchingError: PathMatchingError[P]
) extends Properties(name) {

  import impl._

  val intSegment = segment[Int]
  val doubleQuery = param[Double]("the-double")
  val booleanFragment = fragment[Boolean]

  val segmentWithParam = intSegment ? doubleQuery

  val host = "http://localhost:9000/"

  property("Segment is failing implies that segmentWithParam is failing like the segment") =
    forAllNoShrink(Gen.alphaNumStr.filter(_.nonEmpty), Gen.chooseNum(-10.0, 10.0)) { (str: String, d: Double) =>
      Prop(intSegment.matchSegments(List(Segment(str))).isLeft) ==> {
        val path = UrlStringGenerator.default.encode(str)
        val query = "the-double=" ++ d.toString

        val left = segmentWithParam.matchUrl(List(Segment(str)), doubleQuery.createParams(d)).left.map(_.toString)
        val right = Left(intSegment.matchSegments(List(Segment(str)))).left.map(_.toString)
        (Prop(left == right) :|
          s"""Left: $left
             |Right: $right
             |""".stripMargin) && Prop(
          segmentWithParam
            .matchRawUrlOption(
              host ++ path ++ "?" ++ query
            )
            .isEmpty
        ) && Prop(segmentWithParam.matchPathAndQueryOption(path, query).isEmpty)
      }
    }

  property("Segment succeeds and query fails implies that segmentWithParam fails like the query") =
    forAllNoShrink(Gen.chooseNum(-100, 100), Gen.alphaNumStr.filter(_.nonEmpty)) { (x: Int, str: String) =>
      val params = Map("the-double" -> Param(List(str)))
      val path = UrlStringGenerator.default.encode(x.toString)
      val query = "the-double=" ++ UrlStringGenerator.default.encode(str)

      Prop(doubleQuery.matchParams(params).isLeft) ==> {
        val left = segmentWithParam.matchUrl(List(Segment(x.toString)), params).left.map(_.map(_.toString))
        val right = doubleQuery.matchParams(params).left.map(err => Right(err.toString))
        (Prop(left == right) :|
          s"""Left: $left
             |Right: $right
             |""".stripMargin) && Prop(segmentWithParam.matchPathAndQueryOption(path, query).isEmpty)
      }
    }

  property("segmentWithParam matches the combination of intSegment and doubleQuery") =
    forAllNoShrink(Gen.chooseNum(-100, 100), Gen.chooseNum(-10.0, 10.0)) { (i: Int, d: Double) =>
      val path = UrlStringGenerator.default.encode(i.toString)
      val query = "the-double=" ++ d.toString
      val url = host ++ path ++ "?" ++ query
      Prop(intSegment.matchRawUrl(url) == segmentWithParam.matchPathAndQuery(path, query).map(_.path)) && Prop(
        doubleQuery.matchRawUrl(url) == segmentWithParam.matchPathAndQuery(path, query).map(_.params)
      )
    }

  property("segmentWithParam creates path of intSegment and query of doubleQuery") =
    forAllNoShrink(Gen.chooseNum(-100, 100), Gen.chooseNum(-10.0, 10.0)) { (i: Int, d: Double) =>
      val obtained = UrlStringParserGenerator.defaultUrlStringParserGenerator.parser(
        host ++ segmentWithParam.createUrlString(i, d)
      )
      val expectedPath = "/" ++ intSegment.createPath(i)
      val expectedQuery = doubleQuery.createParamsString(d)
      (Prop(obtained.path == expectedPath) :|
        s"""Obtained: ${obtained.path}
           |Expected: $expectedPath
           |""".stripMargin) && Prop(obtained.queryParametersString == expectedQuery) :|
        s"""Obtained: ${obtained.queryParametersString}
           |Expected: $expectedQuery
           |""".stripMargin
    }

  property("Create url creates the pair from intSegment and doubleQuery") =
    forAllNoShrink(Gen.chooseNum(-100, 100), Gen.chooseNum(-10.0, 10.0)) { (i: Int, d: Double) =>
      Prop(segmentWithParam.createUrl(i, d) == (intSegment.createSegments(i), doubleQuery.createParams(d))) && Prop(
        (intSegment.provide(i) ? doubleQuery).createUrl(d) == segmentWithParam.createUrl(i, d)
      )
    }

  property("Create part is the combination of intSegment and doubleQuery") =
    forAllNoShrink(Gen.chooseNum(-100, 100), Gen.chooseNum(-10.0, 10.0)) { (i: Int, d: Double) =>
      val path = UrlStringGenerator.default.encode(i.toString)
      val query = "the-double=" ++ d.toString
      segmentWithParam.createPart(UrlMatching(i, d)) == path ++ "?" ++ query
    }

}
