package urldsl.language

import urldsl.errors.{DummyError, ErrorFromThrowable, PathMatchingError, SimplePathMatchingError}
import urldsl.url.{UrlStringDecoder, UrlStringGenerator, UrlStringParserGenerator}
import urldsl.vocabulary._

import scala.language.implicitConversions

/**
  * Represents a part of the path string of an URL, containing an information of type T, or an error of type A.
  * @tparam T type represented by this PathSegment
  * @tparam A type of the error that this PathSegment produces on "illegal" url paths.
  */
trait PathSegment[T, +A] extends UrlPart[T, A] {

  /**
    * Tries to match the list of [[urldsl.vocabulary.Segment]]s to create an instance of `T`.
    * If it can not, it returns an error indicating the reason of the failure.
    * If it could, it returns the value of `T`, as well as the list of unused segments.
    *
    * @example
    *          For example, a segment that matches simply a String in the first segment, when giving segments like
    *          List(Segment("hello"), Segment("3"))
    *          will return
    *          Right(PathMatchOutput("hello", List(Segment("3")))
    *
    * @param segments             The list of [[urldsl.vocabulary.Segment]] to match this path segment again.
    * @return The "de-serialized" element with unused segment, if successful.
    */
  def matchSegments(segments: List[Segment]): Either[A, PathMatchOutput[T]]

  /**
    * Matches the given raw `url` using the given [[urldsl.url.UrlStringParserGenerator]] for creating a
    * [[urldsl.url.UrlStringParser]].
    *
    * This method doesn't return the information about the remaining unused segments. The thought leading to this is
    * that [[urldsl.vocabulary.PathMatchOutput]] are supposed to be internal mechanics, while this method is supposed to
    * be the exposed interface of this [[urldsl.language.PathSegment]].
    *
    * @param url                      the url to parse. It has to be a well formed URL, otherwise this could raise an
    *                                  exception, depending on the provided [[urldsl.url.UrlStringParserGenerator]].
    * @param urlStringParserGenerator the [[urldsl.url.UrlStringParserGenerator]] used to create the
    *                                  [[urldsl.url.UrlStringParser]] that will actually parse the url to create the
    *                                  segments. The default one is usually a good choice. It has different
    *                                  implementations in JVM and JS, but they *should* behave the same way.
    * @return                         the output contained in the url, or the error if something fails.
    */
  def matchRawUrl(
      url: String,
      urlStringParserGenerator: UrlStringParserGenerator = UrlStringParserGenerator.defaultUrlStringParserGenerator
  ): Either[A, T] =
    matchSegments(urlStringParserGenerator.parser(url).segments).map(_.output)

  def matchPath(path: String, decoder: UrlStringDecoder = UrlStringDecoder.defaultDecoder): Either[A, T] =
    matchSegments(decoder.decodePath(path)).map(_.output)

  /**
    * Generate a list of segments representing the argument `t`.
    *
    * `matchSegments` and `createSegments` should be (functional) inverse of each other. That is,
    * `this.matchSegments(this.createSegments(t)) == Right(PathMathOutput(t, Nil))`
    */
  def createSegments(t: T): List[Segment]

  /**
    * Sugar when `T =:= Unit`
    */
  final def createSegments()(implicit ev: Unit =:= T): List[Segment] = createSegments(ev(()))

  /**
    * Concatenates the segments generated by `createSegments`
    */
  def createPath(t: T, encoder: UrlStringGenerator = UrlStringGenerator.default): String =
    encoder.makePath(createSegments(t))

  /**
    * Sugar when `T =:= Unit`
    */
  final def createPath()(implicit ev: Unit =:= T): String =
    createPath(())
  final def createPath(encoder: UrlStringGenerator)(implicit ev: Unit =:= T): String =
    createPath((), encoder)

  final def createPart(t: T, encoder: UrlStringGenerator): String = createPath(t, encoder)

  /**
    * Concatenates `this` [[urldsl.language.PathSegment]] with `that` one, "tupling" the types with the [[Tupler]]
    * rules.
    */
  final def /[U, A1 >: A](that: PathSegment[U, A1])(implicit ev: Tupler[T, U]): PathSegment[ev.Out, A1] =
    PathSegment.factory[ev.Out, A1](
      (segments: List[Segment]) =>
        for {
          firstOut <- this.matchSegments(segments)
          PathMatchOutput(t, remaining) = firstOut
          secondOut <- that.matchSegments(remaining)
          PathMatchOutput(u, lastRemaining) = secondOut
        } yield PathMatchOutput(ev(t, u), lastRemaining),
      (out: ev.Out) => {
        val (t, u) = ev.unapply(out)

        this.createSegments(t) ++ that.createSegments(u)
      }
    )

  final def ?[ParamsType, QPError](
      params: QueryParameters[ParamsType, QPError]
  ): PathSegmentWithQueryParams[T, A, ParamsType, QPError] =
    new PathSegmentWithQueryParams(this, params)

