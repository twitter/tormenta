/*
 Copyright 2012 Twitter, Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.twitter.tormenta.scheme

import java.nio.ByteBuffer
import org.scalatest._
import scala.collection.JavaConverters._

class SchemeWithHandlerSpecification extends WordSpec with Matchers {
  "Scheme" should {
    val f: ByteBuffer => List[String] = b => throw new IllegalArgumentException("decode failed")

    def eqv[T, U](t: T, u: U)(implicit ev: U =:= T): Boolean = (t == u)

    def checkResult[T](scheme: Scheme[T], expectedResult: List[T]) {
      val result = scheme.deserialize(ByteBuffer.wrap("test string".getBytes("UTF-8")))
      assert(result.asScala.isEmpty == false) //test fails, returns an empty list
      assert {
        eqv(result.asScala.toList.map(_.get(0).asInstanceOf[T]), expectedResult)
      }
    }

    val schemeWithErrorHandler = Scheme(f).withHandler(t => List(t.getMessage))

    "handle failure" in checkResult(schemeWithErrorHandler, List("decode failed"))

    "map failure" in checkResult(schemeWithErrorHandler.map(_.length), List(13))

    "flatMap failure" in {
      val scheme = schemeWithErrorHandler.flatMap(s => List(s, s))
      checkResult(scheme, List("decode failed", "decode failed"))
    }

    "filter failure" in {
      val scheme = schemeWithErrorHandler
        .flatMap(s => List(s, "one"))
        .filter(_ == "one")
      checkResult(scheme, List("one"))
    }
  }
}
