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

  val dummyErrorImpl: AllImpl[DummyError, DummyError, DummyError] = AllImpl[DummyError, DummyError, DummyError]
  val simpleErrorImpl: AllImpl[SimplePathMatchingError, SimpleParamMatchingError, SimpleFragmentMatchingError] =
    AllImpl[SimplePathMatchingError, SimpleParamMatchingError, SimpleFragmentMatchingError]

}
