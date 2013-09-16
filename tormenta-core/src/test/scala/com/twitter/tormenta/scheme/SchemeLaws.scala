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

import org.specs._
import scala.collection.JavaConverters._

object SchemeWithHandlerSpecification extends Specification {
  "Scheme should handle failure" should {
    "test failure" in {
      val f: Array[Byte] => List[String] = b => throw new IllegalArgumentException("decode failed")

      val schemeWithErrorHandler = Scheme(f).withHandler(t => List(t.getMessage))
      val result = schemeWithErrorHandler.deserialize("test string".getBytes("UTF-8"))

      result.asScala.isEmpty mustBe false   //test fails, returns an empty list
      result.asScala.head.get(0) must_== "decode failed"
    }
  }
}
