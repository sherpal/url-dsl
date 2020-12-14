package urldsl.vocabulary

import java.util.UUID

import urldsl.errors.ErrorFromThrowable

import scala.util.{Failure, Success, Try}

trait FromString[T, A]:
  def fromString(str: String): Either[A, T]
  def apply(str: String): Either[A, T] = fromString(str)


object FromString:

  def apply[T, A](using fromString: FromString[T, A]): FromString[T, A] = fromString

  def factory[T, A](read: String => Either[A, T]): FromString[T, A] = (str: String) => read(str)

  given [A] as FromString[String, A] = factory(Right(_))
  given[T, A](
      using num: Numeric[T],
      fromThrowable: ErrorFromThrowable[A]
  ) as FromString[T, A] = FromString.factory(
    s =>
      num.parseString(s) match {
        case Some(t) => Right(t)
        case None    => Left(fromThrowable.fromThrowable(new Exception(s"$s is not numeric")))
      }
  )
  given [A](using fromThrowable: ErrorFromThrowable[A]) as FromString[Boolean, A] = factory(
    s =>
      Try(s.toBoolean) match {
        case Success(bool) => Right(bool)
        case Failure(_)    => Left(fromThrowable.fromThrowable(new Exception(s"$s is not a Boolean")))
      }
  )
  given [A](using fromThrowable: ErrorFromThrowable[A]) as FromString[UUID, A] = factory(
    s => Try(UUID.fromString(s)) match {
      case Success(uuid) => Right(uuid)
      case Failure(exception) => Left(fromThrowable.fromThrowable(exception))
    }
  )

end FromString
