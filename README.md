# URL DSL

This is a tiny library for parsing and generating paths and parameters of urls.

## Getting started

We represent the path and query parameters of a url as follows:
```scala
import urldsl.language.PathSegment.simplePathErrorImpl._
import urldsl.language.QueryParameters.simpleParamErrorImpl._
import urldsl.vocabulary.Segment
import urldsl.vocabulary.Param
import urldsl.language.PathSegmentWithQueryParams.UrlMatching

val path = root / "hello" / segment[Int] / segment[String] / endOfSegments
val params = param[Int]("age") & listParam[String]("drinks")

val pathWithParams = path ? params

pathWithParams.matchUrl(
  List(Segment("hello"), Segment("2019"), Segment("january")),
  Map("age" -> Param(List("10")), "drinks" -> Param(List("Orange juice", "Water")))
) == Right(UrlMatching((2019, "january"), (10, List("Orange juice", "Water"))))

path.matchSegments(
  List(Segment("hello"), Segment("2019"), Segment("january"), Segment("16"))
) == Left(urldsl.errors.SimplePathMatchingError.EndOfSegmentRequired(List(Segment("16"))))
```

For more example usages, head over the tests.

*TODO*
