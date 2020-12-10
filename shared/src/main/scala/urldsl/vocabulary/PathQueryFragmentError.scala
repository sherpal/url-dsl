package urldsl.vocabulary

import urldsl.errors.{FragmentMatchingError, ParamMatchingError, PathMatchingError}

sealed trait PathQueryFragmentError[+P, +Q, +F]

object PathQueryFragmentError {

  case class PathError[A](error: A) extends PathQueryFragmentError[A, Nothing, Nothing]
  case class ParamsError[A](error: A) extends PathQueryFragmentError[Nothing, A, Nothing]
  case class FragmentError[A](error: A) extends PathQueryFragmentError[Nothing, Nothing, A]

}
