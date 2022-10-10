package urldsl.language

import urldsl.errors.ParamMatchingError
import urldsl.vocabulary.{FromString, Printer}

trait QueryParametersImpl[A] {

  implicit protected val queryError: ParamMatchingError[A]

  @deprecated(
    "empty was poorly named, and is replaced by `ignore`. The semantic for empty might change in the future!",
    since = "0.3.0"
  )
  val empty: QueryParameters[Unit, A] = QueryParameters.empty

  val ignore: QueryParameters[Unit, A] = QueryParameters.ignore

  def param[Q](paramName: String)(implicit fromString: FromString[Q, A], printer: Printer[Q]): QueryParameters[Q, A] =
    QueryParameters.param(paramName)

  def listParam[Q](
      paramName: String
  )(implicit fromString: FromString[Q, A], printer: Printer[Q]): QueryParameters[List[Q], A] =
    QueryParameters.listParam(paramName)

}

object QueryParametersImpl {

  /** Invoker */
  def apply[A](implicit error: ParamMatchingError[A]): QueryParametersImpl[A] = new QueryParametersImpl[A] {
    implicit protected val queryError: ParamMatchingError[A] = error
  }

}
