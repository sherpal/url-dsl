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

final class PathQueryFragmentRepr[PathType, +PathError, ParamsType, +ParamsError, FragmentType, +FragmentError] private[language] (
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
    PathQueryFragmentMatching[PathMatchOutput[PathType], ParamMatchOutput[ParamsType], FragmentType]
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
    matchUrl(parser.segments, parser.params, parser.maybeFragmentObj).map(_.extractInfo)
  }

  def createPart(
      info: PathQueryFragmentMatching[PathType, ParamsType, FragmentType],
      encoder: UrlStringGenerator
  ): String = {
    val PathQueryFragmentMatching(path, query, fragmentInfo) = info

    val queryPart = queryParams.createPart(query, encoder) match {
      case ""    => ""
      case other => "?" ++ other
    }

    pathSegment.createPart(path, encoder) ++ queryPart ++ fragment.createPart(fragmentInfo, encoder)
  }

  /** If this instance actually only bear path information, retrieves that information only. */
  def pathOnly(
      implicit ev1: Unit =:= ParamsType,
      ev2: Unit =:= FragmentType
  ): UrlPart[PathType, PathQueryFragmentError[PathError, ParamsError, FragmentError]] =
    UrlPart.factory(
      (str, urlParserGenerator) => matchRawUrl(str, urlParserGenerator).map(_.path),
      (p, encoder) => createPart(PathQueryFragmentMatching(p, ev1(()), ev2(())), encoder)
    )

  /** If this instance actually only bear query information, retrieves that information only. */
  def queryOnly(
      implicit ev1: Unit =:= PathType,
      ev2: Unit =:= FragmentType
  ): UrlPart[ParamsType, PathQueryFragmentError[PathError, ParamsError, FragmentError]] =
    UrlPart.factory(
      (str, urlParserGenerator) => matchRawUrl(str, urlParserGenerator).map(_.query),
      (q, encoder) => createPart(PathQueryFragmentMatching(ev1(()), q, ev2(())), encoder)
    )

  /** If this instance actually only bear fragment information, retrieves that information only. */
  def fragmentOnly(
      implicit ev1: Unit =:= ParamsType,
      ev2: Unit =:= PathType
  ): UrlPart[FragmentType, PathQueryFragmentError[PathError, ParamsError, FragmentError]] =
    UrlPart.factory(
      (str, urlParserGenerator) => matchRawUrl(str, urlParserGenerator).map(_.fragment),
      (f, encoder) => createPart(PathQueryFragmentMatching(ev2(()), ev1(()), f), encoder)
    )

}
