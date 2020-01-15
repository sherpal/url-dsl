package urldsl.errors

/**
  * You can implement this trait for your own error type `A`, and provide an implicit instance in the companion object of
  * `A` in order to use all pre-defined [[urldsl.language.QueryParameters]].
  *
  * @example
  *          See implementations of [[DummyError]] and [[SimpleParamMatchingError]]
  *
  * @tparam A the type of your error.
  */
trait ParamMatchingError[A] {
  def missingParameterError(paramName: String): A
}
