package urldsl.url

trait UrlStringParserGenerator {

  def parser(rawUrl: String): UrlStringParser

}

object UrlStringParserGenerator extends DefaultUrlParserGenerator {

  // The unnecessary trick below is used to make IntelliJ happier in the rest of the codebase.
  // It ain't pretty, but does the trick.

  /** Returns a default implementation of the [[UrlStringParserGenerator]] */
  val defaultUrlStringParserGenerator: UrlStringParserGenerator = defaultUrlStringParserGenerator0

}
