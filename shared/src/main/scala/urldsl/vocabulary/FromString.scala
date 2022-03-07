package urldsl.vocabulary

import urldsl.errors.ErrorFromThrowable

import scala.util.{Failure, Success, Try}

trait FromString[T, A] {

  def fromString(str: String): Either[A, T]

  def apply(str: String): Either[A, T] = fromString(str)

}

object FromString extends FromStringWithNumeric {

  def apply[T, A](implicit fromString: FromString[T, A]): FromString[T, A] = fromString

  def factory[T, A](read: String => Either[A, T]): FromString[T, A] = (str: String) => read(str)

  /**
    * Creates an instance of [[FromString]] from a function that "casts" a [[java.lang.String]] to an instance of T.
    * This cast can be unsafe. If it fails (throws), it will be caught and wrapped in the
    * [[ErrorFromThrowable.fromThrowable()]] method.
    */
  case class FromUnsafeMap[T, A](cast: String => T, fromThrowable: ErrorFromThrowable[A]) extends FromString[T, A] {
    def fromString(str: String): Either[A, T] = Try(cast(str)).toEither.left.map(fromThrowable.fromThrowable)
  }

  implicit def stringFromString[A]: FromString[String, A] = factory(Right(_))
  implicit def intFromString[A](implicit fromThrowable: ErrorFromThrowable[A]): FromString[Int, A] =
    FromUnsafeMap(_.toInt, fromThrowable)
  implicit def doubleFromString[A](implicit fromThrowable: ErrorFromThrowable[A]): FromString[Double, A] =
    FromUnsafeMap(_.toDouble, fromThrowable)
  implicit def booleanFromString[A](implicit fromThrowable: ErrorFromThrowable[A]): FromString[Boolean, A] =
    FromUnsafeMap(_.toBoolean, fromThrowable)
  implicit def uuidFromString[A](implicit fromThrowable: ErrorFromThrowable[A]): FromString[java.util.UUID, A] =
    FromUnsafeMap(java.util.UUID.fromString, fromThrowable)

}
