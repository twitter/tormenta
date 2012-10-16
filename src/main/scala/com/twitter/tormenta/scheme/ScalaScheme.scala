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
import backtype.storm.spout.Scheme

/**
 *  @author Oscar Boykin
 *  @author Sam Ritchie
 */

trait ScalaScheme[T] extends Scheme with java.io.Serializable {

  def decode(bytes: Array[Byte]): Option[T]

  def filter(fn: (T) => Boolean): ScalaScheme[T] = {
    val outerDecode = this.decode _
    new ScalaScheme[T] {
      override def decode(bytes: Array[Byte]) = outerDecode(bytes) filter { fn(_) }
    }
  }
  def map[R](fn: (T) => R): ScalaScheme[R] = {
    val outerDecode = this.decode _
    new ScalaScheme[R] {
      override def decode(bytes: Array[Byte]) = outerDecode(bytes) map { fn(_) }
    }
  }

  // TODO: Think of a more elegant way to handle exceptions vs
  // catching all. Can we expose this error handling to the user?
  override def deserialize(bytes: Array[Byte]) = {
    try {
      decode(bytes)
      .map { t: T => new Values(t.asInstanceOf[AnyRef]) }
      .getOrElse(null)
    } catch {
      case _ => null
    }
  }

  override lazy val getOutputFields = new Fields("summingEvent")
}
