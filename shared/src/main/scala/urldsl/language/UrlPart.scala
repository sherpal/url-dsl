package urldsl.language

import urldsl.url.{UrlStringGenerator, UrlStringParserGenerator}

/**
  * A [[UrlPart]] represents a part of (or the entire) URL and is able to extract some information out of it.
  * When it succeeds to extract information, it returns an element of type T (wrapped in a [[Right]]). When it fails
  * to extract such information, it returns an error type E (wrapped in a [[Left]].
  *
  * A [[UrlPart]] is also able to generate its corresponding part of the URL by ingesting an element of type T. When
  * doing that, it outputs a String (whose semantic may vary depending on the type of [[UrlPart]] you are dealing with).
  */
trait UrlPart[T, +E] {

  def matchRawUrl(
      url: String,
      urlStringParserGenerator: UrlStringParserGenerator = UrlStringParserGenerator.defaultUrlStringParserGenerator
  ): Either[E, T]

  /** Takes an instance of T and generate the part of the URL contained in this T */
  def createPart(t: T, encoder: UrlStringGenerator = UrlStringGenerator.default): String

  /** Sugar when T =:= Unit */
  final def createPart(encoder: UrlStringGenerator)(implicit ev: Unit =:= T): String =
    createPart(ev(()), encoder)

  /** Sugar when T =:= Unit */
  final def createPart()(implicit ev: Unit =:= T): String = createPart(ev(()))

}
