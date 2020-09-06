package urldsl.language

import urldsl.errors.ParamMatchingError
import urldsl.vocabulary.{FromString, Printer}

final class QueryParametersImpl[A](using error: ParamMatchingError[A]) {

  val empty: QueryParameters[Unit, A] = QueryParameters.empty[A]

  def param[Q](paramName: String)(using fromString: FromString[Q, A], printer: Printer[Q]): QueryParameters[Q, A] =
    QueryParameters.param(paramName)

  def listParam[Q](
      paramName: String
  )(using fromString: FromString[Q, A], printer: Printer[Q]): QueryParameters[List[Q], A] =
    QueryParameters.listParam(paramName)

}

object QueryParametersImpl {

  /** Invoker */
  def apply[A](using error: ParamMatchingError[A]): QueryParametersImpl[A] = new QueryParametersImpl[A]

}
