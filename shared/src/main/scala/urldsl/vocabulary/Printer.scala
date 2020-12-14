package urldsl.vocabulary

import java.util.UUID

trait Printer[T]:
  def print(t: T): String
  final def apply(t: T): String = print(t)


object Printer:

  def apply[T](using printer: Printer[T]): Printer[T] = printer

  def factory[T](printing: T => String): Printer[T] = (t: T) => printing(t)

  given Printer[String] = factory(identity)
  given [T](using Numeric[T]) as Printer[T] = factory(_.toString)
  given Printer[Boolean] = factory(_.toString)
  given Printer[UUID] = factory(_.toString)

