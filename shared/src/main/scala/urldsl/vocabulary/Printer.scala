package urldsl.vocabulary

trait Printer[T]:

  def print(t: T): String

  final def apply(t: T): String = print(t)


object Printer:

  def apply[T](implicit printer: Printer[T]): Printer[T] = printer

  def factory[T](printing: T => String): Printer[T] = (t: T) => printing(t)

  given Printer[String] = factory(identity)
  given [T](using Numeric[T]) as Printer[T] = factory(_.toString)
  given Printer[Boolean] = factory(_.toString)
  // implicit def intPrinter: Printer[Int] = factory(_.toString)
  // implicit def longPrinter: Printer[Long] = factory(_.toString)
  // implicit def doublePrinter: Printer[Double] = factory(_.toString)
  // implicit def bigIntPrinter: Printer[BigInt] = factory(_.toString)
  // implicit def floatPrinter: Printer[Float] = factory(_.toString)

