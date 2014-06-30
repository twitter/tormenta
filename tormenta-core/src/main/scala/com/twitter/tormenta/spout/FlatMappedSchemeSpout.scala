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
import com.twitter.tormenta.scheme.SchemeTransformer
import backtype.storm.topology.base.BaseRichSpout

/**
 * SpoutProvider that performs a flatMap operation on its contained
 * SchemeSpout. Used to implement map, filter and flatMap on
 * SchemeSpout.
 */

class FlatMappedSpoutProvider[+T](spout: SpoutProvider[T]) {
  def flatMap[U](fn: T => TraversableOnce[U]): SpoutProvider[U] = new FlatMappedEnabledSpoutProvider(spout)(fn)

  def filter(fn: T => Boolean): SpoutProvider[T] =
    flatMap[T](t => if (fn(t)) Some(t) else None)

  def map[U](fn: T => U): SpoutProvider[U] =
    flatMap(t => Some(fn(t)))
}

class FlatMappedEnabledSpoutProvider[-T, +U](provider: SpoutProvider[T])(fn: T => TraversableOnce[U]) extends SpoutProvider[U] {
  override def getSpout[R](transform: SchemeTransformer[U, R]) =
    provider.getSpout(new SchemeTransformer(fn).flatMap(transform.apply(_)))
}
