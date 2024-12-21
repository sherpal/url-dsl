package urldsl.language

import urldsl.errors.{FragmentMatchingError, ParamMatchingError, PathMatchingError}

final class AllImpl[P, Q, F] private (implicit
    val pathError: PathMatchingError[P],
    val queryError: ParamMatchingError[Q],
    val fragmentError: FragmentMatchingError[F]
) extends PathSegmentImpl[P]
    with QueryParametersImpl[Q]
    with FragmentImpl[F]

object AllImpl {
  def apply[P, Q, F](implicit
      pathError: PathMatchingError[P],
      queryError: ParamMatchingError[Q],
      fragmentError: FragmentMatchingError[F]
  ): AllImpl[P, Q, F] = new AllImpl
}
