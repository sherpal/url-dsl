package urldsl.language

import urldsl.errors.PathMatchingError
import urldsl.vocabulary.{FromString, Printer, Segment}

import scala.language.implicitConversions

/** Using the pre-defined path segments in [[PathSegment]] can be cumbersome if you have to constantly specify the error
  * type A, for example in the [[PathSegment.segment]] or [[PathSegment.root]] methods. Therefore, you can invoke an
  * implementation of this class and import its members instead. We don't redefine [[PathSegment.intSegment]] and
  * [[PathSegment.stringSegment]] since they can be easily and conveniently invoked using the `segment` method below.
  *
  * @example
  *   {{{
  *          val pathSegmentImpl = PathSegmentImpl[DummyError]
  *          import pathSegmentImpl._
  *
  *          root / "hello"  // this is of type PathSegment[Unit, DummyError] thanks to the import above.
  *           (note that in this case there is already a PathSegmentImpl for [[urldsl.errors.DummyError]] in the
  *           [[PathSegment]] companion object)
  *   }}}
  *
  * @tparam A
  *   type of error.
  */
trait PathSegmentImpl[A] {

  /** implementation of [[urldsl.errors.PathMatchingError]] for type A. */
  implicit protected val pathError: PathMatchingError[A]

  val root: PathSegment[Unit, A] = PathSegment.root
  val remainingSegments: PathSegment[List[String], A] = PathSegment.remainingSegments
  lazy val endOfSegments: PathSegment[Unit, A] = PathSegment.endOfSegments
  lazy val noMatch: PathSegment[Unit, A] = PathSegment.noMatch[A]

  def segment[T](implicit fromString: FromString[T, A], printer: Printer[T]): PathSegment[T, A] =
    PathSegment.segment[T, A]

  def oneOf[T](t: T, ts: T*)(implicit fromString: FromString[T, A], printer: Printer[T]): PathSegment[Unit, A] =
    PathSegment.oneOf(t, ts: _*)

  /* I think this should not be necessary but the compiler has difficulties finding the correct error due to covariance */
  implicit def unaryPathSegment[T](
      t: T
  )(implicit
      fromString: FromString[T, A],
      printer: Printer[T]
  ): PathSegment[Unit, A] =
    PathSegment.simplePathSegment(
      s =>
        fromString(s.content)
          .filterOrElse[A](_ == t, pathError.wrongValue(printer(t), s.content))
          .map(_ => ()),
      (_: Unit) => Segment(printer(t))
    )

}

object PathSegmentImpl {

  /** Invoker. */
  def apply[A](implicit error: PathMatchingError[A]): PathSegmentImpl[A] = new PathSegmentImpl[A] {
    implicit protected val pathError: PathMatchingError[A] = error
  }

}
