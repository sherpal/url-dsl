package urldsl.url

import scala.scalajs.js

trait DefaultUrlStringGenerator {

  protected val default0: UrlStringGenerator = (str: String, _: String) =>
    js.Dynamic.global.applyDynamic("encodeURIComponent")(str).toString

}
