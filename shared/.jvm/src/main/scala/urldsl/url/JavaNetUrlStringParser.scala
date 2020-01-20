package urldsl.url

import java.net.{URL, URLDecoder}

final class JavaNetUrlStringParser(val rawUrl: String) extends UrlStringParser {

  private val urlParser = new URL(rawUrl)

  def queryParametersString: String = urlParser.getQuery

  def path: String = urlParser.getPath

  def decode(str: String, encoding: String): String = URLDecoder.decode(str, encoding)
}

object JavaNetUrlStringParser {

  final lazy val javaNetUrlStringParserGenerator: UrlStringParserGenerator =
    (rawUrl: String) => new JavaNetUrlStringParser(rawUrl)

}
