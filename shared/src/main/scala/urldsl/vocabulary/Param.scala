package urldsl.vocabulary

final case class Param(content: List[String]) {
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
        case first :: second :: Nil => first -> second
      }
      .groupBy(_._1)
      .view
      .mapValues(_.map(_._2))
      .mapValues(Param(_))
      .toMap

}
