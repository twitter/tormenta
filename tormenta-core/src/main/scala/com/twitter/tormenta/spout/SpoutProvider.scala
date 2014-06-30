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
import backtype.storm.topology.base.BaseRichSpout
import backtype.storm.task.TopologyContext
import backtype.storm.spout.SpoutOutputCollector
import backtype.storm.topology.OutputFieldsDeclarer
import backtype.storm.tuple.{ Fields, Values }
import java.util.{ Map => JMap }
import com.twitter.tormenta.scheme.SchemeTransformer

object SpoutProvider {
  implicit def withMetrics[T](provider: SpoutProvider[T]): MetricsSpoutProvider[T] = new MetricsSpoutProvider(provider)

  def fromTraversable[T](items: TraversableOnce[T]): SpoutProvider[T] = new TraversableSpoutProvider(items, "item")

  def fromFn[T](fn: () => TraversableOnce[T]): SpoutProvider[T] =
    new SpoutProvider[T] {
      def getSpout[R](transformer: SchemeTransformer[T, R]): IRichSpout =
        new BaseRichSpout {
          var collector: SpoutOutputCollector = null
          override def open(conf: JMap[_, _], context: TopologyContext, coll: SpoutOutputCollector) {
            collector = coll
          }
          def fieldName: String = "item"
          def poll: TraversableOnce[T] = fn.apply
          def onEmpty: Unit = Thread.sleep(50)
          override def nextTuple {
            poll match {
              case Nil => onEmpty
              case items => items.foreach { item =>
                collector.emit(new Values(item.asInstanceOf[AnyRef]))
              }
            }
          }

          override def declareOutputFields(declarer: OutputFieldsDeclarer) {
            declarer.declare(new Fields(fieldName))
          }
        }
    }

}

trait SpoutProvider[+T] { self =>
  def getSpout: IRichSpout = getSpout(SchemeTransformer.identity)

  def getSpout[R](transformer: SchemeTransformer[T, R]): IRichSpout

  def flatMap[U](fn: T => TraversableOnce[U]): SpoutProvider[U] = new FlatMappedSpoutProvider(self)(fn)

  def filter(fn: T => Boolean): SpoutProvider[T] =
    flatMap[T](t => if (fn(t)) Some(t) else None)

  def map[U](fn: T => U): SpoutProvider[U] =
    flatMap(t => Some(fn(t)))
}
