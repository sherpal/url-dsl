package urldsl.vocabulary

final case class UrlMatching[P, Q](path: P, params: Q)
