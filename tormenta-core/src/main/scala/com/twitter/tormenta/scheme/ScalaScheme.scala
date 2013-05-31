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

import backtype.storm.tuple.{ Fields, Values }
import backtype.storm.spout.MultiScheme
import java.util.{ List => JList }
import scala.collection.JavaConverters._


/**
 *  @author Oscar Boykin
 *  @author Sam Ritchie
 */

object ScalaScheme {
  def apply[T](decodeFn: (Array[Byte]) => TraversableOnce[T]) =
    new ScalaScheme[T] {
      override def decode(bytes: Array[Byte]) = decodeFn(bytes)
    }
}

trait ScalaScheme[T] extends MultiScheme with java.io.Serializable {
  def decode(bytes: Array[Byte]): TraversableOnce[T]

  def filter(fn: (T) => Boolean): ScalaScheme[T] =
    ScalaScheme(this.decode(_) filter { fn(_) })

  def map[R](fn: (T) => R): ScalaScheme[R] =
    ScalaScheme(this.decode(_) map { fn(_) })

  def flatMap[R](fn: (T) => TraversableOnce[R]): ScalaScheme[R] =
    ScalaScheme(this.decode(_) flatMap { fn(_) })

  private def cast(t: T): JList[AnyRef] = new Values(t.asInstanceOf[AnyRef])

  // TODO: Catch exceptions and put them into an "Either" type. The
  // final scheme should return Either[Exception,Result].
  override def deserialize(bytes: Array[Byte]) = {
    try {
      val tuples = decode(bytes) map { cast(_) }
      if (!tuples.isEmpty) tuples.toIterable.asJava else null
    } catch {
      case _: Throwable => null
    }
  }

  override lazy val getOutputFields = new Fields("summingEvent")
}
