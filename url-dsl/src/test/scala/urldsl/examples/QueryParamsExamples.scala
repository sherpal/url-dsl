package urldsl.examples

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import urldsl.errors.SimpleParamMatchingError
import urldsl.language.QueryParameters

/** This class exposes some example of usage of the [[urldsl.language.QueryParameters]] class.
  *
  * The `sampleUrl` used throughout this example class is defined in the [[urldsl.examples]] package object.
  */
final class QueryParamsExamples extends AnyFlatSpec with Matchers {

  import urldsl.language.simpleErrorImpl._

  "Some matching examples" should "work" in {

    /** You can match a simple information from the query parameter. This param will look for the parameter "bar" in the
      * query string.
      */
    param[String]("bar").matchRawUrl(sampleUrl) should be(Right("stuff"))

    /** By default, special symbols are decoded automatically. */
    param[String]("babar").matchRawUrl(sampleUrl) should be(Right("other stuff"))

    /** You can ask to read a query parameter as a specific type, as long as the right implicits are in scope. */
    param[Boolean]("ok").matchRawUrl(sampleUrl) should be(Right(true))

    /** But you can always match the same parameter as a string. */
    param[String]("ok").matchRawUrl(sampleUrl) should be(Right("true"))

    /** Parameter encoding a tuple */
    param[(Int, Int)]("tuple").matchRawUrl(sampleUrl) should be(Right((11, 22)))

    /** Sometimes parameters are actually a list of parameters with the same key. You can read this as well. Note that
      * we sort the list for the check, since the actual order is somewhat unpredictable (although deterministic).
      */
    listParam[Int]("other").matchRawUrl(sampleUrl).map(_.sorted) should be(Right(List(2, 3)))

    listParam[String]("non-existent").matchRawUrl("http://foo.com/") should be(Right(Nil))

    /** You can compose several [[urldsl.language.QueryParameters]] with the `&` operator. */
    (param[String]("bar") & param[String]("babar")).matchRawUrl(sampleUrl) should be(
      Right(("stuff", "other stuff"))
    )

    /** Composing with a tupled parameter */
    (param[String]("ok") & param[(Int, Int)]("tuple")).matchRawUrl(sampleUrl) should be(Right(("true", 11, 22)))
    (param[(Int, Int)]("tuple") & param[String]("ok")).matchRawUrl(sampleUrl) should be(Right((11, 22, "true")))

    /** Swapping the operands will match the same query strings, but the output is interchanged! */
    (param[String]("babar") & param[String]("bar")).matchRawUrl(sampleUrl) should be(
      Right(("other stuff", "stuff"))
    )

    /** If you want your parameters to match optionally, you can ascribe your [[urldsl.language.QueryParameters]] with
      * `.?`.
      */
    param[String]("does-not-exist").?.matchRawUrl(sampleUrl) should be(Right(None))
    param[String]("bar").?.matchRawUrl(sampleUrl) should be(Right(Some("stuff")))
    param[String]("empty").?.matchRawUrl(sampleUrl) should be(Right(Some("")))

    param[(Int, Int)]("tuple").?.matchRawUrl(sampleUrl) should be(Right(Some((11, 22))))

    /** Decoding failures on optional params result in None */
    param[Int]("bar").?.matchRawUrl(sampleUrl) should be(Right(None))
    param[Int]("empty").?.matchRawUrl(sampleUrl) should be(Right(None))

    /** [[urldsl.language.QueryParameters]] have a filter method allowing to restrict the things it matches.
      */
    param[String]("bar").filter(_.length > 3, _ => "too short").matchRawUrl(sampleUrl) should be(
      Right(
        "stuff"
      )
    )
    // Note: this is poorly typed as the error type is Any, here.
    param[String]("bar").filter(_.length > 10, _ => "too short").matchRawUrl(sampleUrl) should be(
      Left(
        "too short"
      )
    )

    /** The filter and ? combinators can be conveniently combined together. This is because ? erases any previously
      * encountered error and returns None.
      */
    param[String]("bar")
      .filter(_.length > 10, _ => SimpleParamMatchingError.FromThrowable(new RuntimeException("too short")))
      .?
      .matchRawUrl(sampleUrl) should be(Right(None))

  }

  "Some matching examples" should "fail" in {

    /** Trying to ask for an absent parameter leads to an error */
    param[String]("does-not-exist").matchRawUrl(sampleUrl) should be(
      Left(SimpleParamMatchingError.MissingParameterError("does-not-exist"))
    )

    /** Trying to decode with the wrong type leads to an error. */
    param[Double]("bar")
      .matchRawUrl(sampleUrl)
      .swap
      .map(_.isInstanceOf[SimpleParamMatchingError.FromThrowable])
      .swap should be(
      Left(true)
    )

    /** [[urldsl.language.QueryParameters]] are immutable objects, but used in a single parameter search, this fact is
      * probably often useless. However, you can re-use it to combine with other parameters.
      */
    val p = param[Int]("other")
    (p & p).matchRawUrl(sampleUrl) should be(Left(SimpleParamMatchingError.MissingParameterError("other")))

  }

  "Some generating examples" should "work" in {

    /** You can also use [[urldsl.language.QueryParameters]] for generating query String */
    (param[String]("p1") & param[Int]("p2")).createPart(("a string", 24)) should be(
      """p1=a%20string&p2=24"""
    )

    /** Flatenning of tuple param */
    (param[String]("p1") & param[(Int, Int)]("p2") & param[Int]("p3")).createPart(("a string", 11, 22, 24)) should be(
      """p1=a%20string&p2=11-22&p3=24"""
    )

    (param[String]("p1") & listParam[String]("p2")).createPart(("hey", List("one", "two"))) should be(
      """p1=hey&p2=one&p2=two"""
    )

    listParam[String]("p1").createPart(Nil) should be("")

    /** Flatenning of tuple param */
    (param[String]("p1") & param[(Int, Int)]("p2") & listParam[String]("p3"))
      .createPart(("hey", 11, 22, List("one", "two"))) should be(
      """p1=hey&p2=11-22&p3=one&p3=two"""
    )

  }

  "No apparent limit to tuple composition" should "compile" in {
    listParam[String]("a").? &
      param[String]("b") &
      param[String]("c") &
      param[String]("d").? &
      param[String]("e") &
      param[String]("f") &
      param[String]("g") &
      listParam[String]("h") &
      param[String]("i") &
      param[String]("j") &
      param[Int]("k") &
      param[Int]("l") &
      param[String]("m") &
      param[String]("n") &
      param[String]("o") &
      param[String]("p")
  }

}
