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

import backtype.storm.spout.SpoutOutputCollector
import backtype.storm.task.TopologyContext
import backtype.storm.topology.OutputFieldsDeclarer
import backtype.storm.topology.base.BaseRichSpout
import backtype.storm.tuple.{ Fields, Values }
import backtype.storm.utils.Time
import java.util.{ Map => JMap }

trait BaseSpout[+T] extends BaseRichSpout with Spout[T] { self =>
  var collector: SpoutOutputCollector = null

  override def registerMetrics(metrics: () => TraversableOnce[Metric[_]]) =
    new BaseSpout[T] {
      override def fieldName = self.fieldName
      override def onEmpty = self.onEmpty
      override def poll = self.poll
      override def metricFactory = metrics :: self.metricFactory
    }

  protected def metricFactory: List[() => TraversableOnce[Metric[_]]] = List()

  override def open(conf: JMap[_, _], context: TopologyContext, coll: SpoutOutputCollector) {
    collector = coll
    metricFactory.foreach(mList => mList().foreach(_.register(context)))
  }

  def fieldName: String = "item"

  /**
   * Override to supply new tuples.
   */
  def poll: TraversableOnce[T]

  /**
   * Override this to change the default spout behavior if poll
   * returns an empty list.
   */
  def onEmpty: Unit = Time.sleep(50)

  override def getSpout = this

  override def flatMap[U](fn: T => TraversableOnce[U]) =
    new BaseSpout[U] {
      override def fieldName = self.fieldName
      override def onEmpty = self.onEmpty
      override def poll = self.poll.flatMap(fn)
      override def metricFactory = self.metricFactory
    }

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
