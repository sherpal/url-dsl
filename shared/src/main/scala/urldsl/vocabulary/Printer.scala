package urldsl.vocabulary

trait Printer[T] {

  def print(t: T): String

  final def apply(t: T): String = print(t)

}

object Printer {

  def apply[T](implicit printer: Printer[T]): Printer[T] = printer

  def factory[T](printing: T => String): Printer[T] = (t: T) => printing(t)

  implicit def stringPrinter: Printer[String] = factory(identity)
  implicit def intPrinter: Printer[Int] = factory(_.toString)
  implicit def longPrinter: Printer[Long] = factory(_.toString)
  implicit def booleanPrinter: Printer[Boolean] = factory(_.toString)
  implicit def doublePrinter: Printer[Double] = factory(_.toString)
  implicit def bigIntPrinter: Printer[BigInt] = factory(_.toString)
  implicit def floatPrinter: Printer[Float] = factory(_.toString)

}
