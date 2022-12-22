package urldsl.language

import urldsl.errors.DummyError
import urldsl.url.UrlStringGenerator
import urldsl.vocabulary.PathQueryFragmentMatching

//noinspection TypeAnnotation
final class PathQueryFragmentReprSpecs extends munit.FunSuite {

  import urldsl.language.dummyErrorImpl._

  val theSegment = root / "hello"
  val theParam = ignore

  val urlRepr = (theSegment ? theParam).withFragment("x")

  val url = "http://localhost/hello#x"

  test("Only path can be called on all-Unit url repr") {
    assertEquals(urlRepr.pathOnly.createPart(), urlRepr.createPart(PathQueryFragmentMatching((), (), ())))
    assertEquals(urlRepr.pathOnly.matchRawUrl(url), urlRepr.matchRawUrl(url).map(_.path))
  }

  test("Only query can be called on all-Unit url repr") {
    assertEquals(urlRepr.queryOnly.createPart(), urlRepr.createPart(PathQueryFragmentMatching((), (), ())))
    assertEquals(urlRepr.queryOnly.matchRawUrl(url), urlRepr.matchRawUrl(url).map(_.query))
  }

  test("Only fragment can be called on all-Unit url repr") {
    assertEquals(urlRepr.fragmentOnly.createPart(), urlRepr.createPart(PathQueryFragmentMatching((), (), ())))
    assertEquals(urlRepr.fragmentOnly.matchRawUrl(url), urlRepr.matchRawUrl(url).map(_.fragment))
  }

  test("Create part with dummy encoder returns empty string") {
    assertEquals(urlRepr.pathOnly.createPart((_: String, _: String) => ""), "#")
  }

}
