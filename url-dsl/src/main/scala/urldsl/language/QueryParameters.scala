package urldsl.language

import app.tulz.tuplez.Composition
import urldsl.errors.{DummyError, ParamMatchingError, SimpleParamMatchingError}
import urldsl.url.{UrlStringDecoder, UrlStringGenerator, UrlStringParserGenerator}
import urldsl.vocabulary._

trait QueryParameters[Q, +A] extends UrlPart[Q, A] {

  import QueryParameters._

  /** Tries to match the map of [[urldsl.vocabulary.Param]]s to create an instance of `Q`. If it can not, it returns an
    * error indicating the reason of the failure. If it could, it returns the value of `Q`, as well as the list of
    * unused parameters.
    *
    * @example
    *   For example, if you try to match a param "name" as String and "age" as Int, calling matchParams on Map("name" ->
    *   Param(List("Alice")), "age" -> Param(List("24"), "year" -> Param(List("2020"))) will return
    *   Right(ParamMatchOutput(("Alice", 24), Map("year" -> Param(List("2020")))
    *
    * @param params
    *   The map of [[urldsl.vocabulary.Param]] to match this path segment again.
    * @return
    *   The "de-serialized" element with unused parameters, if successful.
    */
  def matchParams(params: Map[String, Param]): Either[A, ParamMatchOutput[Q]]

  def matchRawUrl(
      url: String,
      urlStringParserGenerator: UrlStringParserGenerator = UrlStringParserGenerator.defaultUrlStringParserGenerator
  ): Either[A, Q] =
    matchParams(urlStringParserGenerator.parser(url).params).map(_.output)

  def matchQueryString(queryString: String, decoder: UrlStringDecoder = UrlStringDecoder.defaultDecoder): Either[A, Q] =
    matchParams(decoder.decodeParams(queryString)).map(_.output)

  /** Generate a map of parameters representing the argument `q`.
    *
    * `matchParams` and `createParams` should be (functional) inverse of each other. That is,
    * `this.matchParams(this.createParams(q)) == Right(ParamMathOutput(q, Map()))` (this property is called
    * "LeftInverse" in the tests)
    */
  def createParams(q: Q): Map[String, Param]

  /** Generates a Map of parameters representing the argument `q`. The keys are not encoded, but the values are lists of
    * encoded strings.
    */
  final def createParamsMap(q: Q, encoder: UrlStringGenerator = UrlStringGenerator.default): Map[String, List[String]] =
    encoder.makeParamsMap(createParams(q))

  /** Generates the query string representing the argument `q`. This String can be used to be part of a URL.
    */
  final def createParamsString(q: Q, encoder: UrlStringGenerator = UrlStringGenerator.default): String =
    encoder.makeParams(createParams(q))

  final def createPart(q: Q, encoder: UrlStringGenerator = UrlStringGenerator.default): String =
    createParamsString(q, encoder)

  /** Adds `that` QueryParameters to `this` one, "tupling" the returned type with the implicit [[Composition]]
    *
    * The matching and writing of strings is functionally commutative under `&`, but the returned type `Q` is not. So,
    * if you have two parameters, one matching an Int and the other one a String, depending on the order in which `&` is
    * called, you can end up with "Q = (Int, String)" or "Q = (String, Int)". This property is called
    * "QuasiCommutativity" in the tests.
    */
  final def &[R, A1 >: A](that: QueryParameters[R, A1])(implicit
      c: Composition[Q, R]
  ): QueryParameters[c.Composed, A1] =
    factory[c.Composed, A1](
      (params: Map[String, Param]) =>
        for {
          firstMatch <- this.matchParams(params)
          ParamMatchOutput(q, remainingParams) = firstMatch
          secondMatch <- that.matchParams(remainingParams)
          ParamMatchOutput(r, finalRemainingParams) = secondMatch
        } yield ParamMatchOutput(c.compose(q, r), finalRemainingParams),
      (out: c.Composed) => {
        val (q, r) = c.decompose(out)
        this.createParams(q) ++ that.createParams(r)
      }
    )

  /** When these query parameters return an error, transform it to None instead.
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

  /** Adds an extra satisfying criteria to the output of this [[QueryParameters]]. If the output satisfies the given
    * `predicate`, then it is left unchanged. Otherwise, it returns the given `error`.
    *
    * Note that it doesn't check that arguments given to `createParams` satisfy this predicate // todo[behaviour]:
    * should that change?
    *
    * @example
    *   {{{param[Int]("age").filter(_ >= 0, (params: Map[String, Param]) => someError(params))}}}
    *
    * @param predicate
    *   the additional predicate that the output must satisfy
    * @param error
    *   the generated error in case it does not satisfy it
    * @return
    *   a new [[QueryParameters]] instance with the same types
    */
  final def filter[A1 >: A](predicate: Q => Boolean, error: Map[String, Param] => A1): QueryParameters[Q, A1] = factory(
    (params: Map[String, Param]) =>
      matchParams(params).filterOrElse(((_: ParamMatchOutput[Q]).output).andThen(predicate), error(params)),
    createParams
  )

