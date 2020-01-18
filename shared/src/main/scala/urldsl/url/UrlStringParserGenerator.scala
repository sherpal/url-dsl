package urldsl.url

trait UrlStringParserGenerator {

  def parser(rawUrl: String): UrlStringParser

}

object UrlStringParserGenerator {

  final val defaultUrlStringParserGenerator: UrlStringParserGenerator =
    JavaNetUrlStringParser.javaNetUrlStringParserGenerator

}
