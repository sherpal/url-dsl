package urldsl.language

import urldsl.errors.ParamMatchingError
import urldsl.vocabulary.{FromString, Printer}

final class QueryParametersImpl[A](implicit error: ParamMatchingError[A]) {

  def param[Q](paramName: String)(implicit fromString: FromString[Q, A], printer: Printer[Q]): QueryParameters[Q, A] =
    QueryParameters.param(paramName)

  def listParam[Q](
      paramName: String
  )(implicit fromString: FromString[Q, A], printer: Printer[Q]): QueryParameters[List[Q], A] =
    QueryParameters.listParam(paramName)

}

object QueryParametersImpl {

  /** Invoker */
  def apply[A](implicit error: ParamMatchingError[A]): QueryParametersImpl[A] = new QueryParametersImpl[A]

}
