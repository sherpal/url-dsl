package urldsl.vocabulary

import urldsl.language.PathSegment

/**
  * Returned type of matching segments against a [[PathSegment]].
  *
  * This is used to avoid returning an ugly tuple.
  *
  * @param output the de-serialized element from the matching
  * @param unusedSegments the segments that were not used to generate the output
  */
final case class PathMatchOutput[T](output: T, unusedSegments: List[Segment]) {
  def map[U](f: T => U): PathMatchOutput[U] = PathMatchOutput(f(output), unusedSegments)
}
