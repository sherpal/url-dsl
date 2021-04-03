package urldsl.language

import org.scalacheck._
import org.scalacheck.Prop._
import urldsl.errors.SimpleFragmentMatchingError
import urldsl.vocabulary.MaybeFragment

import scala.util.Try
import urldsl.language.Fragment.simpleFragmentErrorImpl._

//noinspection TypeAnnotation
final class FragmentProperties extends Properties("Fragment") {

  val fragmentGen: Gen[MaybeFragment] = Gen.option(Gen.asciiStr).map(MaybeFragment.apply)
  val intFragmentGen: Gen[MaybeFragment] = Gen.chooseNum(-1000, 1000).map(_.toString).map(Some(_)).map(MaybeFragment.apply)

  val nonIntFragmentGen: Gen[MaybeFragment] = fragmentGen.filter {
    case MaybeFragment(Some(value)) => Try(value.toInt).isFailure
    case _                          => true
  }

  val stringFragment = fragment[String]
  val maybeStringFragment = maybeFragment[String]
  val intFragment = fragment[Int]
  val maybeIntFragment = maybeFragment[Int]

  property("fragment gen sometimes generate empty fragment") = exists(fragmentGen)(_.isEmpty)

  property("Maybe Matching existing string always works") = forAll(fragmentGen) { fragment =>
    maybeStringFragment.matchFragment(fragment).isRight
  }

  property("Matching existing string works when fragment is non empty") = forAll(fragmentGen) { fragment =>
    (stringFragment.matchFragment(fragment), fragment) match {
      case (Left(value), MaybeFragment(None)) => Prop(value == SimpleFragmentMatchingError.MissingFragmentError)
      case (Left(error), MaybeFragment(Some(value))) =>
        Prop(false) :| s"Fragment contained value $value but I got the error $error"
      case (Right(value), MaybeFragment(None)) =>
        Prop(false) :| s"Fragment was empty and yet I extracted the value $value"
      case (Right(extractedValue), MaybeFragment(Some(value))) =>
        Prop(extractedValue == value) :| s"Fragment extracted the value $extractedValue but was supposed to get $value."
    }
  }

  property("Empty matching matches when fragment is empty") = forAll(fragmentGen) { fragment =>
    emptyFragment.matchFragment(fragment).isRight == fragment.isEmpty
  }

  property("Matching an integer works") = forAll(intFragmentGen) { fragment =>
    intFragment.matchFragment(fragment).map(_.toString).map(Some(_)) == Right(fragment.value)
  }

  property("Int matching always fail when it's not an int") = forAll(nonIntFragmentGen) { fragment =>
    intFragment.matchFragment(fragment) match {
      case Right(value) => Prop(false) :| s"This was supposed to not match but I got $value"
      case Left(error) =>
        Prop(fragment.value match {
          case Some(_) => error.isInstanceOf[SimpleFragmentMatchingError.FromThrowable]
          case None    => error == SimpleFragmentMatchingError.MissingFragmentError
        }) :| s"Correctly found out that it was an error but error was wrong, got: $error"

    }
  }

  property("Mapping string results to its length works") = forAll(fragmentGen) { fragment =>
    val stringLengthFragment = stringFragment.as((_: String).length, (_: Int).toString)

    Prop(fragment.nonEmpty) ==> Prop(
      stringLengthFragment.matchFragment(fragment) == Right(fragment.value.get.length)
    )
  }

  property("Matching is the left inverse for generating in string fragment") = forAll(Gen.asciiStr) {
    (fragment: String) =>
      stringFragment.matchFragment(stringFragment.createFragment(fragment)) == Right(fragment)
  }

  property("Matching is the left inverse for generating in int fragment") = forAll(Gen.chooseNum(-1000, 1000)) {
    (fragment: Int) =>
      intFragment.matchFragment(intFragment.createFragment(fragment)) == Right(fragment)
  }

}
