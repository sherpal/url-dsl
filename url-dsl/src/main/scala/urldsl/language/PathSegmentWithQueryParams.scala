package urldsl.language

import app.tulz.tuplez.Composition
import urldsl.url.{UrlStringDecoder, UrlStringGenerator, UrlStringParserGenerator}
import urldsl.vocabulary._

final class PathSegmentWithQueryParams[PathType, +PathError, ParamsType, +ParamsError] private[language] (
    pathSegment: PathSegment[PathType, PathError],
    queryParams: QueryParameters[ParamsType, ParamsError]
) extends UrlPart[UrlMatching[PathType, ParamsType], Either[PathError, ParamsError]] {

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

  def createUrl(query: ParamsType)(implicit ev: Unit =:= PathType): (List[Segment], Map[String, Param]) =
    createUrl(ev(()), query)

  def createUrlString(
      path: PathType,
      params: ParamsType,
      generator: UrlStringGenerator = UrlStringGenerator.default
  ): String =
    pathSegment.createPath(path, generator) ++ "?" ++ queryParams.createParamsString(params, generator)

  def &[OtherParamsType, ParamsError1 >: ParamsError](otherParams: QueryParameters[OtherParamsType, ParamsError1])(
      implicit c: Composition[ParamsType, OtherParamsType]
  ): PathSegmentWithQueryParams[PathType, PathError, c.Composed, ParamsError1] =
    new PathSegmentWithQueryParams[PathType, PathError, c.Composed, ParamsError1](
      pathSegment,
      (queryParams & otherParams)
        .asInstanceOf[QueryParameters[c.Composed, ParamsError1]] // not necessary but IntelliJ complains.
    )

  def withFragment[FragmentType, FragmentError](
      fragment: Fragment[FragmentType, FragmentError]
  ): PathQueryFragmentRepr[PathType, PathError, ParamsType, ParamsError, FragmentType, FragmentError] =
    new PathQueryFragmentRepr(pathSegment, queryParams, fragment)

  def createPart(t: UrlMatching[PathType, ParamsType], encoder: UrlStringGenerator): String =
    createUrlString(t.path, t.params, encoder)
}
