package urldsl.examples

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import urldsl.language.UrlPart.SimpleUrlPart
import urldsl.vocabulary.{PathQueryFragmentMatching, UrlMatching}

/**
  * This class shows a possible implementation of a Router. This could be used in a web frontend (using Scala.js) for
  * displaying the correct component, or in a web server for triggering the action corresponding to the calling route.
  *
  * All abstract models are actually instances of [[urldsl.language.UrlPart]], which easily allows to abstract away
  * the concrete choice the user could make as to how they want to match the URL. In this case, we will simply return
  * a value based on what was extracted from the route. This value will be an instance of the
  * [[urldsl.examples.RouterUseCaseExample#Output]], so that we can easily test that the correct route was called.
  *
  * We use the [[urldsl.errors.DummyError]] implementations since we really want to route correctly, we don't care about
  * why a particular url was not matched.
  */
final class RouterUseCaseExample extends AnyFlatSpec with Matchers {

  val beginUrl = "http://www.stuff.be/"

  case class Output(value: String)

  import urldsl.language.dummyErrorImpl._

  /** Link a [[urldsl.language.UrlPart]] matching some routes, to an action using the information extracted from the route. */
  sealed trait Route[T] {
    def urlPart: SimpleUrlPart[T]
    def action(t: T): Output

    final def apply(rawUrl: String): Option[Output] = urlPart.matchRawUrl(rawUrl).map(action).toOption
  }

  object Route {
    def apply[T](urlPart0: SimpleUrlPart[T])(action0: T => Output): Route[T] = new Route[T] {
      val urlPart: SimpleUrlPart[T] = urlPart0
      def action(t: T): Output = action0(t)
    }
  }

  /** Collection of [[Route]] to be tried in order when calling with a given url. */
  case class Router(routes: List[Route[_]]) {

    /**
      * Sequentially tries to match the given url with all the routes, and call the action of the first matching.
      */
    def maybeCallAction(rawUrl: String): Option[Output] =
      routes.view
        .map(_(rawUrl))
        .collectFirst {
          case Some(output) => output
        }

    /** Same as maybeCallAction with a default output when no match is found. */
    def callActionWithDefault(rawUrl: String, default: Output): Output = maybeCallAction(rawUrl).getOrElse(default)
  }

  object Router {
    def apply(routes: Route[_]*): Router = Router(routes.toList)
  }

  "Router" should "work" in {

    Router().maybeCallAction("") should be(None)

    val notFound = root / 404

    /** Definition of the router, with in-order defined routes. */
    val theRouter = Router(
      Route(root / endOfSegments)(_ => Output("home")),
      Route((root / "users").withFragment(fragment[String]).fragmentOnly)(
        (ref: String) => Output(s"Users view with fragment $ref")
      ),
      Route(((root / "admin") ? param[String]("username")).withFragment(fragment[String])) {
        case PathQueryFragmentMatching(_, query, f) => Output(s"Admin view for $query at $f")
      },
      Route((root / "admin") ? param[String]("username")) {
        case UrlMatching(_, username) => Output(s"Welcome, $username")
      },
      Route(root / "admin")(_ => Output("admin view.")),
      Route(notFound)(_ => Output("not found"))
    )

    /** Checks that the correct output is called with the given url. */
    theRouter.maybeCallAction(beginUrl) should be(Some(Output("home")))
    theRouter.maybeCallAction(beginUrl ++ "users#info") should be(Some(Output("Users view with fragment info")))
    theRouter.maybeCallAction(beginUrl ++ "admin") should be(Some(Output("admin view.")))
    theRouter.maybeCallAction(beginUrl ++ "admin?username=Antoine") should be(Some(Output("Welcome, Antoine")))
    theRouter.maybeCallAction(beginUrl ++ "404") should be(Some(Output("not found")))
    theRouter.maybeCallAction(beginUrl ++ "does-not-exist") should be(None)

  }

}
