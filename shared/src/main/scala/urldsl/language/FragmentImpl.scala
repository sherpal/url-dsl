package urldsl.language

import urldsl.errors.FragmentMatchingError
import urldsl.vocabulary.{FromString, Printer}

import scala.reflect.ClassTag

/**
  * This is the analogue of [[PathSegmentImpl]] for the [[Fragment]] trait. It "pre-applies" the error type based on the
  * provided [[FragmentMatchingError]].
  *
  * @param error implementation of [[FragmentMatchingError]] for generating relevant matching errors.
  * @tparam E type of the "pre-applied" errors
  */
final class FragmentImpl[E](implicit error: FragmentMatchingError[E]) {

  def fragment[T](implicit fromString: FromString[T, E], printer: Printer[T]): Fragment[T, E] = Fragment.fragment[T, E]

  def maybeFragment[T](implicit fromString: FromString[T, E], printer: Printer[T]): Fragment[Option[T], E] =
    Fragment.maybeFragment[T, E]

  def emptyFragment: Fragment[Unit, E] = Fragment.empty

  def asFragment[T](t: T)(
      implicit fromString: FromString[T, E],
      printer: Printer[T],
      classTag: ClassTag[T]
  ): Fragment[T, E] = t

}

object FragmentImpl {

  /** Summoner for a [[FragmentImpl]] instance, given the [[FragmentMatchingError]] error. */
  def apply[E](implicit error: FragmentMatchingError[E]): FragmentImpl[E] = new FragmentImpl[E]
}
