package urldsl.language

import org.scalacheck.Prop.forAllNoShrink
import org.scalacheck._
import urldsl.errors.DummyError
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
    val filtering = (_: Int) > 0

    param[Int]("x").filter(filtering).matchParams(params) ==
      (if (filtering(x)) Right(ParamMatchOutput(x, Map())) else Left(DummyError.dummyError))
  }

  property("'foo=' returns empty strings") =
    forAllNoShrink(Gen.alphaNumStr.filter(_.nonEmpty).filterNot(_.head.isDigit)) { (key: String) =>
      val p = param[String](key)

      val firstResult = p.matchRawUrl(s"http://localhost:8080/stuff?$key=").toOption
      val secondResult = p.matchRawUrl(s"http://www.my-website.com/stuff?$key=&other=2").toOption
      (Prop(firstResult.contains("")) && Prop(secondResult.contains(""))) :|
        s"""$firstResult
           |$secondResult""".stripMargin
    }

}
