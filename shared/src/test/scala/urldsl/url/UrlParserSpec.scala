package urldsl.url

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

//noinspection TypeAnnotation
final class UrlParserSpec extends AnyFlatSpec with Matchers {

  val parserGenerator = UrlStringParserGenerator.defaultUrlStringParserGenerator

  val sampleUrl = "https://scala-lang.org/hello?q=3"
  val fragment = "hey"
  val sampleUrlWithFragment = sampleUrl ++ "#" ++ fragment

  val hashtagEndingUrl = sampleUrl ++ "#"

  "A url without fragment" should "issue None when parsing" in {
    parserGenerator.parser(sampleUrl).maybeFragment should be(None)
  }

  "A url with fragment" should "issue some when parsing" in {
    parserGenerator.parser(sampleUrlWithFragment).maybeFragment should be(Some(fragment))
  }

  "A url ending with #" should "issue None when parsing" in {
    parserGenerator.parser(hashtagEndingUrl).maybeFragment should be(None)
  }

}
