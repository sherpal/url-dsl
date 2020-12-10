package urldsl.language

import urldsl.url.{UrlStringGenerator, UrlStringParserGenerator}
import urldsl.vocabulary.{
  MaybeFragment,
  Param,
  ParamMatchOutput,
  PathMatchOutput,
  PathQueryFragmentError,
  PathQueryFragmentMatching,
  Segment
}

final class PathQueryFragmentRepr[PathType, +PathError, ParamsType, ParamsError, FragmentType, FragmentError] private[language] (
    pathSegment: PathSegment[PathType, PathError],
    queryParams: QueryParameters[ParamsType, ParamsError],
    fragment: Fragment[FragmentType, FragmentError]
) extends UrlPart[
      PathQueryFragmentMatching[PathType, ParamsType, FragmentType],
      PathQueryFragmentError[PathError, ParamsError, FragmentError]
    ] {

  private implicit class ErrorMappingEither[E, A](either: Either[E, A]) {
    def mapError[F](f: E => F): Either[F, A] = either match {
      case Left(value)  => Left(f(value))
      case Right(value) => Right(value)
    }
  }

  // note: should this rather be some kind of Validated instead of either?
  private[language] def matchUrl(path: List[Segment], params: Map[String, Param], maybeFragment: MaybeFragment): Either[
    PathQueryFragmentError[PathError, ParamsError, FragmentError],
    PathQueryFragmentMatching[PathType, ParamsType, FragmentType]
  ] =
    for {
      path <- pathSegment
        .matchSegments(path)
        .mapError[PathQueryFragmentError[PathError, ParamsError, FragmentError]](PathQueryFragmentError.PathError(_))
      query <- queryParams
        .matchParams(params)
        .mapError[PathQueryFragmentError[PathError, ParamsError, FragmentError]](PathQueryFragmentError.ParamsError(_))
      fragment <- fragment
        .matchFragment(maybeFragment)
        .mapError[PathQueryFragmentError[PathError, ParamsError, FragmentError]](
          PathQueryFragmentError.FragmentError(_)
        )
    } yield PathQueryFragmentMatching(path, query, fragment)

  def matchRawUrl(
      url: String,
      urlStringParserGenerator: UrlStringParserGenerator = UrlStringParserGenerator.defaultUrlStringParserGenerator
  ): Either[PathQueryFragmentError[PathError, ParamsError, FragmentError], PathQueryFragmentMatching[
    PathType,
    ParamsType,
    FragmentType
  ]] = {
    val parser = urlStringParserGenerator.parser(url)
    matchUrl(parser.segments, parser.params, parser.maybeFragmentObj)
  }

  def createPart(
      info: PathQueryFragmentMatching[PathType, ParamsType, FragmentType],
      encoder: UrlStringGenerator
  ): String = {
    val PathQueryFragmentMatching(PathMatchOutput(path, _), ParamMatchOutput(query, _), fragmentInfo) = info

    val queryPart = queryParams.createPart(query, encoder) match {
      case ""    => ""
      case other => "?" ++ other
    }

    pathSegment.createPart(path, encoder) ++ queryPart ++ fragment.createPart(fragmentInfo, encoder)
  }

}
