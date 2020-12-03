package urldsl.errors

trait FragmentMatchingError[E] {

  /** Happens when the fragment was expected but is missing. */
  def missingFragmentError: E

  /** Happens when the fragment is present but does not match the expected value. */
  def wrongValue[T](actual: T, expected: T): E
}
