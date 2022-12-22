package urldsl.language

import urldsl.errors.{DummyError, FragmentMatchingError, SimpleFragmentMatchingError}
import urldsl.url.{UrlStringGenerator, UrlStringParserGenerator}
import urldsl.vocabulary.{Codec, FromString, MaybeFragment, Printer}

import scala.language.implicitConversions
import scala.reflect.ClassTag

/**
  * Represents the fragment (or ref) of an URL, containing an information of type T, or an error of type E.
  *
  * @tparam T type represented by this PathSegment
  * @tparam E type of the error that this PathSegment produces on "illegal" url paths.
  */
trait Fragment[T, +E] extends UrlPart[T, E] {

  import Fragment.factory

  /**
    * Extract the information contained in this fragment, as an instance of T.
    *
    * @param maybeFragment raw fragment information from the URL
    * @return Right a T when the extraction was successful, and Left an error otherwise.
    */
  def matchFragment(maybeFragment: MaybeFragment): Either[E, T]

  def matchRawUrl(url: String, urlStringParserGenerator: UrlStringParserGenerator): Either[E, T] =
    matchFragment(urlStringParserGenerator.parser(url).maybeFragmentObj)

  /**
    * Creates a fragment information from an instance of T.
    */
  def createFragment(t: T): MaybeFragment

  /** Creates the Fragment string contained in the given instance of T. Automatically prepend # if non empty. */
  def fragmentString(t: T, encoder: UrlStringGenerator = UrlStringGenerator.default): String =
    encoder.makeFragment(createFragment(t))

  /** Sugar when {{{T =:= Unit}}}. */
  def fragmentString()(implicit ev: Unit =:= T): String = fragmentString(())

  def createPart(t: T, encoder: UrlStringGenerator = UrlStringGenerator.default): String =
    fragmentString(t, encoder)

  /** By-map this fragment into a type U. */
  def as[U](tToU: T => U, uToT: U => T): Fragment[U, E] = factory(
    fragment => matchFragment(fragment).map(tToU),
    (u: U) => createFragment(uToT(u))
  )

  def as[U](implicit codec: Codec[T, U]): Fragment[U, E] = as[U](codec.leftToRight _, codec.rightToLeft _)

  /**
    * Turns this fragment matching a `T` into a fragment matching an [[Option]] of T.
    * It will return Some(t) if t could be extracted, and None otherwise.
    *
    * The failure that happened and led to an error does not matter: it will result in None, no matter what.
    */
  def ? : Fragment[Option[T], E] = factory(
    matchFragment(_) match {
      case Left(_)      => Right(None)
      case Right(value) => Right(Some(value))
    }, {
      case Some(t) => createFragment(t)
      case None    => MaybeFragment(None)
    }
  )

  /**
    * Adds an extra satisfying criteria to the de-serialized output of this [[Fragment]].
    * When the output of this [[Fragment]] does not satisfy the given predicate, the given error is returned
    * instead.
    *
    * @param predicate criteria that the output has to verify
    * @param error error happening when it's not the case
    * @tparam E1 new type of the error
    * @return a new [[Fragment]] matching the same fragment information, but only when the predicate is satisfied
    */
  final def filter[E1 >: E](predicate: T => Boolean, error: MaybeFragment => E1): Fragment[T, E1] =
    Fragment.factory[T, E1](
      (maybeFragment: MaybeFragment) => matchFragment(maybeFragment).filterOrElse(predicate, error(maybeFragment)),
      createFragment
    )

  /** Sugar when `T =:= DummyError`. */
  final def filter(predicate: T => Boolean)(implicit ev: E <:< DummyError): Fragment[T, DummyError] = {
    // type F[+E1] = Fragment[T, E1]
    // ev.liftCo[F].apply(this).filter(predicate, _ => DummyError.dummyError)
    // we keep this ugliness below while supporting 2.12 todo[scala3] remove this
    this.asInstanceOf[Fragment[T, DummyError]].filter(predicate, _ => DummyError.dummyError)
  }

