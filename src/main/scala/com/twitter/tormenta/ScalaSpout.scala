package com.twitter.tormenta

import backtype.storm.spout.KestrelThriftSpout
import backtype.storm.topology.IRichSpout

import scala.collection.JavaConverters._
import storm.kafka.{ KafkaSpout => StormKafkaSpout, KafkaConfig, SpoutConfig }

/**
 *  @author Oscar Boykin
 *  @author Sam Ritchie
 */

trait ScalaSpout[T] extends java.io.Serializable {
  def getSpout: IRichSpout = getSpout(identity _)
  def getSpout[R](transformer: (ScalaScheme[T]) => ScalaScheme[R]): IRichSpout
  def parallelism: Int
}

class KestrelSpout[T](scheme: ScalaScheme[T], hosts : List[String], name : String, val parallelism : Int = 1, port : Int = 2229)
extends ScalaSpout[T] {
  override def getSpout[R](transformer: (ScalaScheme[T]) => ScalaScheme[R]) =
    new KestrelThriftSpout(hosts.asJava, port, name, transformer(scheme))
}

// TODO: Make zookeeper information configurable
class KafkaSpout[T](scheme: ScalaScheme[T], zkHost: String, topic: String, appID: String, val parallelism: Int = 1)
extends ScalaSpout[T] {

  override def getSpout[R](transformer: (ScalaScheme[T]) => ScalaScheme[R]) = {
    val spoutConfig = new SpoutConfig(new KafkaConfig.ZkHosts(zkHost, "/brokers"),
                                      topic,
                                      "/kafkastorm",
                                      appID)

    spoutConfig.scheme = transformer(scheme)
    spoutConfig.forceStartOffsetTime(-1)

    new StormKafkaSpout(spoutConfig)
  }
}
