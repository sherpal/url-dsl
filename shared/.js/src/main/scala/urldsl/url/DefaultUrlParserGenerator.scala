package urldsl.url

trait DefaultUrlParserGenerator {

  final val defaultUrlStringParserGenerator: UrlStringParserGenerator =
    JSUrlStringParser.jsUrlStringParserGenerator

}
