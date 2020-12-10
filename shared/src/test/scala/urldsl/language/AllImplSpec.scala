package urldsl.language

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import urldsl.vocabulary.UrlMatching

/** Simple specs to test whether everything combines nicely. */
//noinspection TypeAnnotation
final class AllImplSpec extends AnyFlatSpec with Matchers {

  import simpleErrorImpl._

  val sampleUrl = "http://www.some-domain.be/hey/you/23?what=foo&who=babar#some-fragment"

  val anySegment = segment[String].ignore("any")
  val path = root / "hey" / anySegment / segment[Int]
  val query = param[String]("what")
  val ref = fragment[String]

  "Simple individual matching" should "work" in {

    path.matchRawUrl(sampleUrl) should be(Right(23))
    query.matchRawUrl(sampleUrl) should be(Right("foo"))
    ref.matchRawUrl(sampleUrl) should be(Right("some-fragment"))

  }

  "Associating path and query" should "be the same as path and query separately" in {

    (path ? query).matchRawUrl(sampleUrl) should be(for {
      p <- path.matchRawUrl(sampleUrl)
      q <- query.matchRawUrl(sampleUrl)
    } yield UrlMatching(p, q))

  }

  "Associating path and fragment" should "be the same as path and fragment separately" in {}

}
