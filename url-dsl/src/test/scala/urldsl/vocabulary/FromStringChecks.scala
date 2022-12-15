package urldsl.vocabulary

import org.scalacheck._
import org.scalacheck.Prop._
import urldsl.errors.{DummyError, ErrorFromThrowable}

import scala.util.{Failure, Success, Try}

object FromStringChecks extends Properties("FromStringChecks") {

  property("Doubles can be decoded from string") = forAll { (x: Double) =>
    FromString[Double, DummyError].fromString(x.toString) == Right(x)
  }

  property("Decoding non-boolean string leads to error") = forAll(Gen.asciiStr) { (s: String) =>
    Try(s.toBoolean) match {
      case Failure(throwable) => FromString[Boolean, DummyError].fromString(s) == Left(ErrorFromThrowable[DummyError].fromThrowable(throwable))
      case Success(_) => true
    }
  }

}
