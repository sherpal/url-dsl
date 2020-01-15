package urldsl.errors

import urldsl.vocabulary.Segment

sealed trait SimplePathMatchingError

object SimplePathMatchingError {

  final case class MalformedInt(str: String) extends SimplePathMatchingError
  final case class EndOfSegmentRequired(remainingSegments: Seq[Segment]) extends SimplePathMatchingError
  final case class WrongValue(expected: String, received: String) extends SimplePathMatchingError
  final case object MissingSegment extends SimplePathMatchingError
  final case class SimpleError(reason: String) extends SimplePathMatchingError

  implicit lazy val pathMatchingError: PathMatchingError[SimplePathMatchingError] =
    new PathMatchingError[SimplePathMatchingError] {
      def malformed(str: String): SimplePathMatchingError = MalformedInt(str)

      def endOfSegmentRequired(remainingSegments: List[Segment]): SimplePathMatchingError =
        EndOfSegmentRequired(remainingSegments)

      def wrongValue(expected: String, actual: String): SimplePathMatchingError = WrongValue(expected, actual)

      def missingSegment: SimplePathMatchingError = MissingSegment
    }

  implicit lazy val errorFromThrowable: ErrorFromThrowable[SimplePathMatchingError] = (throwable: Throwable) =>
    SimpleError(throwable.getMessage)

}
