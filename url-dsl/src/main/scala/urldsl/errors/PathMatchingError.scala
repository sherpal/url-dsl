package urldsl.errors

import urldsl.vocabulary.Segment

/** You can implement this trait for your own error type `A`, a giving an implicit instance in the companion object of
  * `A` so that it is available for all pre-defined [[urldsl.language.PathSegment]].
  *
  * @example
  *   See implementations of [[DummyError]] or [[SimpleParamMatchingError]]
  *
  * @tparam A
  *   type of the error.
  */
trait PathMatchingError[+A] {

  def malformed(str: => String): A
  def endOfSegmentRequired(remainingSegments: => List[Segment]): A
  def wrongValue(expected: => String, actual: => String): A
  def missingSegment: A
  def unit: A

}
