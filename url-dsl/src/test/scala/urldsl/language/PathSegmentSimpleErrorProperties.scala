package urldsl.language

import urldsl.errors.DummyError.dummyErrorIsPathMatchingError
import urldsl.errors.SimplePathMatchingError

final class PathSegmentSimpleErrorProperties
    extends PathSegmentProperties(
      simpleErrorImpl,
      SimplePathMatchingError.pathMatchingError,
      "PathSegmentSimpleError"
    ) {}
