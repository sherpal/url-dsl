package urldsl.url

import scala.scalajs.js

trait DefaultUrlStringDecoder {

  protected final val defaultDecoder0: UrlStringDecoder = (str: String, _: String) =>
    js.Dynamic.global.applyDynamic("decodeURIComponent")(str).toString

}