  /**
    * Adds an extra satisfying criteria to the de-serialized output of this [[urldsl.language.PathSegment]].
    *
    * The new de-serialization works as follows:
    * - if the initial de-serialization fails, then it returns the generated error
    * - otherwise, if the de-serialized element satisfies the predicate, then it returns the element
    * - if the predicate is false, generates the given `error` by feeding it the segments that it tried to match.
    *
    * This can be useful in, among others, two scenarios:
    * - enforce bigger restriction on a segment (e.g., from integers to positive integer, regex match...)
    * - in a multi-part segment, ensure consistency between the different component (e.g., a range of two integers that
    *   should not be too large...)
    */
  final def filter[A1 >: A](predicate: T => Boolean, error: List[Segment] => A1): PathSegment[T, A1] =
    PathSegment.factory[T, A1](
      (segments: List[Segment]) =>
        matchSegments(segments)
          .filterOrElse(((_: PathMatchOutput[T]).output).andThen(predicate), error(segments)),
      createSegments
    )

  /** Sugar for when `A =:= DummyError` */
  final def filter(predicate: T => Boolean)(implicit ev: A <:< DummyError): PathSegment[T, DummyError] = {
    type F[+E] = PathSegment[T, E]
    ev.liftCo[F].apply(this).filter(predicate, _ => DummyError.dummyError)
  }

  /**
    * Builds a [[PathSegment]] that first tries to match with this one, then tries to match with `that` one.
    * If both fail, the error of the second is returned (todo[behaviour]: should that change?)
    */
  final def ||[U, A1 >: A](that: PathSegment[U, A1]): PathSegment[Either[T, U], A1] =
    PathSegment.factory[Either[T, U], A1](
      segments =>
        this.matchSegments(segments) match {
          case Right(output) => Right(PathMatchOutput(Left(output.output), output.unusedSegments))
          case Left(_) =>
            that.matchSegments(segments).map(output => PathMatchOutput(Right(output.output), output.unusedSegments))
        },
      _.fold(this.createSegments, that.createSegments)
    )

  /**
    * Casts this [[PathSegment]] to the new type U. Note that the [[urldsl.vocabulary.Codec]] must be an exception-free
    * bijection between T and U (or at least an embedding, if you know that you are doing).
    */
  final def as[U](implicit codec: Codec[T, U]): PathSegment[U, A] = as[U](codec.leftToRight _, codec.rightToLeft _)

  /**
    * Casts this [[PathSegment]] to the new type U. The conversion functions should form an exception-free bijection
    * between T and U (or at least an embedding, if you know that you are doing).
    */
  final def as[U](fromTToU: T => U, fromUToT: U => T): PathSegment[U, A] = PathSegment.factory[U, A](
    (matchSegments _).andThen(_.map(_.map(fromTToU))),
    fromUToT.andThen(createSegments)
  )

  /**
    * Matches using this [[PathSegment]], and then forgets its content.
    * Uses the `default` value when creating the path to go back.
    */
  final def ignore(default: => T): PathSegment[Unit, A] = PathSegment.factory[Unit, A](
    matchSegments(_).map(_.map(_ => ())),
    (_: Unit) => createSegments(default)
  )

  /**
    * Forgets the information contained in the path parameter by injecting one.
    * This turn this "dynamic" [[PathSegment]] into a fix one.
    */
  final def provide[A1 >: A](
      t: T
  )(implicit pathMatchingError: PathMatchingError[A1], printer: Printer[T]): PathSegment[Unit, A1] =
    PathSegment.factory[Unit, A1](
      segments =>
        for {
          tMatch <- matchSegments(segments)
          PathMatchOutput(tOutput, unusedSegments) = tMatch
          unitMatched <- if (tOutput != t) Left(pathMatchingError.wrongValue(printer(t), printer(tOutput)))
          else Right(PathMatchOutput((), unusedSegments))
        } yield unitMatched,
      (_: Unit) => createSegments(t)
    )

  /**
    * Associates this [[PathSegment]] with the given [[Fragment]] in order to match raw urls satisfying both
    * conditions, and returning the outputs from both.
    *
    * The query part of the url will be *ignored* (and will return Unit).
    */
  final def withFragment[FragmentType, FragmentError](
      fragment: Fragment[FragmentType, FragmentError]
  ): PathQueryFragmentRepr[T, A, Unit, Nothing, FragmentType, FragmentError] =
    new PathQueryFragmentRepr(this, QueryParameters.ignore, fragment)

}

object PathSegment {

  type PathSegmentSimpleError[T] = PathSegment[T, SimplePathMatchingError]

  /**
    * A Type of path segment where we don't care about the error.
    */
  type PathSegmentNoError[T] = PathSegment[T, DummyError]

