package urldsl.vocabulary

import org.scalacheck._
import org.scalacheck.Prop._

object PrinterChecks extends Properties("PrinterChecks") {

  val someStringGen: Gen[String] = Gen.asciiPrintableStr

  def checkPrinterToString[T](t: T, printer: Printer[T]): Prop = Prop(t.toString == printer(t))

  property("strings are printed as is") = forAll(someStringGen)(checkPrinterToString(_, Printer.stringPrinter))
  property("ints are printed with toString") =
    forAll(Arbitrary.arbitrary[Int])(checkPrinterToString(_, Printer.intPrinter))
  property("longs are printed with toString") =
    forAll(Arbitrary.arbitrary[Long])(checkPrinterToString(_, Printer.longPrinter))
  property("booleans are printed with toString") =
    forAll(Gen.oneOf(true, false))(checkPrinterToString(_, Printer.booleanPrinter))
  property("doubles are printed with toString") =
    forAll(Arbitrary.arbitrary[Double])(checkPrinterToString(_, Printer.doublePrinter))
  property("big intss are printed with toString") =
    forAll(Arbitrary.arbitrary[BigInt])(checkPrinterToString(_, Printer.bigIntPrinter))
  property("floats are printed with toString") =
    forAll(Arbitrary.arbitrary[Float])(checkPrinterToString(_, Printer.floatPrinter))

  property("summoner method is called") = forAll(Gen.const(()))(_ => Printer[String] != null)

}
