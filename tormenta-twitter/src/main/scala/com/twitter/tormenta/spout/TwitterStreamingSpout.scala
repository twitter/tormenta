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
import backtype.storm.tuple.{Fields, Values}
import backtype.storm.utils.Time
import com.twitter.tormenta.scheme.ScalaScheme
import java.util.{Map => JMap}
import java.util.concurrent.LinkedBlockingQueue
import twitter4j._

/**
  * Storm Spout implementation for Twitter's streaming API.
  *
  * @author Sam Ritchie
  */

object TwitterStreamingSpout {
  val QUEUE_LIMIT = 1000 // default max queue size.
  val FIELD_NAME = "tweet" // default output field name.

  def apply(factory: TwitterStreamFactory, limit: Int = QUEUE_LIMIT): TwitterStreamingSpout[Status] =
    of(factory, limit)(identity)

  def of[R](factory: TwitterStreamFactory, limit: Int = QUEUE_LIMIT, fieldName: String = FIELD_NAME)
    (transformer: ScalaScheme[Status] => ScalaScheme[R]): TwitterStreamingSpout[R] =
    new TwitterStreamingSpout[R](factory, limit, fieldName, transformer)
}

class TwitterStreamingSpout[R](
  factory: TwitterStreamFactory,
  limit: Int,
  fieldName: String,
  transformer: ScalaScheme[Status] => ScalaScheme[R]) extends BaseRichSpout {

  private lazy val scheme: ScalaScheme[R] = transformer {
    new ScalaScheme[Status] {
      def decode(bytes: Array[Byte]) = Some(bytes.asInstanceOf[Status])
    }
  }

  private def expand(status: Status): TraversableOnce[R] =
    scheme.decode(status.asInstanceOf[Array[Byte]])

  lazy val queue = new LinkedBlockingQueue[Status](limit)

  lazy val listener = new StatusListener {
    def onStatus(status: Status) {
      queue.offer(status)
    }
    def onDeletionNotice(notice: StatusDeletionNotice) { }
    def onScrubGeo(userId: Long, upToStatusId: Long) { }
    def onStallWarning(warning: StallWarning) { }
    def onTrackLimitationNotice(numberOfLimitedStatuses: Int) { }
    def onException(ex: Exception) { }
  }

  var stream: TwitterStream = null
  var collector: SpoutOutputCollector = null

  override def open(conf: JMap[_, _], context: TopologyContext, coll: SpoutOutputCollector) {
    collector = coll
    stream = factory.getInstance
    stream.addListener(listener)

    // TODO: Add support beyond "sample". (GardenHose, for example.)
    stream.sample
  }

  override def nextTuple {
    Option(queue.poll) match {
      case None => Time.sleep(50)
      case Some(status) => expand(status).foreach { r =>
        collector.emit(new Values(r.asInstanceOf[AnyRef]))
      }
    }
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer) {
    declarer.declare(new Fields(fieldName))
  }

  override def close { stream.shutdown }
}
