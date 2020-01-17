# URL DSL

This is a tiny library for parsing and generating paths and parameters of urls.

## Getting started

We represent the path and query parameters of a url as follows:
```scala
import urldsl.language.PathSegment.simplePathErrorImpl._
import urldsl.language.QueryParameters.simpleParamErrorImpl._
import urldsl.vocabulary.{Segment, Param, UrlMatching}

val path = root / "hello" / segment[Int] / segment[String] / endOfSegments
val params = param[Int]("age") & listParam[String]("drinks")

val pathWithParams = path ? params

pathWithParams.matchRawUrl[JavaNetUrlStringParser](
  "http://localhost:8080/hello/2019/january?age=10&drinks=orange+juice&drinks=water"
) should be(
  Right(UrlMatching((2019, "january"), (10, List("orange juice", "water"))))
)

path.matchPath("/hello/2019/january") should be(
  Right((2019, "january"))
)

params.matchQueryString("age=22&drinks=orange+juice&drinks=water") should be(
  Right((22, List("orange juice", "water")))
)
```

For more example usages, head over the tests.

*TODO*
