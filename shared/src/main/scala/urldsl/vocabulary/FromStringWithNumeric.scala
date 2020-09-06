package urldsl.vocabulary

import scala.language.`3.1`

import urldsl.errors.ErrorFromThrowable

trait FromStringWithNumeric {

  given numericFromString [T, A] (
    using num: Numeric[T],
    fromThrowable: ErrorFromThrowable[A]
  ) as FromString[T, A] {
    def fromString(str: String) =
      num.parseString(str) match {
        case Some(t) => Right(t)
        case None    => Left(fromThrowable.fromThrowable(new Exception(s"$str is not numeric")))
      }
  }

}
