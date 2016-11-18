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

import org.apache.storm.tuple.{ Fields, Values }
import org.apache.storm.spout.MultiScheme
import java.util.{ List => JList }
import scala.collection.JavaConverters._
import java.io.Serializable
import java.nio.ByteBuffer
import org.slf4j.LoggerFactory

/**
 *  @author Oscar Boykin
 *  @author Sam Ritchie
 */

object Scheme {
  val identity: Scheme[ByteBuffer] = Scheme(Iterator.single)

  def apply[T](decodeFn: ByteBuffer => TraversableOnce[T]) =
    new Scheme[T] {
      override def decode(bytes: ByteBuffer) = decodeFn(bytes)
    }
}

trait Scheme[+T] extends MultiScheme with Serializable { self =>
  /**
   * This is the only method you're required to implement.
   */
  def decode(bytes: ByteBuffer): TraversableOnce[T]

  def handle(t: Throwable): TraversableOnce[T] = {
    // We assume this is rare enough that the perf hit of
    // getLogger+getClass is better than
    // forcing a new variable on everyone, even those that override this
    LoggerFactory.getLogger(getClass).error("decoding error, ignoring", t)
    List.empty
  }

  def withHandler[U >: T](fn: Throwable => TraversableOnce[U]): Scheme[U] =
    new Scheme[U] {
      override def handle(t: Throwable) = fn(t)
      override def decode(bytes: ByteBuffer) = self.decode(bytes)
    }

  def filter(fn: T => Boolean): Scheme[T] =
    Scheme(self.decode(_).filter(fn))
      .withHandler(self.handle(_).filter(fn))

  def map[R](fn: T => R): Scheme[R] =
    Scheme(self.decode(_).map(fn))
      .withHandler(self.handle(_).map(fn))

  def flatMap[R](fn: T => TraversableOnce[R]): Scheme[R] =
    Scheme(self.decode(_).flatMap(fn))
      .withHandler(self.handle(_).flatMap(fn))

  private def cast(t: Any): JList[AnyRef] = new Values(t.asInstanceOf[AnyRef])
  private def toJava(items: TraversableOnce[Any]) =
    if (!items.isEmpty)
      items.map(cast).toIterable.asJava
    else
      null

  override def deserialize(bytes: ByteBuffer) =
    try {
      toJava(decode(bytes))
    } catch {
      case t: Throwable => toJava(handle(t))
    }

  override lazy val getOutputFields = new Fields("summingEvent")
}
