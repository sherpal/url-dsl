package urldsl.language

import urldsl.url.{UrlStringDecoder, UrlStringGenerator, UrlStringParserGenerator}
import urldsl.vocabulary._

final class PathSegmentWithQueryParams[PathType, PathError, ParamsType, ParamsError] private[language] (
    pathSegment: PathSegment[PathType, PathError],
    queryParams: QueryParameters[ParamsType, ParamsError]
) {

  def matchUrl(
      path: List[Segment],
      params: Map[String, Param]
  ): Either[Either[PathError, ParamsError], UrlMatching[PathType, ParamsType]] =
    pathSegment.matchSegments(path) match {
      case Left(error) => Left(Left(error))
      case Right(PathMatchOutput(pathOut, _)) =>
        queryParams.matchParams(params) match {
          case Left(error)                           => Left(Right(error))
          case Right(ParamMatchOutput(paramsOut, _)) => Right(UrlMatching(pathOut, paramsOut))
        }
    }

  def matchRawUrl(
      url: String,
      parserGenerator: UrlStringParserGenerator = UrlStringParserGenerator.defaultUrlStringParserGenerator
  ): Either[Either[PathError, ParamsError], UrlMatching[PathType, ParamsType]] =
    pathSegment.matchRawUrl(url, parserGenerator) match {
      case Left(error) => Left(Left(error))
      case Right(pathOutput) =>
        queryParams.matchRawUrl(url, parserGenerator) match {
          case Left(error)         => Left(Right(error))
          case Right(paramsOutput) => Right(UrlMatching(pathOutput, paramsOutput))
        }
    }

  def matchRawUrlOption(
      url: String,
      parserGenerator: UrlStringParserGenerator = UrlStringParserGenerator.defaultUrlStringParserGenerator
  ): Option[UrlMatching[PathType, ParamsType]] = matchRawUrl(url, parserGenerator).toOption

  def matchPathAndQuery(
      path: String,
      queryString: String,
      decoder: UrlStringDecoder = UrlStringDecoder.defaultDecoder
  ): Either[Either[PathError, ParamsError], UrlMatching[PathType, ParamsType]] =
    pathSegment.matchPath(path, decoder) match {
      case Left(error) => Left(Left(error))
      case Right(pathOutput) =>
        queryParams.matchQueryString(queryString, decoder) match {
          case Left(error)         => Left(Right(error))
          case Right(paramsOutput) => Right(UrlMatching(pathOutput, paramsOutput))
        }
    }

  def matchPathAndQueryOption(
      path: String,
      queryString: String,
      decoder: UrlStringDecoder = UrlStringDecoder.defaultDecoder
  ): Option[UrlMatching[PathType, ParamsType]] = matchPathAndQuery(path, queryString, decoder).toOption

  def createUrl(path: PathType, params: ParamsType): (List[Segment], Map[String, Param]) =
    (pathSegment.createSegments(path), queryParams.createParams(params))

  def createUrl(path: PathType)(implicit ev: Unit =:= ParamsType): (List[Segment], Map[String, Param]) =
    createUrl(path, ev(()))

  def createUrlString(
      path: PathType,
      params: ParamsType,
      generator: UrlStringGenerator = UrlStringGenerator.default
  ): String =
    pathSegment.createPath(path, generator) ++ "?" ++ queryParams.createParamsString(params, generator)

  def &[OtherParamsType](otherParams: QueryParameters[OtherParamsType, ParamsError])(
      implicit
      ev: Tupler[ParamsType, OtherParamsType]
  ): PathSegmentWithQueryParams[PathType, PathError, ev.Out, ParamsError] =
    new PathSegmentWithQueryParams[PathType, PathError, ev.Out, ParamsError](
      pathSegment,
      (queryParams & otherParams)
        .asInstanceOf[QueryParameters[ev.Out, ParamsError]] // not necessary but IntelliJ complains.
    )

}
