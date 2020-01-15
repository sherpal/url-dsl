package urldsl.vocabulary

/**
  * Represents a bijection between `Left` and `Right`.
  */
trait Codec[Left, Right] {

  def leftToRight(left: Left): Right
  def rightToLeft(right: Right): Left

}

object Codec {

  def apply[T, U](implicit codec: Codec[T, U]): Codec[T, U] = codec

  def factory[T, U](tToU: T => U, uToT: U => T): Codec[T, U] = new Codec[T, U] {
    def leftToRight(left: T): U = tToU(left)
    def rightToLeft(right: U): T = uToT(right)
  }

}
