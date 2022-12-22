package urldsl.language

import org.scalacheck._
import urldsl.errors.DummyError
import urldsl.vocabulary.{Codec, Param, ParamMatchOutput}

import scala.util.Try

final class QueryParameterProperties extends Properties("QueryParameters") {
  import Prop.forAll

  import QueryParameters.dummyErrorImpl._

  def error = DummyError.dummyErrorIsParamMatchingError

  property("QuasiCommutativity") = forAll { (x: String, y: String) =>
    val params = Map("x" -> Param(List(x)), "y" -> Param(List(y)))
    (param[String]("x") & param[String]("y")).matchParams(params) ==
      (param[String]("y") & param[String]("x")).matchParams(params).map(_.map(_.swap))
  }

  property("LeftInverse") = forAll { (x: Int) =>
    val p = param[Int]("x")

    p.matchParams(p.createParams(x)) == Right(ParamMatchOutput(x, Map()))
  }

  property("ListParam") = forAll(Gen.nonEmptyListOf(Gen.asciiStr)) { (ls: List[String]) =>
    val params = Map("ls" -> Param(ls))
    listParam[String]("ls").matchParams(params) == Right(ParamMatchOutput(ls, Map()))
  }

  property("OptionInt") = forAll { (s: String) =>
    val params = Map("s" -> Param(List(s)))
    val asInt = Try(s.toInt)
    val remainingParams = asInt.map(_ => Map[String, Param]()).getOrElse(params)

    param[Int]("s").?.matchParams(params) == Right(ParamMatchOutput(asInt.toOption, remainingParams))
  }

  property("Filtering") = forAll { (x: Int) =>
    val params = Map("x" -> Param(List(x.toString)))
    val predicate = (_: Int) > 0

    val intParam = param[Int]("x").filter(predicate)

    Prop(intParam.matchParams(params) ==
      (if (predicate(x)) Right(ParamMatchOutput(x, Map())) else Left(DummyError.dummyError))) && Prop(
      intParam.createParams(x) == params
    )
  }

  property("Single param maps") = forAll { (x: Int) =>
    param[Int]("a").createParamsMap(x) == Map("a" -> List(x.toString))
  }

  property("as is bijection with container") = forAll { (x: Int) =>
    case class Container(value: Int)
    implicit def codec: Codec[Int, Container] = Codec.factory(Container.apply, _.value)

    val containerParam = param[Int]("a").as[Container]

    val paramMap = Map("a" -> Param(List(x.toString)))

    Prop(containerParam.createParams(Container(x)) == paramMap) && Prop(
      containerParam.matchParams(paramMap) == Right(ParamMatchOutput(Container(x), Map.empty))
    )
  }

  property("Unused params pass to remaining") = forAll { (x: Int) =>
    val intParam = param[Int]("a")
    val paramMap = Map("a" -> Param(List(x.toString)), "b" -> Param(List("some-string")))

    intParam.matchParams(paramMap) == Right(ParamMatchOutput(x, paramMap - "a"))
  }

  property("Matching param fails on empty map") = forAll { (x: Int) =>
    val intParam = param[Int]("a")

    intParam.matchParams(Map.empty) == Left(error.missingParameterError("a"))
  }

}
