package urldsl.url

//noinspection TypeAnnotation
final class UrlParserSpec extends munit.FunSuite {

  val parserGenerator = UrlStringParserGenerator.defaultUrlStringParserGenerator

  val sampleUrl = "https://scala-lang.org/hello?q=3"
  val fragment = "hey"
  val sampleUrlWithFragment = sampleUrl ++ "#" ++ fragment

  val hashtagEndingUrl = sampleUrl ++ "#"

  test("A url without fragment should issue None when parsing") {
    assertEquals(parserGenerator.parser(sampleUrl).maybeFragment, None)
  }

  test("A url with fragment should issue some when parsing") {
    assertEquals(parserGenerator.parser(sampleUrlWithFragment).maybeFragment, Some(fragment))
  }

  test("A url ending with # should issue None when parsing") {
    assertEquals(parserGenerator.parser(hashtagEndingUrl).maybeFragment, None)
  }

}
