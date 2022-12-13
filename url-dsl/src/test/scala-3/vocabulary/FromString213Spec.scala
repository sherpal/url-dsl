package urldsl.vocabulary

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import urldsl.errors.DummyError

final class FromString3Spec extends AnyFlatSpec with Matchers {

    def getTheT[T](s: String)(using fromString: FromString[T, DummyError]): Either[DummyError, T] = fromString(s)

    "The FromString implicit for Numeric" should "be correctly called" in {

        getTheT[BigInt]("123456789012345678901234567890") should be(
            Right(BigInt("123456789012345678901234567890"))
        )

        getTheT[Int]("123") should be (Right(123))

        getTheT[Double]("Hi") should be (Left(DummyError.dummyError))

    }

}