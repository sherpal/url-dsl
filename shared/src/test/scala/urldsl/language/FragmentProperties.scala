package urldsl.language

import org.scalacheck._
import urldsl.errors.DummyError
import urldsl.errors.DummyError.{dummyErrorIsPathMatchingError => e}
import urldsl.vocabulary.{PathMatchOutput, Segment}

final class FragmentProperties {}
