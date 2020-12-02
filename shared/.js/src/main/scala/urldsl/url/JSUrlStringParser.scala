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
       * If the ref is empty and the url does not end with #, then there was no fragment at all.
       * We do this to match exactly the JVM's behavior which, in my opinion, seems better since it gives
       * more information about the url.
       */
      .filterNot(ref => ref.isEmpty && !rawUrl.endsWith("#"))

  def decode(str: String, encoding: String): String = js.Dynamic.global.applyDynamic("decodeURIComponent")(str).toString

}

object JSUrlStringParser {

  final lazy val jsUrlStringParserGenerator: UrlStringParserGenerator =
    (rawUrl: String) => new JSUrlStringParser(rawUrl)

}
