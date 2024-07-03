package urldsl.vocabulary

/** Represents a bijection between `Left` and `Right`.
  *
  * This bijection is supposed to be "exception-free", and be an actual bijection.
  */
trait Codec[Left, Right] {

  def leftToRight(left: Left): Right
  def rightToLeft(right: Right): Left

  def ++[To](that: Codec[Right, To]): Codec[Left, To] = Codec.factory(
    this.leftToRight _ andThen that.leftToRight,
    that.rightToLeft _ andThen this.rightToLeft
  )

}

object Codec {

  def apply[T, U](implicit codec: Codec[T, U]): Codec[T, U] = codec

  def factory[T, U](tToU: T => U, uToT: U => T): Codec[T, U] = new Codec[T, U] {
    def leftToRight(left: T): U = tToU(left)
    def rightToLeft(right: U): T = uToT(right)
  }

  implicit def composeCodecs[Left, Middle, Right](implicit
      leftCodec: Codec[Left, Middle],
      rightCodec: Codec[Middle, Right]
  ): Codec[Left, Right] = leftCodec ++ rightCodec

  implicit def liftCodec[Left, Right](implicit codec: Codec[Left, Right]): Codec[List[Left], List[Right]] =
    factory(_.map(codec.leftToRight), _.map(codec.rightToLeft))

  implicit def zipCodec[T, U]: Codec[(List[T], List[U]), List[(T, U)]] = factory(
    { case (ls1, ls2) => ls1 zip ls2 },
    _.unzip
  )

}
