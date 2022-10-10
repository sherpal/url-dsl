package urldsl

import urldsl.errors.{
  DummyError,
  FragmentMatchingError,
  ParamMatchingError,
  PathMatchingError,
  SimpleFragmentMatchingError,
  SimpleParamMatchingError,
  SimplePathMatchingError
}

package object language {

  lazy val dummyErrorImpl: AllImpl[DummyError, DummyError, DummyError] = AllImpl[DummyError, DummyError, DummyError]
  lazy val simpleErrorImpl: AllImpl[SimplePathMatchingError, SimpleParamMatchingError, SimpleFragmentMatchingError] =
    AllImpl[SimplePathMatchingError, SimpleParamMatchingError, SimpleFragmentMatchingError]

}
