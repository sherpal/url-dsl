package urldsl.url

import org.scalacheck._
import org.scalacheck.Prop._
import urldsl.vocabulary.{Param, Segment}

//noinspection TypeAnnotation
object UrlStringGeneratorChecks extends Properties("UrlStringGeneratorChecks") {

  val generator = UrlStringGenerator.default

  val paramGen = Gen.nonEmptyListOf(Gen.asciiStr.filter(_.nonEmpty)).map(Param.apply)
  val paramMapGen = for {
    numberOfParams <- Gen.choose(0, 10)
    names <- Gen.listOfN(numberOfParams, Gen.alphaNumStr.filterNot(_.headOption.exists(_.isDigit)))
    params <- Gen.listOfN(names.length, paramGen)
  } yield names.zip(params).toMap
  val segmentGen = Gen.alphaNumStr.filter(_.nonEmpty).map(Segment.apply)
  val segmentsGen = Gen.nonEmptyListOf(segmentGen)

  property("Make url contains encoded segments") = forAllNoShrink(segmentsGen, paramMapGen) { (segments, paramMap) =>
    val url = generator.makeUrl(segments, paramMap)
    segments.map(_.content).map(generator.encode(_)).forall(url.contains)
  }

  property("Make url contains all param map names") = forAllNoShrink(segmentsGen, paramMapGen) { (segments, paramMap) =>
    val url = generator.makeUrl(segments, paramMap)
    paramMap.keys
      .map(name =>
        Prop(url.contains(name)) :|
          s"""Url did not contain the name:
         |Name: $name
         |Url: $url
         |""".stripMargin
      )
      .foldLeft(proved)(_ && _)
  }

  property("Make url contains all encoded param values") = forAllNoShrink(segmentsGen, paramMapGen) {
    (segments, paramMap) =>
      val url = generator.makeUrl(segments, paramMap)

      (for {
        param <- paramMap.values.toList
        content <- param.content
        encodedContent = generator.encode(content)
      } yield Prop(url.contains(encodedContent)) :| s"""Url did not contain the content $content:
         |Encoded content: $encodedContent
         |Url: $url
         |""".stripMargin).foldLeft(proved)(_ && _)
  }

}
