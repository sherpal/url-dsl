package urldsl.parsers

import java.net.URL

final class JavaNetUrlStringParser(val rawUrl: String) extends UrlStringParser {

  private val urlParser = new URL(rawUrl)

  def queryParameters: String = urlParser.getQuery

  def path: String = urlParser.getPath
}

object JavaNetUrlStringParser {

  implicit lazy val javaNetUrlStringParserGenerator: UrlStringParserGenerator[JavaNetUrlStringParser] =
    (rawUrl: String) => new JavaNetUrlStringParser(rawUrl)

}
