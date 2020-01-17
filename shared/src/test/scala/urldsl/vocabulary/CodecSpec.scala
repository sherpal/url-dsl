package urldsl.vocabulary

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class CodecSpec extends AnyFlatSpec with Matchers {

  implicit val userCodec: Codec[(String, Int), User] = Codec.factory((User.apply _).tupled, {
    case User(s, x) => (s, x)
  })
  implicit def swapCodec[A, B]: Codec[(A, B), (B, A)] = Codec.factory(_.swap, _.swap)

  def useACodec[T, U](t: T)(implicit codec: Codec[T, U]): U = codec.leftToRight(t)

  "The implicit composed codec" should "be invoked" in {

    useACodec[(Int, String), User]((22, "Alice")) should be(User("Alice", 22))

  }

}
