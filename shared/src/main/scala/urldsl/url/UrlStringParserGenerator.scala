package urldsl.url

trait UrlStringParserGenerator {

  def parser(rawUrl: String): UrlStringParser

}

object UrlStringParserGenerator extends DefaultUrlParserGenerator
