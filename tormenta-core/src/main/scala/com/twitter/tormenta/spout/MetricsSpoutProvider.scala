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

class MetricsSpoutProvider[+T](spout: SpoutProvider[T]) {
  def registerMetrics(metrics: () => TraversableOnce[Metric[_]]) =
    new MetricsEnabledSpoutProvider[T](spout, metrics)
}

class MetricsEnabledSpoutProvider[+T](spout: SpoutProvider[T], metrics: () => TraversableOnce[Metric[_]]) extends SpoutProvider[T] {
  override def getSpout: Spout[T] = getSpout(SchemeTransformer.identity)
  override def getSpout[R](transform: SchemeTransformer[T, R]): Spout[R] =
    new RichStormSpout[R, Spout[R]](spout.getSpout(transform), metrics)
}
