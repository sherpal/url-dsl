package urldsl.parsers

trait UrlStringParserGenerator[A <: UrlStringParser] {

  def parser(rawUrl: String): A

}
