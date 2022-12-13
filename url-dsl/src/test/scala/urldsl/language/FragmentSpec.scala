package urldsl.language

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import urldsl.errors.{FragmentMatchingError, SimpleFragmentMatchingError}
import urldsl.language.Fragment.simpleFragmentErrorImpl._
import urldsl.vocabulary.MaybeFragment

final class FragmentSpec extends AnyFlatSpec with Matchers {

  val fragment: Fragment[Unit, Any] = "hello"
  val url = "http://localhost/#hello"
  val error: FragmentMatchingError[SimpleFragmentMatchingError] = SimpleFragmentMatchingError.itIsFragmentMatchingError

  "Implicit conversion to fragment" should "match the singleton" in {
    fragment.matchRawUrl(url) should ===(Right(()))
    fragment.matchFragment(MaybeFragment(Some("hello"))) should ===(Right(()))
  }

  it should "not match the singleton when it's different" in {
    fragment.matchFragment(MaybeFragment(Some("other"))) should ===(Left(error.wrongValue("other", "hello")))
  }

  it should "not match the singleton when it's empty" in {
    fragment.matchFragment(MaybeFragment(None)) should ===(Left(error.missingFragmentError))
  }

}
