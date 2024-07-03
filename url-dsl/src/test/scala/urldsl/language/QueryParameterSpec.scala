package urldsl.language

import urldsl.errors.{DummyError, ParamMatchingError}
import urldsl.vocabulary.{Param, ParamMatchOutput}

final class QueryParameterSpec extends munit.FunSuite {

  val impl: AllImpl[DummyError, DummyError, DummyError] = dummyErrorImpl
  val error: ParamMatchingError[DummyError] = DummyError.dummyErrorIsParamMatchingError

  import impl._

  test("Matching the empty list works") {
    assertEquals(
      listParam[Int]("my-list").matchParams(Map("my-list" -> Param(Nil))),
      Right(ParamMatchOutput(List.empty[Int], Map.empty))
    )
  }

  test("Matching single element when list of param is empty fails") {
    assertEquals(param[Int]("a").matchParams(Map("a" -> Param(Nil))), Left(error.missingParameterError("a")))
  }

}
