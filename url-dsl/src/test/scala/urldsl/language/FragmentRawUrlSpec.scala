package urldsl.language

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import urldsl.language.Fragment.simpleFragmentErrorImpl._

//noinspection TypeAnnotation
final class FragmentRawUrlSpec extends AnyFlatSpec with Matchers {

  val stringFragment = fragment[String]

  "Parsing a fragment with a space" should "read %20 as space" in {
    val url = "http://localhost/#hey%20friends"
    fragment[String].matchRawUrl(url) shouldBe Right("hey friends")
  }

  "Generating a fragment with a space" should "put %20 instead" in {
    val str = "hey friends"
    stringFragment.createPart(str) should be("#" ++ str.replaceAll(" ", "%20"))
  }

  "Generating a fragment from an empty string" should "give the empty string" in {
    stringFragment.createPart("") should be("")
  }

}
