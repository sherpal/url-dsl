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

object UrlPart {

  private[language] def factory[T, E](
      matcher: (String, UrlStringParserGenerator) => Either[E, T],
      generator: (T, UrlStringGenerator) => String
  ) = new UrlPart[T, E] {
    def matchRawUrl(url: String, urlStringParserGenerator: UrlStringParserGenerator): Either[E, T] =
      matcher(url, urlStringParserGenerator)

    def createPart(t: T, encoder: UrlStringGenerator): String = generator(t, encoder)
  }

  /**
    * Type alias when you don't care about what kind of error is issued.
    * [[Any]] can seem weird, but it has to be understood as "since it can fail with anything, I won't be able to do
    * anything with the error, which means that I can only check whether it failed or not".
    */
  type SimpleUrlPart[T] = UrlPart[T, Any]

}
