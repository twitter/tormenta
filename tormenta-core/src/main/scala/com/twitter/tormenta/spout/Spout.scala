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

import backtype.storm.task.TopologyContext
import backtype.storm.topology.IRichSpout

import java.io.Serializable

/**
 * Base trait for Spout implementations.
 *
 *  @author Oscar Boykin
 *  @author Sam Ritchie
 */

object Spout {
  // TODO: Should this be a TravOnce[TravOnce[T]] to test multi-emit?
  def fromTraversable[T](items: TraversableOnce[T]): Spout[T] =
    TraversableSpout(items)

  def fromFn[T](fn: () => TraversableOnce[T]): Spout[T] =
    new BaseSpout[T] {
      override def poll = fn.apply
    }
}

trait Spout[+T] extends Serializable { self =>
  def getSpout: IRichSpout

  def registerMetricHandlers(metrics: () => TraversableOnce[Metric[_]], regFn: TopologyContext => Unit) = self

  def flatMap[U](fn: T => TraversableOnce[U]): Spout[U]

  def filter(fn: T => Boolean): Spout[T] =
    flatMap[T](t => if (fn(t)) Some(t) else None)

  def map[U](fn: T => U): Spout[U] =
    flatMap(t => Some(fn(t)))
}
