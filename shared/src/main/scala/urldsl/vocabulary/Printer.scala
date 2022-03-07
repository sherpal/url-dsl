package urldsl.vocabulary

trait Printer[T] {

  def print(t: T): String

  final def apply(t: T): String = print(t)

}

object Printer {

  def apply[T](implicit printer: Printer[T]): Printer[T] = printer

  def factory[T](printing: T => String): Printer[T] = (t: T) => printing(t)

  private class ToStringPrinter[T] extends Printer[T] {
    def print(t: T): String = t.toString
  }

  implicit def stringPrinter: Printer[String] = new ToStringPrinter
  implicit def intPrinter: Printer[Int] = new ToStringPrinter
  implicit def longPrinter: Printer[Long] = new ToStringPrinter
  implicit def booleanPrinter: Printer[Boolean] = new ToStringPrinter
  implicit def doublePrinter: Printer[Double] = new ToStringPrinter
  implicit def bigIntPrinter: Printer[BigInt] = new ToStringPrinter
  implicit def floatPrinter: Printer[Float] = new ToStringPrinter
  implicit def uuidPrinter: Printer[java.util.UUID] = new ToStringPrinter

}
