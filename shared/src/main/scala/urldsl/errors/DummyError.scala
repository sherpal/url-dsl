package urldsl.errors
import urldsl.vocabulary.Segment

import scala.language.`3.1`

/**
  * Error type with only one instance, for when you only care about knowing whether the error exists.
  */
sealed trait DummyError

object DummyError {

  final val dummyError: DummyError = new DummyError {}

  given ParamMatchingError [DummyError] {
    def missingParameterError(paramName: String): DummyError = dummyError

    def fromThrowable(throwable: Throwable): DummyError = dummyError
  }

  given PathMatchingError [DummyError] {
    def malformed(str: String): DummyError = dummyError

    def endOfSegmentRequired(remainingSegments: List[Segment]): DummyError = dummyError

    def wrongValue(expected: String, actual: String): DummyError = dummyError

    def missingSegment: DummyError = dummyError

    def fromThrowable(throwable: Throwable): DummyError = dummyError

    def unit: DummyError = dummyError
  }

  given ErrorFromThrowable [DummyError] {
    def fromThrowable(t: Throwable): DummyError = new DummyError {}
  }

}
