package urldsl.language

import urldsl.errors.{FragmentMatchingError, ParamMatchingError, PathMatchingError}

final class AllImpl[P, Q, F] private (
    using
    protected val pathError: PathMatchingError[P],
    protected val queryError: ParamMatchingError[Q],
    protected val fragmentError: FragmentMatchingError[F]
) extends PathSegmentImpl[P]
    with QueryParametersImpl[Q]
    with FragmentImpl[F]

object AllImpl:
  def apply[P, Q, F](
      using
      pathError: PathMatchingError[P],
      queryError: ParamMatchingError[Q],
      fragmentError: FragmentMatchingError[F]
  ): AllImpl[P, Q, F] = new AllImpl
