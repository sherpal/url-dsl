package urldsl.vocabulary

final class CodecSpecFor3 extends munit.FunSuite {
  implicit val userCodec: Codec[(String, Int), User] = Codec.factory(
    (User.apply _).tupled,
    { case User(s, x) =>
      (s, x)
    }
  )

  implicit def swapCodec[A, B]: Codec[(A, B), (B, A)] = Codec.factory(_.swap, _.swap)

  def useACodec[T, U](t: T)(implicit codec: Codec[T, U]): U = codec.leftToRight(t)

  def useACodecReverse[T, U](u: U)(implicit codec: Codec[T, U]): T = codec.rightToLeft(u)

  test("Summoner method is called") {
    assert(Codec[(Int, String), (String, Int)] != null)
  }

  test("The implicit composed codec should be invoked") {
    assertEquals(useACodec[(Int, String), User]((22, "Alice")), User("Alice", 22))
    assertEquals(useACodecReverse[(Int, String), User](User("Alice", 22)), (22, "Alice"))
  }

  test("Codec can be lifted to seq") {
    assertEquals(useACodec[List[(Int, String)], List[User]](List((22, "Alice"))), List(User("Alice", 22)))
    assertEquals(useACodecReverse[List[(Int, String)], List[User]](List(User("Alice", 22))), List((22, "Alice")))
  }

  test("Zip codec can be used") {
    assertEquals(
      useACodec[(List[String], List[Int]), List[(String, Int)]]((List("hello"), List(7))),
      List(("hello", 7))
    )
    assertEquals(
      useACodecReverse[(List[String], List[Int]), List[(String, Int)]](List(("hello", 7))),
      (List("hello"), List(7))
    )
  }

}
