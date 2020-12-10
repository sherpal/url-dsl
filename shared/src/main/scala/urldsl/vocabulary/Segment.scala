package urldsl.vocabulary

import scala.language.implicitConversions

/** A [[Segment]] is a simple wrapper around a specific String content between two `/`. */
final case class Segment(content: String) extends AnyVal {
  def map(f: String => String): Segment = Segment(f(content))
}

object Segment {

  implicit def simpleSegment[T](t: T)(implicit printer: Printer[T]): Segment = Segment(printer(t))

  def fromPath(path: String): List[Segment] = path.dropWhile(_ == '/').split("/").toList.filter(_.nonEmpty).map(apply)

}
