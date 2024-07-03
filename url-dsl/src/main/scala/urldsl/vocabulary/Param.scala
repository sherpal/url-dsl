package urldsl.vocabulary

final case class Param(content: List[String]) extends AnyVal {
  def transform(f: String => String): Param = Param(content.map(f))
}

object Param {

  def fromQueryString(queryString: String): Map[String, Param] =
    queryString
      .dropWhile(_ == '?')
      .split("&")
      .filter(_.nonEmpty)
      .map(_.split("=").toList)
      .toList
      .collect {
        case first :: Nil if first.nonEmpty           => first -> ""
        case first :: second :: Nil if first.nonEmpty => first -> second
      }
      .groupBy(_._1)
      .map { case (key, value) => key -> value.map(_._2) }
      .map { case (key, value) => key -> Param(value) }

}
