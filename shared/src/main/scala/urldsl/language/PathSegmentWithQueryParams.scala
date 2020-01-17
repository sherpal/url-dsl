package urldsl.language

import urldsl.url.UrlStringGenerator
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

  def createUrl(path: PathType, params: ParamsType): (List[Segment], Map[String, Param]) =
    (pathSegment.createSegments(path), queryParams.createParams(params))

  def createUrl(path: PathType)(implicit ev: Unit =:= ParamsType): (List[Segment], Map[String, Param]) =
    createUrl(path, ev(()))

  def createUrlString[Generator <: UrlStringGenerator](path: PathType, params: ParamsType)(
      implicit generator: Generator
  ): String =
    pathSegment.createPath(path) + "?" + queryParams.createParamsString(params)

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
