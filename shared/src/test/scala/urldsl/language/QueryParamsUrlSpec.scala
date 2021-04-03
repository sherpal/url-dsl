package urldsl.language

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import urldsl.errors.DummyError
import urldsl.language.QueryParameters.dummyErrorImpl._
import urldsl.language.QueryParameters.dummyErrorImpl.{ignore => root}
import urldsl.vocabulary.Codec

final class QueryParamsUrlSpec extends AnyFlatSpec with Matchers {

  private implicit class QueryParamsEnhanced[Q](qp: QueryParameters[Q, DummyError]) {
    def params(q: Q): String = qp.createParamsString(q)
  }

  "QueryParameters" should "correctly generate string" in {

    root.params(()) should be("")

    param[String]("name").params("Hello") should be("name=Hello")
    (param[String]("name") & param[Int]("age")).params("Alice", 22) should be(
      "name=Alice&age=22"
    )

    listParam[String]("names").params(List("Alice", "Bob")) should be("names=Alice&names=Bob")

    // demonstrates quasi commutativity
    (listParam[Int]("ages") & param[String]("name")).params(List(32, 23), "Bob") should be(
      "ages=32&ages=23&name=Bob"
    )
    (param[String]("name") & listParam[Int]("ages")).params("Bob", List(32, 23)) should be(
      "name=Bob&ages=32&ages=23"
    )

    // testing with & and spaces characters
    param[String]("withSymbol").params("hi&friends") should be("withSymbol=hi%26friends")
    param[String]("withSpace").params("hi friends") should be("withSpace=hi%20friends")

  }

  "QueryParameters" should "correctly generate strings under `as` transformations" in {

    case class User(name: String, age: Int)
    implicit val userCodec: Codec[(String, Int), User] =
      Codec.factory((User.apply _).tupled, { case User(name, age) => (name, age) })

    (param[String]("name") & param[Int]("age")).as[User].params(User("Alice", 22)) should be(
      "name=Alice&age=22"
    )

    // in real life applications you should never stores lists that way in query parameters
    // the following call to `as` uses 4 implicit resolutions
    (listParam[String]("names") & listParam[Int]("ages"))
      .as[List[User]]
      .params(
        List(
          User("Alice", 22),
          User("Bob", 30)
        )
      ) should be(
      "names=Alice&names=Bob&ages=22&ages=30"
    )

  }

}
