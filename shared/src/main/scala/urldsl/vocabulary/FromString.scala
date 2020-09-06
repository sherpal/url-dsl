package urldsl.vocabulary

import scala.language.`3.1`

import urldsl.errors.ErrorFromThrowable

import scala.util.{Failure, Success, Try}

trait FromString[T, A] {

  def fromString(str: String): Either[A, T]

  final def apply(str: String): Either[A, T] = fromString(str)

}

object FromString extends FromStringWithNumeric {

  def apply[T, A](implicit fromString: FromString[T, A]): FromString[T, A] = fromString

  def factory[T, A](read: String => Either[A, T]): FromString[T, A] = (str: String) => read(str)

  given stringFromString [A] as FromString [String, A] {
    def fromString(str: String): Either[A, String] = Right(str)
  }
  given intFromString [A] (using fromThrowable: ErrorFromThrowable[A]) as FromString[Int, A] {
    def fromString(str: String) =
      Try(str.toInt) match {
        case Success(int)       => Right(int)
        case Failure(exception) => Left(fromThrowable.fromThrowable(exception))
      }

  }

  given doubleFromString [A] (using fromThrowable: ErrorFromThrowable[A]) as FromString[Double, A] {
    def fromString(str: String) =
      Try(str.toDouble) match {
        case Success(double)    => Right(double)
        case Failure(exception) => Left(fromThrowable.fromThrowable(exception))
      }
  }
  given booleanFromString [A] (using fromThrowable: ErrorFromThrowable[A]) as FromString[Boolean, A] {
    def fromString(str: String) =
      Try(str.toBoolean) match {
        case Success(bool) => Right(bool)
        case Failure(_)    => Left(fromThrowable.fromThrowable(new Exception(s"$str is not a Boolean")))
      }
  }

}
