package urldsl.url

trait DefaultUrlStringGenerator {

  protected val default0: UrlStringGenerator = (str: String, encoding: String) =>
    java.net.URLEncoder
      .encode(str, encoding)
      .replaceAll("\\+", "%20")
      .replaceAll("%21", "!")
      .replaceAll("%27", "'")
      .replaceAll("%28", "(")
      .replaceAll("%29", ")")
      .replaceAll("%7E", "~")

}
