package urldsl.url

import urldsl.vocabulary.{Param, Segment}

trait UrlStringParser extends UrlStringDecoder {

  val rawUrl: String

  /** Returns the raw content of the query string. */
  def queryParametersString: String

  /** Returns the raw content of the path. */
  def path: String

  /** Returns the raw content of the fragment (sometimes called ref), or None if there is no fragment */
  def maybeFragment: Option[String]

  /** Alias for [[maybeFragment]].  */
  final def maybeRef: Option[String] = maybeFragment

  final def segments: List[Segment] = decodePath(path)
  final def params: Map[String, Param] = decodeParams(queryParametersString)

}
