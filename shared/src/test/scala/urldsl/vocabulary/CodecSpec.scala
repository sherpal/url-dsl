package urldsl.vocabulary

import scala.language.`3.1`


import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class CodecSpec extends AnyFlatSpec with Matchers {

  given Codec[(String, Int), User] {

    def leftToRight(tuple: (String, Int)): User = User.tupled(tuple)
    def rightToLeft(user: User): (String, Int) = (user.name, user.age)

  }
  implicit def swapCodec[A, B]: Codec[(A, B), (B, A)] = Codec.factory(_.swap, _.swap)

  def useACodec[T, U](t: T)(implicit codec: Codec[T, U]): U = codec.leftToRight(t)

  "The implicit composed codec" should "be invoked" in {

    useACodec[(Int, String), User]((22, "Alice")) should be(User("Alice", 22))

  }

}
