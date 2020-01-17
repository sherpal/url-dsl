package urldsl.url

import urldsl.vocabulary.{Param, Segment}

trait UrlStringParser {

  val rawUrl: String

  def queryParametersString: String
  def path: String

  def decode(str: String, encoding: String = "utf-8"): String

  final def segments: List[Segment] = Segment.fromPath(path).map(_.map(decode(_)))
  final def params: Map[String, Param] =
    Param.fromQueryString(queryParametersString).view.mapValues(_.transform(decode(_))).toMap

}
