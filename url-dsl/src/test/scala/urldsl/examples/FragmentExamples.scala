package urldsl.examples

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import urldsl.errors.SimpleFragmentMatchingError
import urldsl.language.Fragment

/** This class exposes some example of usage of the [[urldsl.language.Fragment]] class.
  *
  * The `sampleUrl` and `sampleUrlWithoutFragment` used throughout this example class is defined in the
  * [[urldsl.examples]] package object.
  */
final class FragmentExamples extends AnyFlatSpec with Matchers {

  import urldsl.language.simpleErrorImpl._

  "Some matching examples" should "work" in {

    /** We can ask that the fragment should be some specific value. Note that unlike [[urldsl.language.PathSegment]],
      * there is no "root" element and hence, we need to cast an element by hand into a [[urldsl.language.Fragment]].
      * There is still an implicit conversion, but it will be called only in a place where you ask for a
      * [[urldsl.language.Fragment]] (see below)
      */
    asFragment("the-ref").matchRawUrl(sampleUrl) should be(Right(()))

    /** We can also ask that the fragment is present and of the desired type. */
    fragment[String].matchRawUrl(sampleUrl) should be(Right("the-ref"))

    /** We can require that the fragment is not there (which would also be the case if the url ends with #!)
      */
    emptyFragment.matchRawUrl(sampleUrl) should be(Left(SimpleFragmentMatchingError.FragmentWasPresent("the-ref")))
    emptyFragment.matchRawUrl(sampleUrlWithoutFragment) should be(Right(()))

    /** We can ask for the fragment, or None if the fragment is not present. */
    maybeFragment[String].matchRawUrl(sampleUrl) should be(Right(Some("the-ref")))
    maybeFragment[String].matchRawUrl(sampleUrlWithoutFragment) should be(Right(None))

    /** When your [[urldsl.language.Fragment]] is optional, you can rely on the extra `getOrElse` method. */
    val stringOrDefault = maybeFragment[String].getOrElse("default value")
    stringOrDefault.matchRawUrl(sampleUrl) should be(Right("the-ref"))
    stringOrDefault.matchRawUrl(sampleUrlWithoutFragment) should be(Right("default value"))

    /** Below we can witness the implicit conversion to a fragment. */
    def askForFragment(
        f: Fragment[Unit, SimpleFragmentMatchingError]
    ): Fragment[Unit, SimpleFragmentMatchingError] = f

    askForFragment("the-ref").matchRawUrl(sampleUrl) should be(Right(()))

  }

}
