package urldsl.url

trait DefaultUrlStringGenerator {

  val default: UrlStringGenerator = (str: String, encoding: String) => java.net.URLEncoder.encode(str, encoding)

}
