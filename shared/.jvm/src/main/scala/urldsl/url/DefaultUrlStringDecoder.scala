package urldsl.url

trait DefaultUrlStringDecoder {

  protected final val defaultDecoder0: UrlStringDecoder = (str: String, encoding: String) =>
    java.net.URLDecoder.decode(str, encoding)

}
