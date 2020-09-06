package urldsl.vocabulary

import scala.language.`3.1`


import scala.language.implicitConversions

final case class Segment(content: String) {
  def map(f: String => String): Segment = Segment(f(content))
}

object Segment {

  given [T](using printer: Printer[T]) as Conversion[T, Segment] {
    def apply(t: T): Segment = Segment(printer(t))
  }

  def fromPath(path: String): List[Segment] = path.dropWhile(_ == '/').split("/").toList.filter(_.nonEmpty).map(apply)

}
