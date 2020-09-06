package urldsl.errors

import scala.language.`3.1`

import urldsl.vocabulary.Segment

sealed trait SimplePathMatchingError

object SimplePathMatchingError {

  case class MalformedInt(str: String) extends SimplePathMatchingError
  case class EndOfSegmentRequired(remainingSegments: Seq[Segment]) extends SimplePathMatchingError
  case class WrongValue(expected: String, received: String) extends SimplePathMatchingError
  case object MissingSegment extends SimplePathMatchingError
  case class SimpleError(reason: String) extends SimplePathMatchingError
  case object AlwaysFalse extends SimplePathMatchingError

  given PathMatchingError [SimplePathMatchingError] {
    def malformed(str: String): SimplePathMatchingError = MalformedInt(str)

    def endOfSegmentRequired(remainingSegments: List[Segment]): SimplePathMatchingError =
      EndOfSegmentRequired(remainingSegments)

    def wrongValue(expected: String, actual: String): SimplePathMatchingError = WrongValue(expected, actual)

    def missingSegment: SimplePathMatchingError = MissingSegment

    def unit: SimplePathMatchingError = AlwaysFalse
  }

  given ErrorFromThrowable [SimplePathMatchingError] {
    def fromThrowable(throwable: Throwable): SimpleError = SimpleError(throwable.getMessage)
  }

}
