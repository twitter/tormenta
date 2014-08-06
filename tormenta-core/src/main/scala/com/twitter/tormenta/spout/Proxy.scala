package com.twitter.tormenta.spout

import backtype.storm.spout.SpoutOutputCollector
import backtype.storm.task.TopologyContext
import backtype.storm.topology.IRichSpout
import backtype.storm.topology.OutputFieldsDeclarer
import com.twitter.tormenta.Externalizer
import java.io.Serializable
import java.util.{ Map => JMap }

/**
 * *
 * Proxied trait for type T
 * allows for overriding certain methods but forwarding behavior of all other methods of T.
 * See com.twitter.storehaus.Proxy for a detailed example.
 */
trait Proxied[+T] {
  protected def self: T
}

trait SpoutProxy[T, U <: Spout[T]] extends Spout[T] with Proxied[U] {
  override def open(conf: JMap[_, _], topologyContext: TopologyContext, outputCollector: SpoutOutputCollector) =
    self.open(conf, topologyContext, outputCollector)
  override def nextTuple = self.nextTuple
  override def declareOutputFields(declarer: OutputFieldsDeclarer) = self.declareOutputFields(declarer)
  override def close = self.close
  override def ack(msgId: Object) = self.ack(msgId)
  override def fail(msgId: Object) = self.fail(msgId)
  override def deactivate = self.deactivate
  override def getComponentConfiguration = self.getComponentConfiguration
  override def activate = self.activate
}

class RichStormSpout[T, U <: Spout[T]](val self: U,
    @transient metrics: () => TraversableOnce[Metric[_]]) extends SpoutProxy[T, U] {
  val lockedMetrics = Externalizer(metrics)

  override def open(conf: JMap[_, _], context: TopologyContext, coll: SpoutOutputCollector) {
    lockedMetrics.get().foreach(_.register(context))
    self.open(conf, context, coll)
  }
}
