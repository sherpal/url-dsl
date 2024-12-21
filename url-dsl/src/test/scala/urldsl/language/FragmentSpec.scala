package urldsl.language

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import urldsl.errors.{FragmentMatchingError, SimpleFragmentMatchingError}
import urldsl.language.Fragment.simpleFragmentErrorImpl._
import urldsl.vocabulary.{FromString, MaybeFragment}

final class FragmentSpec extends munit.FunSuite {

  val fragment: Fragment[Unit, SimpleFragmentMatchingError] = 5
  val url = "http://localhost/#5"
  val error: FragmentMatchingError[SimpleFragmentMatchingError] = SimpleFragmentMatchingError.itIsFragmentMatchingError

  test("Implicit conversion to fragment should match the singleton") {
    assertEquals(fragment.matchRawUrl(url), Right(()))
    assertEquals(fragment.matchFragment(MaybeFragment(Some("5"))), Right(()))
  }

  test("it should fail when decoding a non-int") {
    assertEquals(
      fragment.matchFragment(MaybeFragment(Some("hello"))).left.map(_.toString),
      FromString[Int, SimpleFragmentMatchingError].apply("hello").map(_ => ()).left.map(_.toString)
    )
  }

  test("it should not match the singleton when it's different") {
    assertEquals(fragment.matchFragment(MaybeFragment(Some("7"))), Left(error.wrongValue(7, 5)))
  }

  test("it should not match the singleton when it's empty") {
    assertEquals(fragment.matchFragment(MaybeFragment(None)), Left(error.missingFragmentError))
  }

}
