package urldsl

/**
  * This package shows examples of usage of url-dsl.
  *
  * When using url-dsl, you are free to chose any error adt system that you want. There are two
  * built-in for you, the [[urldsl.errors.SimplePathMatchingError]] (and siblings) and the
  * [[urldsl.errors.DummyError]]. We will use the former in these example. The latter being perfect
  * in cases where you don't care about what caused the error (for example a router) or when you use primarily
  * url-dsl for generating urls, and not for parsing them.
  */
package object examples {
  val sampleUrl = "http://www.some-domain.be/foo/23/true?bar=stuff&babar=other%20stuff&other=2&other=3&ok=true#the-ref"
  val sampleUrlWithoutFragment =
    "http://www.some-domain.be/foo/23/true?bar=stuff&babar=other%20stuff&other=2&other=3&ok=true"
}
