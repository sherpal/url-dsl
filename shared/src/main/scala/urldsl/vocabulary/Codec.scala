package urldsl.vocabulary

import scala.language.`3.1`

/**
  * Represents a bijection between `Left` and `Right`.
  *
  * This bijection is supposed to be "exception-free", and be an actual bijection.
  */
trait Codec[Left, Right] {

  def leftToRight(left: Left): Right
  def rightToLeft(right: Right): Left

  @scala.annotation.alpha("++")
  final def ++[To](that: Codec[Right, To]): Codec[Left, To] = Codec.factory(
    this.leftToRight andThen that.leftToRight,
    that.rightToLeft andThen this.rightToLeft
  )

}

object Codec {

  def apply[T, U](implicit codec: Codec[T, U]): Codec[T, U] = codec

  def factory[T, U](tToU: T => U, uToT: U => T): Codec[T, U] = new Codec[T, U] {
    def leftToRight(left: T): U = tToU(left)
    def rightToLeft(right: U): T = uToT(right)
  }

  given composeCodecs [Left, Middle, Right] (using leftCodec: Codec[Left, Middle],
  rightCodec: Codec[Middle, Right]) as Codec[Left, Right] {
    val composition = leftCodec ++ rightCodec

    def leftToRight(left: Left): Right = composition.leftToRight(left)
    def rightToLeft(right: Right): Left = composition.rightToLeft(right)

  }

  given liftCodec [Left, Right] (using codec: Codec[Left, Right]) as Codec[List[Left], List[Right]] {
    def leftToRight(left: List[Left]): List[Right] = left.map(codec.leftToRight)
    def rightToLeft(right: List[Right]): List[Left] = right.map(codec.rightToLeft)
  }

  given zipCodec [T, U] as Codec [(List[T], List[U]), List[(T, U)]] {
    def leftToRight(left: (List[T], List[U])): List[(T, U)] = left._1 zip left._2
    def rightToLeft(right: List[(T, U)]): (List[T], List[U]) = right.unzip
  }

}
