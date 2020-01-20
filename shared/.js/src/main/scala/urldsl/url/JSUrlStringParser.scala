package urldsl.url

import org.scalajs.dom.experimental.URL

import scala.scalajs.js

final class JSUrlStringParser(val rawUrl: String) extends UrlStringParser {

  private val urlParser = new URL(rawUrl)

  def queryParametersString: String = urlParser.search

  def path: String = urlParser.pathname

  def decode(str: String, encoding: String): String = js.Dynamic.global.applyDynamic("encodeURI")(str).toString
}

object JSUrlStringParser {

  final lazy val jsUrlStringParserGenerator: UrlStringParserGenerator =
    (rawUrl: String) => new JSUrlStringParser(rawUrl)

}
