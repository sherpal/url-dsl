package urldsl.vocabulary

final case class ParamMatchOutput[Q](output: Q, unusedParams: Map[String, Param]) {
  def map[R](f: Q => R): ParamMatchOutput[R] = ParamMatchOutput(f(output), unusedParams)
}
