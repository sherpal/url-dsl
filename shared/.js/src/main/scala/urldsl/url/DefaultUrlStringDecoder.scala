package urldsl.url

import scala.scalajs.js

trait DefaultUrlStringDecoder {

  val defaultDecoder: UrlStringDecoder = (str: String, _: String) =>
    js.Dynamic.global.applyDynamic("decodeURIComponent")(str).toString

}
