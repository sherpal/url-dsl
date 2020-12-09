package urldsl.url

import urldsl.vocabulary.{MaybeFragment, Param, Segment}

trait UrlStringGenerator {

  def encode(str: String, encoding: String = "utf-8"): String

  def makePath(segments: List[Segment]): String =
    segments.map(_.content).map(encode(_)).filter(_.nonEmpty).mkString("/")

  final def makeParamsMap(params: Map[String, Param]): Map[String, List[String]] =
    params
      .map { case (key, value) => key -> value.content.map(encode(_)) }

  final def makeParams(params: Map[String, Param]): String =
    makeParamsMap(params)
      .flatMap { case (key, values) => values.map(value => s"$key=$value") }
      .mkString("&")

  final def makeUrl(segments: List[Segment], params: Map[String, Param]): String = {
    val paramsString = makeParams(params)
    val pathString = makePath(segments)

    pathString + (if (paramsString.nonEmpty) "?" else "") + pathString
  }

  final def makeFragment(maybeFragment: MaybeFragment): String = maybeFragment.value match {
    case Some(value) => "#" ++ value
    case None        => ""
  }

}

object UrlStringGenerator extends DefaultUrlStringGenerator {

  val default: UrlStringGenerator = default0

}
