package urldsl.language

import urldsl.errors.{DummyError, ParamMatchingError, SimpleParamMatchingError}
import urldsl.url.UrlStringGenerator
import urldsl.vocabulary.{Codec, FromString, Param, ParamMatchOutput, Printer}

trait QueryParameters[Q, A] {

  import QueryParameters._

  /**
    * Tries to match the map of [[urldsl.vocabulary.Param]]s to create an instance of `Q`.
    * If it can not, it returns an error indicating the reason of the failure.
    * If it could, it returns the value of `Q`, as well as the list of unused parameters.
    *
    * @example
    *           For example, if you try to match a param "name" as String and "age" as Int, calling matchParams on
    *           Map("name" -> Param(List("Alice")), "age" -> Param(List("24"), "year" -> Param(List("2020")))
    *           will return
    *           Right(ParamMatchOutput(("Alice", 24), Map("year" -> Param(List("2020")))
    *
    * @param params The map of [[urldsl.vocabulary.Param]] to match this path segment again.
    * @return The "de-serialized" element with unused parameters, if successful.
    */
  def matchParams(params: Map[String, Param]): Either[A, ParamMatchOutput[Q]]

  /**
    * Generate a map of parameters representing the argument `q`.
    *
    * `matchParams` and `createParams` should be (functional) inverse of each other. That is,
    * `this.matchParams(this.createParams(q)) == Right(ParamMathOutput(q, Map()))` (this property is called
    * "LeftInverse" in the tests)
    */
  def createParams(q: Q): Map[String, Param]

  final def createParamsString[A <: UrlStringGenerator](q: Q)(implicit generator: A): String =
    generator.makeParams(createParams(q))

  /**
    * Adds `that` QueryParameters to `this` one, "tupling" the returned type with the implicit [[urldsl.language.Tupler]]
    *
    * The matching and writing of strings is functionally commutative under `&`, but the returned type `Q` is not. So,
    * if you have two parameters, one matching an Int and the other one a String, depending on the order in which `&` is
    * called, you can end up with "Q = (Int, String)" or "Q = (String, Int)". This property is called
    * "QuasiCommutativity" in the tests.
    */
  final def &[R](that: QueryParameters[R, A])(implicit ev: Tupler[Q, R]): QueryParameters[ev.Out, A] =
    factory[ev.Out, A](
      (params: Map[String, Param]) =>
        for {
          firstMatch <- this.matchParams(params)
          ParamMatchOutput(q, remainingParams) = firstMatch
          secondMatch <- that.matchParams(remainingParams)
          ParamMatchOutput(r, finalRemainingParams) = secondMatch
        } yield ParamMatchOutput(ev(q, r), finalRemainingParams),
      (out: ev.Out) => {
        val (q, r) = ev.unapply(out)
        this.createParams(q) ++ that.createParams(r)
      }
    )

  /**
    * When these query parameters return an error, transform it to None instead.
    *
    * This should be used to represent (possibly) missing parameters.
    */
  final def ? : QueryParameters[Option[Q], A] = factory[Option[Q], A](
    (params: Map[String, Param]) =>
      matchParams(params) match {
        case Right(ParamMatchOutput(output, unused)) => Right(ParamMatchOutput(Some(output), unused))
        case Left(_)                                 => Right(ParamMatchOutput(None, params))
      },
    _.map(createParams).getOrElse(Map())
  )

  final def filter(predicate: Q => Boolean, error: Map[String, Param] => A): QueryParameters[Q, A] = factory(
    (params: Map[String, Param]) =>
      matchParams(params).filterOrElse(((_: ParamMatchOutput[Q]).output).andThen(predicate), error(params)),
    createParams
  )

  /**
    * Casts this [[QueryParameters]] to the new type R. Note that the [[urldsl.vocabulary.Codec]] must be an
    * exception-free bijection between Q and R.
    */
  final def as[R](implicit codec: Codec[Q, R]): QueryParameters[R, A] = factory(
    (matchParams _).andThen(_.map(_.map(codec.leftToRight))),
    (codec.rightToLeft _).andThen(createParams)
  )

}

object QueryParameters {

  def factory[Q, A](
      matching: Map[String, Param] => Either[A, ParamMatchOutput[Q]],
      creating: Q => Map[String, Param]
  ): QueryParameters[Q, A] = new QueryParameters[Q, A] {
    def matchParams(params: Map[String, Param]): Either[A, ParamMatchOutput[Q]] = matching(params)
    def createParams(q: Q): Map[String, Param] = creating(q)
  }

  final def simpleQueryParam[Q, A](
      paramName: String,
      matching: Param => Either[A, Q],
      creating: Q => Param
  )(implicit paramMatchingError: ParamMatchingError[A]): QueryParameters[Q, A] = factory[Q, A](
    (params: Map[String, Param]) =>
      params
        .get(paramName)
        .map(matching)
        .map(_.map(ParamMatchOutput(_, params - paramName))) // consumes that param
        .getOrElse(Left(paramMatchingError.missingParameterError(paramName))),
    creating.andThen(paramName -> _).andThen(Map(_))
  )

  final def param[Q, A](
      paramName: String
  )(
      implicit fromString: FromString[Q, A],
      printer: Printer[Q],
      paramMatchingError: ParamMatchingError[A]
  ): QueryParameters[Q, A] =
    simpleQueryParam[Q, A](
      paramName,
      (_: Param) match {
        case Param(Nil)       => Left(paramMatchingError.missingParameterError(paramName))
        case Param(head :: _) => fromString(head)
      },
      q => Param(List(printer(q)))
    )

  final def listParam[Q, A](
      paramName: String
  )(
      implicit fromString: FromString[Q, A],
      printer: Printer[Q],
      paramMatchingError: ParamMatchingError[A]
  ): QueryParameters[List[Q], A] =
    simpleQueryParam[List[Q], A](
      paramName,
      (_: Param) match {
        case Param(Nil) => Right(List[Q]())
        case Param(head :: tail) =>
          tail
            .map(fromString.apply)
            .foldLeft(fromString(head).map(List(_))) { (acc, next) =>
              for {
                firstResults <- acc
                nextResult <- next
              } yield nextResult +: firstResults
            }
            .map(_.reverse)
      },
      q => Param(q.map(printer.apply))
    )

  final lazy val dummyErrorImpl = QueryParametersImpl[DummyError]
  final lazy val simpleParamErrorImpl = QueryParametersImpl[SimpleParamMatchingError]

}
