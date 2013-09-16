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

import com.twitter.bijection.Injection
import scala.util.{Failure, Success}
import com.twitter.tormenta.scheme.Scheme

/**
 * @author Mansur Ashraf
 * @since 9/14/13
 */
trait AvroScheme[T] extends Scheme[T] {

  def decodeRecord(bytes: Array[Byte])(implicit inj: Injection[T, Array[Byte]]): TraversableOnce[T] = Injection.invert[T, Array[Byte]](bytes) match {
    case Success(x) => Seq(x)
    case Failure(x) => throw x
  }
}
