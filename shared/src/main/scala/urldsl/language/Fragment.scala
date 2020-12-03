package urldsl.language

import urldsl.errors.FragmentMatchingError
import urldsl.url.UrlStringGenerator
import urldsl.vocabulary.{Codec, FromString, MaybeFragment, Printer}

import scala.language.implicitConversions

/**
  * Represents the fragment (or ref) of an URL, containing an information of type T, or an error of type E.
  *
  * @tparam T type represented by this PathSegment
  * @tparam E type of the error that this PathSegment produces on "illegal" url paths.
  */
trait Fragment[T, E] {

  import Fragment.factory

  /**
    * Extract the information contained in this fragment, as an instance of T.
    *
    * @param maybeFragment raw fragment information from the URL
    * @return Right a T when the extraction was successful, and Left an error otherwise.
    */
  def matchFragment(maybeFragment: MaybeFragment): Either[E, T]

  /**
    * Creates a fragment information from an instance of T.
    */
  def createFragment(t: T): MaybeFragment

  def fragmentString(t: T, encoder: UrlStringGenerator = UrlStringGenerator.default): String =
    encoder.makeFragment(createFragment(t))

  def fragmentString()(implicit ev: Unit =:= T): String = fragmentString(())

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
    (printer.apply _).andThen(Some(_)).andThen(MaybeFragment)
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

  implicit def fragmentFromExactValue[T, A](t: T)(
      implicit fromString: FromString[T, A],
      printer: Printer[T],
      fragmentMatchingError: FragmentMatchingError[A]
  ): Fragment[T, A] = factory[T, A](
    {
      case MaybeFragment(None) => Left(fragmentMatchingError.missingFragmentError)
      case MaybeFragment(Some(fragment)) =>
        fromString(fragment) match {
          case Left(value)  => Left(value)
          case Right(t)     => Right(t)
          case Right(value) => Left(fragmentMatchingError.wrongValue(value, t))
        }
    },
    (printer.apply _).andThen(Some(_)).andThen(MaybeFragment)
  )

}
