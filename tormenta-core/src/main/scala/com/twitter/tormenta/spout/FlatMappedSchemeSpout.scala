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

/**
  * Spout that performs a flatMap operation on its contained
  * SchemeSpout. Used to implement map, filter and flatMap on
  * SchemeSpout.
  */

class FlatMappedSchemeSpout[-T, +U](spout: SchemeSpout[T])(fn: T => TraversableOnce[U])
    extends SchemeSpout[U] {
  override def getSpout = spout.getSpout(_.flatMap(fn), metricFactory.toList)
  override def getSpout[R](transform: Scheme[U] => Scheme[R], metrics: List[()=>TraversableOnce[Metric[_]]]) =
    spout.getSpout(scheme => transform(scheme.flatMap(fn)), metricFactory.toList ++ metrics)
}
