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
import com.twitter.tormenta.scheme.Scheme
import backtype.storm.task.TopologyContext

trait SchemeSpout[+T] extends BaseSpout[T] {
  /**
   * This is the only required override.
   */
  def getSpout[R](transformer: Scheme[T] => Scheme[R], fn: => TopologyContext => Unit): IRichSpout

  override def poll = List()

  override def getSpout = getSpout(identity(_), callOnOpen)

  override def flatMap[U](fn: T => TraversableOnce[U]): BaseSpout[U] =
    new FlatMappedSchemeSpout(this)(fn)
}
