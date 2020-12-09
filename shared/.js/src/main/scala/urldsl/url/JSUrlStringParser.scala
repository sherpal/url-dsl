package urldsl.url

import org.scalajs.dom.experimental.URL

import scala.scalajs.js

final class JSUrlStringParser(val rawUrl: String) extends UrlStringParser {

  private val urlParser = new URL(rawUrl)

  def queryParametersString: String = urlParser.search

  def path: String = urlParser.pathname

  def maybeFragment: Option[String] =
    Option(urlParser.hash)
    /*
       * Empty fragment are considered to have no fragment at all
       */
      .filter(_.nonEmpty)
      .map(_.drop(1)) // remove the # symbol

  def decode(str: String, encoding: String): String = js.Dynamic.global.applyDynamic("decodeURIComponent")(str).toString

}

object JSUrlStringParser {

  final lazy val jsUrlStringParserGenerator: UrlStringParserGenerator =
    (rawUrl: String) => new JSUrlStringParser(rawUrl)

}
