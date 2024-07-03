package urldsl.url

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

/** This is stolen from org.scalajs.dom so that we don't need to import the whole library. */
@js.native
@JSGlobal
private[url] final class URL(url: String) extends js.Object {

  /** Is a DOMString containing an initial '/' followed by the path of the URL.
    *
    * MDN
    */
  var pathname: String = js.native

  /** Is a DOMString containing a '?' followed by the parameters of the URL.
    *
    * MDN
    */
  var search: String = js.native

  /** Is a DOMString containing a '#' followed by the fragment identifier of the URL.
    *
    * MDN
    */
  var hash: String = js.native
}
