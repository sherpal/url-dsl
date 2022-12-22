package urldsl.language

import org.scalacheck.{Gen, Prop}
import org.scalacheck.Prop.forAll
import urldsl.errors.{DummyError, ErrorFromThrowable, FragmentMatchingError}
import urldsl.vocabulary.{Codec, MaybeFragment}

final class FragmentDummyErrorProperties
    extends FragmentProperties[DummyError](
      Fragment.dummyErrorImpl,
      DummyError.dummyErrorIsFragmentMatchingError,
      "FragmentDummyErrorProperties"
    ) {

  property("Filtering non positive ints with dummy error sugar") = forAll(Gen.option(Gen.choose(-1000, 1000))) {
    (maybeX: Option[Int]) =>
      case class PosInt(value: Int)

      implicit def codec: Codec[Int, PosInt] = Codec.factory(PosInt.apply, _.value)

      val predicate: Int => Boolean = _ > 0

      Prop(
        intFragment
          .filter(predicate)
          .as[PosInt]
          .?
          .matchFragment(MaybeFragment(maybeX.map(_.toString))) == Right(maybeX.filter(predicate).map(PosInt.apply))
      ) && Prop(
        intFragment.?.createFragment(maybeX) == MaybeFragment(maybeX.map(_.toString))
      )
  }

}
