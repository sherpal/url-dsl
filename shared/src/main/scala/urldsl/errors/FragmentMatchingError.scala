package urldsl.errors

trait FragmentMatchingError[E] {

  /** Happens when the fragment was expected but is missing. */
  def missingFragmentError: E

  /** Happens when you don't want the fragment to be present, but it was there. */
  def fragmentWasPresent(value: String): E

  /** Happens when the fragment is present but does not match the expected value. */
  def wrongValue[T](actual: T, expected: T): E
}
