package urldsl

import urldsl.errors.ErrorFromThrowable
import urldsl.vocabulary.{FromString, Printer}

import scala.util.{Failure, Success, Try}

/** This package shows examples of usage of url-dsl.
  *
  * When using url-dsl, you are free to chose any error adt system that you want. There are two built-in for you, the
  * [[urldsl.errors.SimplePathMatchingError]] (and siblings) and the [[urldsl.errors.DummyError]]. We will use the
  * former in these example. The latter being perfect in cases where you don't care about what caused the error (for
  * example a router) or when you use primarily url-dsl for generating urls, and not for parsing them.
  */
package object examples {

  val sampleUrl =
    "http://www.some-domain.be/foo/23/true?bar=stuff&babar=other%20stuff&other=2&other=3&tuple=11-22&empty=&ok=true#the-ref"

  val sampleUrlWithoutFragment =
    "http://www.some-domain.be/foo/23/true?bar=stuff&babar=other%20stuff&other=2&other=3&ok=true"

  implicit def intTupleFromDashedString[A](implicit
      fromThrowable: ErrorFromThrowable[A]
  ): FromString[(Int, Int), A] = FromString.factory { s =>
    Try {
      val parts = s.split("-")
      (parts(0).toInt, parts(1).toInt)
    } match {
      case Success((int1, int2)) => Right((int1, int2))
      case Failure(exception)    => Left(fromThrowable.fromThrowable(exception))
    }
  }

  implicit def intTupleDashedPrinter: Printer[(Int, Int)] = Printer.factory { case (int1, int2) =>
    int1.toString + "-" + int2.toString
  }
}
