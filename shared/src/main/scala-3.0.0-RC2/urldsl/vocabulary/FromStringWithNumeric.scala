package urldsl.vocabulary

import urldsl.errors.ErrorFromThrowable

trait FromStringWithNumeric {

  implicit def numericFromString[T, A](
      implicit num: Numeric[T],
      fromThrowable: ErrorFromThrowable[A]
  ): FromString[T, A] = FromString.factory(
    s =>
      num.parseString(s) match {
        case Some(t) => Right(t)
        case None    => Left(fromThrowable.fromThrowable(new Exception(s"$s is not numeric")))
      }
  )

}
