package urldsl.url

trait UrlStringParserGenerator[A <: UrlStringParser] {

  def parser(rawUrl: String): A

}
