package urldsl.language

import org.scalacheck._
import org.scalacheck.Prop._
import urldsl.language.simpleErrorImpl._
import urldsl.vocabulary.{MaybeFragment, Param, Segment}
import urldsl.errors.SimpleParamMatchingError

object FilteringProperties extends Properties("Filtering") {

  def predicate(x: Int): Boolean = x % 2 == 0

  property("Filtering segment on even integers") = forAll(Gen.posNum[Int]) { (number: Int) =>
    segment[Int]
      .filter(predicate, _ => pathError.unit)
      .matchSegments(List(Segment(number.toString)))
      .isRight == predicate(number)
  }

  property("Filtering query param on event integers") = forAll(Gen.posNum[Int]) { (number: Int) =>
    param[Int]("num")
      .filter(predicate, x => SimpleParamMatchingError.FromThrowable(new IllegalArgumentException(s"$x is not even!")))
      .matchParams(Map("num" -> Param(List(number.toString))))
      .isRight == predicate(number)
  }

  property("Filtering fragment on even integers") = forAll(Gen.posNum[Int]) { (number: Int) =>
    fragment[Int].filter(predicate, _ => ()).matchFragment(MaybeFragment(Some(number.toString))).isRight == predicate(
      number
    )
  }

}