  /** Trait factory */
  def factory[T, A](
      matching: List[Segment] => Either[A, PathMatchOutput[T]],
      creating: T => List[Segment]
  ): PathSegment[T, A] = new PathSegment[T, A] {
    def matchSegments(segments: List[Segment]): Either[A, PathMatchOutput[T]] = matching(segments)

    def createSegments(t: T): List[Segment] = creating(t)
  }

  /** Simple path segment that matches everything by passing segments down the line. */
  final def empty: PathSegment[Unit, Nothing] =
    factory[Unit, Nothing](segments => Right(PathMatchOutput((), segments)), _ => Nil)
  final def root: PathSegment[Unit, Nothing] = empty

  /** Simple path segment that matches nothing. This is the neutral of the || operator. */
  final def noMatch[A](implicit pathMatchingError: PathMatchingError[A]): PathSegment[Unit, A] =
    factory[Unit, A](_ => Left(pathMatchingError.unit), _ => Nil)

  /**
    * Simple trait factory for "single segment"-oriented path Segments.
    *
    * This can be used to match a simple String, or a simple Int, etc...
    */
  final def simplePathSegment[T, A](matching: Segment => Either[A, T], creating: T => Segment)(
      implicit pathMatchingError: PathMatchingError[A]
  ): PathSegment[T, A] =
    factory(
      (_: Seq[Segment]) match {
        case Nil           => Left(pathMatchingError.missingSegment)
        case first :: rest => matching(first).map(PathMatchOutput(_, rest))
      },
      List(_).map(creating)
    )

  /** Matches a simple String and returning it. */
  final def stringSegment[A](implicit pathMatchingError: PathMatchingError[A]): PathSegment[String, A] =
    segment[String, A]

  /** Matches a simple Int and tries to convert it to an Int. */
  final def intSegment[A](
      implicit pathMatchingError: PathMatchingError[A],
      fromThrowable: ErrorFromThrowable[A]
  ): PathSegment[Int, A] = segment[Int, A]

  /**
    * Creates a segment matching any element of type `T`, as long as the [[urldsl.vocabulary.FromString]] can
    * de-serialize it.
    */
  final def segment[T, A](
      implicit fromString: FromString[T, A],
      printer: Printer[T],
      error: PathMatchingError[A]
  ): PathSegment[T, A] = simplePathSegment[T, A]((fromString.apply _).compose(_.content), printer.print)

  /**
    * Check that the segments ends at this point.
    */
  final def endOfSegments[A](implicit pathMatchingError: PathMatchingError[A]): PathSegment[Unit, A] = factory[Unit, A](
    (_: List[Segment]) match {
      case Nil => Right(PathMatchOutput((), Nil))
      case ss  => Left(pathMatchingError.endOfSegmentRequired(ss))
    },
    _ => Nil
  )

  /**
    * Consumes all the remaining segments.
    *
    * This can be useful for static resources.
    */
  final def remainingSegments[A]: PathSegment[List[String], A] = factory[List[String], A](
    segments => Right(PathMatchOutput(segments.map(_.content), Nil)),
    _.map(Segment.apply)
  )

  /**
    * [[PathSegment]] that matches one of the given different possibilities.
    *
    * This can be useful in a Router, when you want to delegate the final decision to an inner router.
    * Since all possibilities are good, the creation of segment simply takes the first one.
    */
  final def oneOf[T, A](t: T, ts: T*)(
      implicit fromString: FromString[T, A],
      printer: Printer[T],
      pathMatchingError: PathMatchingError[A]
  ): PathSegment[Unit, A] = {
    val allTs = t +: ts.toList
    simplePathSegment(
      s =>
        fromString(s.content)
          .filterOrElse(
            allTs.contains,
            pathMatchingError.wrongValue("One of: " + allTs.map(printer.apply).mkString(", "), s.content)
          )
          .map(_ => ()),
      (_: Unit) => t
    )
  }

  /**
    * Returns a [[urldsl.language.PathSegment]] which matches exactly the argument `t`.
    *
    * This conversion is implicit if you can provide a [[urldsl.vocabulary.FromString]] and a
    * [[urldsl.vocabulary.Printer]], so that it enables writing,
    * e.g.,
    * `root / "hello" / true`
    */
  implicit final def unaryPathSegment[T, A](
      t: T
  )(
      implicit fromString: FromString[T, A],
      printer: Printer[T],
      pathMatchingError: PathMatchingError[A]
  ): PathSegment[Unit, A] =
    simplePathSegment(
      s =>
        fromString(s.content)
          .filterOrElse[A](_ == t, pathMatchingError.wrongValue(printer(t), s.content))
          .map(_ => ()),
      (_: Unit) => Segment(printer(t))
    )

  final lazy val dummyErrorImpl = PathSegmentImpl[DummyError]
  final lazy val simplePathErrorImpl = PathSegmentImpl[SimplePathMatchingError]

}
