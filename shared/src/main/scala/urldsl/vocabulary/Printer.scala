package urldsl.vocabulary

import scala.language.`3.1`

trait Printer[T] {

  def print(t: T): String

  final def apply(t: T): String = print(t)

}

object Printer {

  def apply[T](implicit printer: Printer[T]): Printer[T] = printer

  def factory[T](printing: T => String): Printer[T] = (t: T) => printing(t)

  given stringPrinter as Printer [String] {
    def print(t: String): String = t
  }
  given intPrinter as Printer [Int] {
    def print(t: Int): String = t.toString
  }
  given longPrinter as Printer [Long] {
    def print(t: Long): String = t.toString
  }
  given booleanPrinter as Printer [Boolean] {
    def print(t: Boolean): String = t.toString
  }
  given doublePrinter as Printer [Double] {
    def print(t: Double): String = t.toString
  }
  given bigIntPrinter as Printer [BigInt] {
    def print(t: BigInt): String = t.toString
  }
  given floatPrinter as Printer [Float] {
    def print(t: Float): String = t.toString
  }

}
