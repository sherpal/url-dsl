package urldsl.language

import urldsl.vocabulary.{Codec, MaybeFragment}

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

  def as[U](tToU: T => U, uToT: U => T): Fragment[U, E] = factory(
    fragment => matchFragment(fragment).map(tToU),
    (u: U) => createFragment(uToT(u))
  )

  def as[U](implicit codec: Codec[T, U]): Fragment[U, E] = as[U](codec.leftToRight, codec.rightToLeft)

}

object Fragment {

  def factory[T, E](extractor: MaybeFragment => Either[E, T], generator: T => MaybeFragment): Fragment[T, E] =
    new Fragment[T, E] {
      def matchFragment(maybeFragment: MaybeFragment): Either[E, T] = extractor(maybeFragment)

      def createFragment(t: T): MaybeFragment = generator(t)
    }

}
