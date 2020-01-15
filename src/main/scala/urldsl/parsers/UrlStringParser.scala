package urldsl.parsers

import urldsl.vocabulary.Segment

trait UrlStringParser {

  val rawUrl: String

  def queryParameters: String
  def path: String

  def segments: List[Segment] = Segment.fromPath(path)

}
