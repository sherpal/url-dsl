package urldsl.errors

/**
  * You can implement this trait for your own error type A, and give a implicit instance in order to use the
  * functionality requiring it, e.g., in [[urldsl.vocabulary.FromString]].
  *
  * @tparam A tye type of your error.
  */
trait ErrorFromThrowable[A] {

  def fromThrowable(throwable: Throwable): A

}
