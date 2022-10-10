package urldsl.vocabulary

/**
  * Wrapper around the raw fragment part of the URL.
  *
  * None when the URL does not contain any fragment.
  */
final case class MaybeFragment(value: Option[String]) extends AnyVal {

  /** Prepend the hashtag symbol if the fragment is present, otherwise returns the empty string. */
  def representation: String = value.map("#" ++ _).getOrElse("")

  def isEmpty: Boolean = value.isEmpty

  def nonEmpty: Boolean = value.nonEmpty

}
