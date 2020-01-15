package urldsl.errors

sealed trait SimpleParamMatchingError

/**
  * An implementation of [[ParamMatchingError]] that simply wraps the trigger of the error inside its components.
  */
object SimpleParamMatchingError {

  case class MissingParameterError(paramName: String) extends SimpleParamMatchingError
  case class FromThrowable(throwable: Throwable) extends SimpleParamMatchingError

  implicit lazy val itIsParamMatchingError: ParamMatchingError[SimpleParamMatchingError] =
    (paramName: String) => MissingParameterError(paramName)

  implicit lazy val simpleParamMatchingErrorIsFromThrowable: ErrorFromThrowable[SimpleParamMatchingError] =
    (throwable: Throwable) => FromThrowable(throwable)

}
