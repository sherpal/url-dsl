package urldsl.url

import scala.scalajs.js

trait DefaultUrlStringGenerator {

  val default: UrlStringGenerator = (str: String, _: String) =>
    js.Dynamic.global.applyDynamic("encodeURIComponent")(str).toString

}
