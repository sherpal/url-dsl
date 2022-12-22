package urldsl.url

import urldsl.vocabulary.MaybeFragment

//noinspection TypeAnnotation
final class UrlStringGeneratorSpec extends munit.FunSuite {

  val generator = UrlStringGenerator.default

  test("Encoding empty fragment results in empty string") {
    assertEquals(generator.makeFragment(MaybeFragment(None)), "")
  }

}
