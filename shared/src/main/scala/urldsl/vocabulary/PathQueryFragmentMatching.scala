package urldsl.vocabulary

final case class PathQueryFragmentMatching[P, Q, F](path: P, query: Q, fragment: F) {

  def extractInfo[P1, Q1](
      implicit ev1: P =:= PathMatchOutput[P1],
      ev2: Q =:= ParamMatchOutput[Q1]
  ): PathQueryFragmentMatching[P1, Q1, F] =
    PathQueryFragmentMatching(ev1(path).output, ev2(query).output, fragment)

}
