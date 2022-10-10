package urldsl.examples

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import urldsl.vocabulary.{PathQueryFragmentMatching, UrlMatching}

/**
  * In this class, we showcase examples of combining:
  * - [[urldsl.language.PathSegment]]
  * - [[urldsl.language.QueryParameters]], and
  * - [[urldsl.language.Fragment]].
  *
  * Unlike other examples, we will use the [[urldsl.errors.DummyError]] implementations.
  *
  * When you combine these, you will end up manipulating objects that can seem nasty. However, the user experience
  * of code that you actually have to write should stay pretty.
  */
//noinspection TypeAnnotation
final class CombinedExamples extends AnyFlatSpec with Matchers {

  import urldsl.language.dummyErrorImpl._

  val pathPart = root / "foo" / segment[Int] / true
  val queryPart = param[String]("bar") & listParam[Int]("other")
  val fragmentPart = fragment[String]

  "Some matching examples" should "work" in {

    /** We can combine a [[urldsl.language.PathSegment]] and a [[urldsl.language.QueryParameters]] */
    (pathPart ? queryPart)
      .matchRawUrl(sampleUrl) should be(
      Right(UrlMatching(23, ("stuff", List(2, 3))))
    )

    /** We can combine a [[urldsl.language.PathSegment]] and a [[urldsl.language.Fragment]] */
    pathPart.withFragment(fragmentPart).matchRawUrl(sampleUrl) should be(
      Right(PathQueryFragmentMatching(23, (), "the-ref"))
    )

    /** And we can of course combine a [[urldsl.language.QueryParameters]] and a [[urldsl.language.Fragment]] */
    queryPart.withFragment(fragmentPart).matchRawUrl(sampleUrl) should be(
      Right(PathQueryFragmentMatching((), ("stuff", List(2, 3)), "the-ref"))
    )

    /**
      * Finally, we can combine everything.
      * Note that we first have to combine a [[urldsl.language.PathSegment]] and the [[urldsl.language.QueryParameters]]
      * and then add the [[urldsl.language.Fragment]].
      */
    (pathPart ? queryPart).withFragment(fragmentPart).matchRawUrl(sampleUrl) should be(
      Right(PathQueryFragmentMatching(23, ("stuff", List(2, 3)), "the-ref"))
    )

  }

  "This generating example" should "work" in {

    pathPart.withFragment(fragmentPart).createPart(PathQueryFragmentMatching(23, (), "some-other-ref")) should be(
      """foo/23/true#some-other-ref"""
    )

    queryPart
      .withFragment(fragmentPart)
      .createPart(PathQueryFragmentMatching((), ("stuff", List(2, 3)), "the-ref")) should be(
      """?bar=stuff&other=2&other=3#the-ref"""
    )

    (pathPart ? queryPart)
      .withFragment(fragmentPart)
      .createPart(PathQueryFragmentMatching(23, ("stuff", List(2, 3)), "the-ref")) should be(
      """foo/23/true?bar=stuff&other=2&other=3#the-ref"""
    )

  }

}
