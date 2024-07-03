package urldsl.url

import java.net.{URI, URLDecoder}

final class JavaNetUrlStringParser(val rawUrl: String) extends UrlStringParser {

  private val urlParser = URI.create(rawUrl).toURL()

  def queryParametersString: String = Option(urlParser.getQuery).getOrElse("")

  def path: String = urlParser.getPath

  /* getRef method of URL returns null if # is not present. */
  def maybeFragment: Option[String] = Option(urlParser.getRef).filter(_.nonEmpty)

  def decode(str: String, encoding: String): String = URLDecoder.decode(str, encoding)
}

object JavaNetUrlStringParser {

  final lazy val javaNetUrlStringParserGenerator: UrlStringParserGenerator = new UrlStringParserGenerator {
    def parser(rawUrl: String): UrlStringParser = new JavaNetUrlStringParser(rawUrl)
  }

}
