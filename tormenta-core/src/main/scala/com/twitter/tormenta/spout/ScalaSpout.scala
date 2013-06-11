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

package com.twitter.tormenta.spout

import backtype.storm.topology.IRichSpout
import com.twitter.bijection.{ AbstractBijection, Bijection, ImplicitBijection }

import java.io.Serializable

/**
  * Base trait for ScalaSpout implementations.
  *
  *  @author Oscar Boykin
  *  @author Sam Ritchie
  */

object ScalaSpout {
  implicit def bijection[T, U](implicit bij: ImplicitBijection[T, U])
      : Bijection[ScalaSpout[T], ScalaSpout[U]] =
    new AbstractBijection[ScalaSpout[T], ScalaSpout[U]] {
      def apply(spout: ScalaSpout[T]) = spout.map(bij(_))
      override def invert(spout: ScalaSpout[U]) = spout.map(bij.invert(_))
    }

  // TODO: Should this be a TravOnce[TravOnce[T]] to test multi-emit?
  def fromTraversable[T](items: TraversableOnce[T]): ScalaSpout[T] =
    new RichScalaSpout[T] {
      private val iter = items.toIterator
      override def poll = if (iter.hasNext) Some(iter.next) else None
    }

  def fromFn[T](fn: () => TraversableOnce[T]): ScalaSpout[T] =
    new RichScalaSpout[T] {
      override def poll = fn.apply
    }
}

trait ScalaSpout[+T] extends Serializable {
  def getSpout: IRichSpout

  def flatMap[U](fn: T => TraversableOnce[U]): ScalaSpout[U]

  def filter(fn: T => Boolean): ScalaSpout[T] =
    flatMap[T](t => if (fn(t)) Some(t) else None)

  def map[U](fn: T => U): ScalaSpout[U] =
    flatMap(t => Some(fn(t)))
}
