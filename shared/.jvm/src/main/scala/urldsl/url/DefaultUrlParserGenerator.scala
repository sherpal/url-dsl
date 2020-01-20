package urldsl.url

trait DefaultUrlParserGenerator {

  final val defaultUrlStringParserGenerator: UrlStringParserGenerator =
    JavaNetUrlStringParser.javaNetUrlStringParserGenerator

}
