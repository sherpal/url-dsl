package urldsl.url

import java.net.{URL, URLDecoder}

final class JavaNetUrlStringParser(val rawUrl: String) extends UrlStringParser {

  private val urlParser = new URL(rawUrl)

  def queryParametersString: String = urlParser.getQuery

  def path: String = urlParser.getPath

  /* getRef method of URL returns null if # is not present. */
  def maybeFragment: Option[String] = Option(urlParser.getRef).filter(_.nonEmpty)

  def decode(str: String, encoding: String): String = URLDecoder.decode(str, encoding)
}

object JavaNetUrlStringParser {

  final lazy val javaNetUrlStringParserGenerator: UrlStringParserGenerator =
    (rawUrl: String) => new JavaNetUrlStringParser(rawUrl)

}
