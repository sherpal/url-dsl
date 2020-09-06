package urldsl.errors

import scala.language.`3.1`


sealed trait SimpleParamMatchingError

/**
  * An implementation of [[ParamMatchingError]] that simply wraps the trigger of the error inside its components.
  */
object SimpleParamMatchingError {

  case class MissingParameterError(paramName: String) extends SimpleParamMatchingError
  case class FromThrowable(throwable: Throwable) extends SimpleParamMatchingError

  given ParamMatchingError[SimpleParamMatchingError] {
    def missingParameterError(paramName: String): MissingParameterError = MissingParameterError(paramName)
  }

  given ErrorFromThrowable[SimpleParamMatchingError] {
    def fromThrowable(throwable: Throwable): FromThrowable = FromThrowable(throwable)
  }

}
