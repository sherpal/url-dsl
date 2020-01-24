package urldsl.vocabulary

import scala.language.implicitConversions

final case class Segment(content: String) {
  def map(f: String => String): Segment = Segment(f(content))
}

object Segment {

  implicit def simpleSegment[T](t: T)(implicit printer: Printer[T]): Segment = Segment(printer(t))

  def fromPath(path: String): List[Segment] = path.dropWhile(_ == '/').split("/").toList.filter(_.nonEmpty).map(apply)

}
