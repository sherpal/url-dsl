package urldsl.vocabulary

final case class PathQueryFragmentMatching[P, Q, F](path: PathMatchOutput[P], query: ParamMatchOutput[Q], fragment: F)
