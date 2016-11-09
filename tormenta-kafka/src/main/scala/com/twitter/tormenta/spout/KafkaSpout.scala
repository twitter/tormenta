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

import com.twitter.tormenta.scheme.Scheme
import org.apache.storm.kafka.{ SpoutConfig, ZkHosts, KafkaSpout => StormKafkaSpout }
import org.apache.storm.task.TopologyContext

/**
 *  @author Oscar Boykin
 *  @author Sam Ritchie
 */

class KafkaSpout[+T](scheme: Scheme[T], zkHost: String, brokerZkPath: String, topic: String, appID: String, zkRoot: String, forceStartOffsetTime: Int = -1)
    extends SchemeSpout[T] {
  override def getSpout[R](transformer: Scheme[T] => Scheme[R], callOnOpen: => TopologyContext => Unit) = {
    // Spout ID needs to be unique per spout, so create that string by taking the topic and appID.
    val spoutId = topic + appID
    val spoutConfig = new SpoutConfig(new ZkHosts(zkHost, brokerZkPath), topic, zkRoot, spoutId)

    spoutConfig.scheme = transformer(scheme)
    if (forceStartOffsetTime != -1) {
      spoutConfig.startOffsetTime = forceStartOffsetTime
    }

    new RichStormSpout(new StormKafkaSpout(spoutConfig), callOnOpen)
  }
}
