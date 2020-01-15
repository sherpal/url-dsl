package urldsl.vocabulary

import scala.language.implicitConversions

final case class Segment(content: String)

object Segment {

  implicit def simpleSegment[T](t: T)(implicit printer: Printer[T]): Segment = Segment(printer(t))

  def fromPath(path: String): List[Segment] = path.dropWhile(_ == '/').split("/").toList.map(apply)

}
