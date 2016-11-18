/*
 * Copyright 2013 Twitter inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.twitter.tormenta.scheme.avro

import com.twitter.bijection.{ Bufferable, Injection }
import scala.util.{ Failure, Success }
import com.twitter.tormenta.scheme.Scheme
import java.nio.ByteBuffer

object BufferableScheme {
  def apply[T](inj: Injection[T, Array[Byte]]): BufferableScheme[T] =
    new BufferableScheme[T](Bufferable.viaInjection(Bufferable.byteArray, inj))
}

class BufferableScheme[T](bufferable: Bufferable[T]) extends Scheme[T] {
  override def decode(bytes: ByteBuffer): TraversableOnce[T] = {
    bufferable.get(bytes) match {
      case Success((newBuffer, x)) => Seq(x)
      case Failure(x) => throw x
    }
  }
}
