package urldsl.url

final class JavaNetUrlGenerator extends UrlStringGenerator {
  def encode(str: String, encoding: String): String = java.net.URLEncoder.encode(str, encoding)
}

object JavaNetUrlGenerator {

  implicit lazy val javaNetUrlGenerator: JavaNetUrlGenerator = new JavaNetUrlGenerator

}
