package urldsl.language

/**
  *The following code has been stolen at
  * https://github.com/julienrf/endpoints/blob/7d0af49bc7a83b5985815d936201616ed8a3fa5d/json-schema/json-schema/src/main/scala/endpoints/Tupler.scala
  * by julienrf
  */
/**
  * The original LICENSE:
  *
  * The MIT License (MIT)
  *
  * Copyright (c) 2016 Julien Richard-Foy
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
/**
  * Defines a strategy for tupling `A` and `B` values, according to types `A` and `B`.
  *
  * The actual implementation avoids nested tuples and eliminates `Unit`, so that instead of ending with, e.g.,
  * the following type:
  *
  * {{{
  *   ((Unit, Int), (((Unit, Unit), String)))
  * }}}
  *
  * We just get:
  *
  * {{{
  *   (Int, String)
  * }}}
  *
  * The following rules are implemented (by increasing priority):
  *  - A, B               -> (A, B)
  *  - A, (B, C)          -> (A, B, C)
  *  - (A, B), C          -> (A, B, C)
  *  - (A, B), (C, D)     -> (A, B, C, D)
  *  - A, (B, C, D, E)    -> (A, B, C, D, E)
  *  - (A, B), (C, D, E)  -> (A, B, C, D, E)
  *  - (A, B, C), D       -> (A, B, C, D)
  *  - (A, B, C, D), E    -> (A, B, C, D, E)
  *  - (A, B, C, D, E), F -> (A, B, C, D, E, F)
  *  - A, Unit            -> A
  *  - Unit, A            -> A
  */
//#definition
trait Tupler[A, B] {
  type Out
  //#definition
  def apply(a: A, b: B): Out
  def unapply(out: Out): (A, B)
  //#definition
}
//#definition

object Tupler extends Tupler4

trait Tupler1 {
  type Aux[A, B, Out0] = Tupler[A, B] { type Out = Out0 }

  implicit def ab[A, B]: Aux[A, B, (A, B)] = new Tupler[A, B] {
    type Out = (A, B)
    def apply(a: A, b: B): (A, B) = (a, b)
    def unapply(out: (A, B)): (A, B) = out
  }

}

trait Tupler2 extends Tupler1 {

  implicit def tupler1And2[A, B, C]: Aux[A, (B, C), (A, B, C)] =
    new Tupler[A, (B, C)] {
      type Out = (A, B, C)
      def apply(a: A, bc: (B, C)): (A, B, C) = (a, bc._1, bc._2)
      def unapply(out: (A, B, C)): (A, (B, C)) = {
        val (a, b, c) = out
        (a, (b, c))
      }
    }

  implicit def tupler2And1[A, B, C]: Aux[(A, B), C, (A, B, C)] =
    new Tupler[(A, B), C] {
      type Out = (A, B, C)
      def apply(ab: (A, B), c: C): (A, B, C) = (ab._1, ab._2, c)
      def unapply(out: (A, B, C)): ((A, B), C) = {
        val (a, b, c) = out
        ((a, b), c)
      }
    }

}

trait Tupler3 extends Tupler2 {

  implicit def tupler2And2[A, B, C, D]: Aux[(A, B), (C, D), (A, B, C, D)] =
    new Tupler[(A, B), (C, D)] {
      type Out = (A, B, C, D)
      def apply(ab: (A, B), cd: (C, D)): (A, B, C, D) = (ab._1, ab._2, cd._1, cd._2)
      def unapply(out: (A, B, C, D)): ((A, B), (C, D)) = {
        val (a, b, c, d) = out
        ((a, b), (c, d))
      }
    }

  implicit def tupler2And3[A, B, C, D, E]: Tupler[(A, B), (C, D, E)] { type Out = (A, B, C, D, E) } =
    new Tupler[(A, B), (C, D, E)] {
      type Out = (A, B, C, D, E)
      def apply(ab: (A, B), cde: (C, D, E)): (A, B, C, D, E) = (ab._1, ab._2, cde._1, cde._2, cde._3)
      def unapply(out: (A, B, C, D, E)): ((A, B), (C, D, E)) = {
        val (a, b, c, d, e) = out
        ((a, b), (c, d, e))
      }
    }

  implicit def leftUnit[A]: Aux[Unit, A, A] = new Tupler[Unit, A] {
    type Out = A
    def apply(a: Unit, b: A): A = b
    def unapply(out: Out): (Unit, A) = ((), out)
  }

}

trait Tupler4 extends Tupler3 {

  implicit def tupler1And4[A, B, C, D, E]: Tupler[A, (B, C, D, E)] { type Out = (A, B, C, D, E) } =
    new Tupler[A, (B, C, D, E)] {
      type Out = (A, B, C, D, E)
      def apply(a: A, bcde: (B, C, D, E)): (A, B, C, D, E) = (a, bcde._1, bcde._2, bcde._3, bcde._4)
      def unapply(out: (A, B, C, D, E)): (A, (B, C, D, E)) = {
        val (a, b, c, d, e) = out
        (a, (b, c, d, e))
      }
    }

  implicit def tupler4And1[A, B, C, D, E]: Tupler[(A, B, C, D), E] { type Out = (A, B, C, D, E) } =
    new Tupler[(A, B, C, D), E] {
      type Out = (A, B, C, D, E)
      def apply(abcd: (A, B, C, D), e: E): (A, B, C, D, E) = (abcd._1, abcd._2, abcd._3, abcd._4, e)
      def unapply(out: (A, B, C, D, E)): ((A, B, C, D), E) = {
        val (a, b, c, d, e) = out
        ((a, b, c, d), e)
      }
    }
  
  implicit def rightUnit[A]: Aux[A, Unit, A] = new Tupler[A, Unit] {
    type Out = A
    def apply(a: A, b: Unit): A = a
    def unapply(out: Out): (A, Unit) = (out, ())
  }

}
