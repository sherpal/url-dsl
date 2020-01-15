package urldsl.vocabulary

import urldsl.errors.ErrorFromThrowable

import scala.util.{Failure, Success, Try}

trait FromString[T, A] {

  def fromString(str: String): Either[A, T]

  def apply(str: String): Either[A, T] = fromString(str)

}

object FromString {

  def apply[T, A](implicit fromString: FromString[T, A]): FromString[T, A] = fromString

  def factory[T, A](read: String => Either[A, T]): FromString[T, A] = (str: String) => read(str)

  implicit def stringFromString[A]: FromString[String, A] = factory(Right(_))
  implicit def intFromString[A](implicit fromThrowable: ErrorFromThrowable[A]): FromString[Int, A] = factory(
    s =>
      Try(s.toInt) match {
        case Success(int)       => Right(int)
        case Failure(exception) => Left(fromThrowable.fromThrowable(exception))
      }
  )
  implicit def doubleFromString[A](implicit fromThrowable: ErrorFromThrowable[A]): FromString[Double, A] = factory(
    s =>
      Try(s.toDouble) match {
        case Success(double)    => Right(double)
        case Failure(exception) => Left(fromThrowable.fromThrowable(exception))
      }
  )
  implicit def booleanFromString[A](implicit fromThrowable: ErrorFromThrowable[A]): FromString[Boolean, A] = factory(
    s =>
      Try(s.toBoolean) match {
        case Success(bool) => Right(bool)
        case Failure(_)    => Left(fromThrowable.fromThrowable(new Exception(s"$s is not a Boolean")))
      }
  )

  implicit def numericFromString[T, A](
      implicit num: Numeric[T],
      fromThrowable: ErrorFromThrowable[A]
  ): FromString[T, A] = factory(
    s =>
      num.parseString(s) match {
        case Some(t) => Right(t)
        case None    => Left(fromThrowable.fromThrowable(new Exception(s"$s is not numeric")))
      }
  )

}
