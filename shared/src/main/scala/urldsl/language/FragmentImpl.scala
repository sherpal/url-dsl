package urldsl.language

import urldsl.errors.FragmentMatchingError
import urldsl.vocabulary.{FromString, Printer}

import scala.reflect.ClassTag

/**
  * This is the analogue of [[PathSegmentImpl]] for the [[Fragment]] trait. It "pre-applies" the error type based on the
  * provided [[FragmentMatchingError]].
  *
  * @tparam E type of the "pre-applied" errors
  */
trait FragmentImpl[E] {

  /** implementation of [[FragmentMatchingError]] for generating relevant matching errors. */
  implicit protected val fragmentError: FragmentMatchingError[E]

  final def fragment[T](implicit fromString: FromString[T, E], printer: Printer[T]): Fragment[T, E] =
    Fragment.fragment[T, E]

  final def maybeFragment[T](implicit fromString: FromString[T, E], printer: Printer[T]): Fragment[Option[T], E] =
    Fragment.maybeFragment[T, E]

  final def emptyFragment: Fragment[Unit, E] = Fragment.empty

  final def asFragment[T](t: T)(
      implicit fromString: FromString[T, E],
      printer: Printer[T],
      classTag: ClassTag[T]
  ): Fragment[T, E] = t

}

object FragmentImpl {

  /** Summoner for a [[FragmentImpl]] instance, given the [[FragmentMatchingError]] error. */
  def apply[E](implicit error: FragmentMatchingError[E]): FragmentImpl[E] = new FragmentImpl[E] {
    implicit protected val fragmentError: FragmentMatchingError[E] = error
  }
}
