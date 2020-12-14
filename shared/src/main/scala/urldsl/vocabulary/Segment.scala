package urldsl.vocabulary

import scala.language.implicitConversions

/** A [[Segment]] is a simple wrapper around a specific String content between two `/`. */
final case class Segment(content: String) extends AnyVal:
  def map(f: String => String): Segment = Segment(f(content))

object Segment:

  given [T](using Printer[T]) as Conversion[T, Segment]:
    def apply(t: T): Segment = Segment(Printer[T](t))

  def fromPath(path: String): List[Segment] = path.dropWhile(_ == '/').split("/").toList.filter(_.nonEmpty).map(apply)