  /**
    * Returns a [[Fragment]] which outputs the contents of this [[Fragment]] when result is a [[Some]] and the
    * specified `default` value otherwise.
    * When generating the path, it will only generate paths corresponding to the [[Some]] case.
    *
    * @note This method is only available when `T =:= Option[U]`.
    *
    * @param default default value when output is empty
    */
  final def getOrElse[U](default: => U)(implicit ev: T =:= Option[U]): Fragment[U, E] =
    factory[U, E](
      (maybeFragment: MaybeFragment) => matchFragment(maybeFragment).map(ev(_).getOrElse(default)),
      //(u: U) => createFragment(ev.flip(Some(u)))
      // we keep the ugliness below while supporting 2.12 todo[scala3] remove
      (u: U) => createFragment(Some(u).asInstanceOf[T])
    )

}

object Fragment {

  def factory[T, E](extractor: MaybeFragment => Either[E, T], generator: T => MaybeFragment): Fragment[T, E] =
    new Fragment[T, E] {
      def matchFragment(maybeFragment: MaybeFragment): Either[E, T] = extractor(maybeFragment)
      def createFragment(t: T): MaybeFragment = generator(t)
    }

  /**
    * Creates a fragment matching any element of type `T`, as long as the [[urldsl.vocabulary.FromString]] can
    * de-serialize it.
    *
    * If the fragment is missing, returns an error.
    */
  def fragment[T, A](
      implicit fromString: FromString[T, A],
      printer: Printer[T],
      fragmentMatchingError: FragmentMatchingError[A]
  ): Fragment[T, A] = factory[T, A](
    {
      case MaybeFragment(None)           => Left(fragmentMatchingError.missingFragmentError)
      case MaybeFragment(Some(fragment)) => fromString(fragment)
    },
    (printer.apply _).andThen(Some(_)).andThen(MaybeFragment.apply)
  )

  /**
    * Creates a fragment matching any element of type `T`, as long as the [[urldsl.vocabulary.FromString]] can
    * de-serialize it.
    *
    * If the fragment is missing, returns None.
    */
  final def maybeFragment[T, A](
      implicit fromString: FromString[T, A],
      printer: Printer[T],
      fragmentMatchingError: FragmentMatchingError[A]
  ): Fragment[Option[T], A] = factory[Option[T], A](
    {
      case MaybeFragment(None)           => Right(None)
      case MaybeFragment(Some(fragment)) => fromString(fragment).map(Some(_))
    },
    (maybeT: Option[T]) => MaybeFragment(maybeT.map(printer.apply))
  )

  /** Imposes that the URL does not contain a Fragment. */
  final def empty[A](implicit fragmentMatchingError: FragmentMatchingError[A]): Fragment[Unit, A] = factory[Unit, A](
    {
      case MaybeFragment(None)           => Right(())
      case MaybeFragment(Some(fragment)) => Left(fragmentMatchingError.fragmentWasPresent(fragment))
    },
    (_: Unit) => MaybeFragment(None)
  )

  implicit def asFragment[T, A](t: T)(
      implicit fromString: FromString[T, A],
      printer: Printer[T],
      fragmentMatchingError: FragmentMatchingError[A],
      classTag: ClassTag[T]
  ): Fragment[Unit, A] = factory[Unit, A](
    {
      case MaybeFragment(None) => Left(fragmentMatchingError.missingFragmentError)
      case MaybeFragment(Some(fragment)) =>
        fromString(fragment) match {
          case Left(value)  => Left(value)
          case Right(decodedValue) => decodedValue match {
            case value: T if value == t => Right(())
            case _ => Left(fragmentMatchingError.wrongValue(decodedValue, t))
          }
        }
    },
    _ => MaybeFragment(Some(printer.print(t)))
  )

  lazy val dummyErrorImpl: FragmentImpl[DummyError] = FragmentImpl[DummyError]
  lazy val simpleFragmentErrorImpl: FragmentImpl[SimpleFragmentMatchingError] =
    FragmentImpl[SimpleFragmentMatchingError]

}
