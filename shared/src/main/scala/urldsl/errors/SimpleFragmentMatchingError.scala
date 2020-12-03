package urldsl.errors

sealed trait SimpleFragmentMatchingError

object SimpleFragmentMatchingError {
  case object MissingFragmentError extends SimpleFragmentMatchingError
  case class WrongValue[T](actual: T, expected: T) extends SimpleFragmentMatchingError
  case class FromThrowable(throwable: Throwable) extends SimpleFragmentMatchingError

  implicit val errorFromThrowable: ErrorFromThrowable[SimpleFragmentMatchingError] = (throwable: Throwable) =>
    FromThrowable(throwable)

  implicit val itIsFragmentMatchingError: FragmentMatchingError[SimpleFragmentMatchingError] =
    new FragmentMatchingError[SimpleFragmentMatchingError] {
      def missingFragmentError: SimpleFragmentMatchingError = MissingFragmentError

      def wrongValue[T](actual: T, expected: T): SimpleFragmentMatchingError = WrongValue(actual, expected)
    }
}
