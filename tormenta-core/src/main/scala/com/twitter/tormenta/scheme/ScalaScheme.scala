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
import com.twitter.bijection.{ AbstractBijection, Bijection, ImplicitBijection }
import java.util.{ List => JList }
import scala.collection.JavaConverters._
import java.io.Serializable

/**
 *  @author Oscar Boykin
 *  @author Sam Ritchie
 */

object ScalaScheme {
  val identity: ScalaScheme[Array[Byte]] = ScalaScheme(Some(_))

  implicit def bijection[T, U](implicit bij: ImplicitBijection[T, U])
      : Bijection[ScalaScheme[T], ScalaScheme[U]] =
    new AbstractBijection[ScalaScheme[T], ScalaScheme[U]] {
      def apply(scheme: ScalaScheme[T]) = scheme.map(bij(_))
      override def invert(scheme: ScalaScheme[U]) = scheme.map(bij.invert(_))
    }

  def apply[T](decodeFn: Array[Byte] => TraversableOnce[T]) =
    new ScalaScheme[T] {
      override def decode(bytes: Array[Byte]) = decodeFn(bytes)
    }
}

trait ScalaScheme[T] extends MultiScheme with Serializable { self =>
  /**
    * This is the only method you're required to implement.
    */
  def decode(bytes: Array[Byte]): TraversableOnce[T]

  def handle(t: Throwable): TraversableOnce[T] = List.empty

  def withHandler(fn: Throwable => TraversableOnce[T]): ScalaScheme[T] =
    new ScalaScheme[T] {
      override def handle(t: Throwable) = fn(t)
      override def decode(bytes: Array[Byte]) = self.decode(bytes)
    }

  def filter(fn: T => Boolean): ScalaScheme[T] =
    ScalaScheme(self.decode(_).filter(fn))

  def map[R](fn: T => R): ScalaScheme[R] =
    ScalaScheme(self.decode(_).map(fn))

  def flatMap[R](fn: T => TraversableOnce[R]): ScalaScheme[R] =
    ScalaScheme(self.decode(_).flatMap(fn))

  private def cast(t: T): JList[AnyRef] = new Values(t.asInstanceOf[AnyRef])
  private def toJava(items: TraversableOnce[T]) =
    if (!items.isEmpty)
      items.map(cast).toIterable.asJava
    else
      null

  override def deserialize(bytes: Array[Byte]) =
    try {
      toJava(decode(bytes))
    } catch {
      case t: Throwable => toJava(handle(t))
    }

  override lazy val getOutputFields = new Fields("summingEvent")
}
