package urldsl.url

import urldsl.vocabulary.{Param, Segment}

trait UrlStringParser extends UrlStringDecoder {

  val rawUrl: String

  def queryParametersString: String
  def path: String

  final def segments: List[Segment] = decodePath(path)
  final def params: Map[String, Param] = decodeParams(queryParametersString)

}
