package urldsl.url

trait DefaultUrlStringDecoder {

  val defaultDecoder: UrlStringDecoder = (str: String, encoding: String) => java.net.URLDecoder.decode(str, encoding)

}