  /** Sugar for when `A =:= DummyError`. */
  final def filter(predicate: Q => Boolean)(implicit ev: A <:< DummyError): QueryParameters[Q, DummyError] = {
    // type F[+E] = QueryParameters[Q, E]
    // ev.liftCo[F].apply(this).filter(predicate, _ => DummyError.dummyError)
    // we keep this ugliness below while supportinig scala 2.12 todo[scala3] remove
    this.asInstanceOf[QueryParameters[Q, DummyError]].filter(predicate, _ => DummyError.dummyError)
  }

  /** Casts this [[QueryParameters]] to the new type R. Note that the [[urldsl.vocabulary.Codec]] must be an
    * exception-free bijection between Q and R.
    */
  final def as[R](implicit codec: Codec[Q, R]): QueryParameters[R, A] = factory(
    (matchParams _).andThen(_.map(_.map(codec.leftToRight))),
    (codec.rightToLeft _).andThen(createParams)
  )

  /** Associates this [[QueryParameters]] with the given [[Fragment]] in order to match raw urls satisfying both
    * conditions, and returning the outputs from both.
    *
    * The path part of the url will be *ignored* (and will return Unit).
    */
  final def withFragment[FragmentType, FragmentError](
      fragment: Fragment[FragmentType, FragmentError]
  ): PathQueryFragmentRepr[Unit, Nothing, Q, A, FragmentType, FragmentError] =
    new PathQueryFragmentRepr(PathSegment.root, this, fragment)

}

object QueryParameters {

  def factory[Q, A](
      matching: Map[String, Param] => Either[A, ParamMatchOutput[Q]],
      creating: Q => Map[String, Param]
  ): QueryParameters[Q, A] = new QueryParameters[Q, A] {
    def matchParams(params: Map[String, Param]): Either[A, ParamMatchOutput[Q]] = matching(params)
    def createParams(q: Q): Map[String, Param] = creating(q)
  }

  final def empty: QueryParameters[Unit, Nothing] = factory[Unit, Nothing](
    (params: Map[String, Param]) => Right(ParamMatchOutput((), params)),
    _ => Map()
  )

  /** Alias for empty which seems to better reflect the semantic. */
  final def ignore: QueryParameters[Unit, Nothing] = empty

  final def simpleQueryParam[Q, A](
      paramName: String,
      matching: Param => Either[A, Q],
      creating: Q => Param,
      onParameterNotFound: Map[String, Param] => Either[A, ParamMatchOutput[Q]]
  )(implicit paramMatchingError: ParamMatchingError[A]): QueryParameters[Q, A] = factory[Q, A](
    (params: Map[String, Param]) =>
      params
        .get(paramName)
        .map(matching)
        .map(_.map(ParamMatchOutput(_, params - paramName))) // consumes that param
        .getOrElse(onParameterNotFound(params)),
    creating.andThen(paramName -> _).andThen(Map(_))
  )

  final def simpleQueryParam[Q, A](
      paramName: String,
      matching: Param => Either[A, Q],
      creating: Q => Param
  )(implicit paramMatchingError: ParamMatchingError[A]): QueryParameters[Q, A] =
    simpleQueryParam(
      paramName,
      matching,
      creating,
      onParameterNotFound = _ => Left(paramMatchingError.missingParameterError(paramName))
    )

  final def param[Q, A](
      paramName: String
  )(implicit
      fromString: FromString[Q, A],
      printer: Printer[Q],
      paramMatchingError: ParamMatchingError[A]
  ): QueryParameters[Q, A] =
    simpleQueryParam[Q, A](
      paramName,
      (_: Param) match {
        case Param(Nil)       => Left(paramMatchingError.missingParameterError(paramName))
        case Param(head :: _) => fromString(head)
      },
      (q: Q) => Param(List(printer(q)))
    )

  final def listParam[Q, A](
      paramName: String
  )(implicit
      fromString: FromString[Q, A],
      printer: Printer[Q],
      paramMatchingError: ParamMatchingError[A]
  ): QueryParameters[List[Q], A] =
    simpleQueryParam[List[Q], A](
      paramName,
      (_: Param) match {
        case Param(Nil) => Right(Nil)
        case Param(head :: tail) =>
          tail
            .map(fromString.apply)
            .foldLeft(fromString(head).map(_ :: Nil)) { (acc, next) =>
              for {
                firstResults <- acc
                nextResult <- next
              } yield nextResult +: firstResults
            }
            .map(_.reverse)
      },
      (q: List[Q]) => Param(q.map(printer.apply)),
      // If `paramName` is not present in the parameters we should return an empty list.
      onParameterNotFound = (params: Map[String, Param]) => Right(ParamMatchOutput(List.empty[Q], params))
    )

  final lazy val dummyErrorImpl = QueryParametersImpl[DummyError]
  final lazy val simpleParamErrorImpl = QueryParametersImpl[SimpleParamMatchingError]

}
