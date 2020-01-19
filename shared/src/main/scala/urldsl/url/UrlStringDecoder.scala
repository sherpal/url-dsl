package urldsl.url

import urldsl.vocabulary.{Param, Segment}

trait UrlStringDecoder {
  def decode(str: String, encoding: String = "utf-8"): String

  def decodePath(path: String): List[Segment] = Segment.fromPath(path).map(_.map(decode(_)))
  def decodeParams(queryString: String): Map[String, Param] =
    Param.fromQueryString(queryString)
    .map { case (key, value) => key -> value.transform(decode(_)) }
}

object UrlStringDecoder {

  val defaultDecoder: UrlStringDecoder = (str: String, encoding: String) => java.net.URLDecoder.decode(str, encoding)

}
