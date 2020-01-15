package urldsl.language

import org.scalacheck._
import urldsl.vocabulary.{Param, ParamMatchOutput}

import scala.util.Try

final class QueryParameterProperties extends Properties("QueryParameters") {
  import Prop.forAll

  import QueryParameters.dummyErrorImpl._

  property("QuasiCommutativity") = forAll { (x: String, y: String) =>
    val params = Map("x" -> Param(List(x)), "y" -> Param(List(y)))
    (param[String]("x") & param[String]("y")).matchParams(params) ==
      (param[String]("y") & param[String]("x")).matchParams(params).map(_.map(_.swap))
  }

  property("LeftInverse") = forAll { x: Int =>
    val p = param[Int]("x")

    p.matchParams(p.createParams(x)) == Right(ParamMatchOutput(x, Map()))
  }

  property("ListParam") = forAll(Gen.nonEmptyListOf(Gen.asciiStr)) { ls: List[String] =>
    val params = Map("ls" -> Param(ls))
    listParam[String]("ls").matchParams(params) == Right(ParamMatchOutput(ls.reverse, Map()))
  }

  property("OptionInt") = forAll { s: String =>
    val params = Map("s" -> Param(List(s)))
    val asInt = Try(s.toInt)
    val remainingParams = asInt.map(_ => Map[String, Param]()).getOrElse(params)

    param[Int]("s").?.matchParams(params) == Right(ParamMatchOutput(asInt.toOption, remainingParams))
  }

}
