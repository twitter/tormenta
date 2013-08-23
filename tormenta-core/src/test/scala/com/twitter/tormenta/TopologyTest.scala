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

package com.twitter.tormenta

import backtype.storm.topology.TopologyBuilder
import backtype.storm.testing.{ MockedSources, TestGlobalCount, CompletableSpout }
import backtype.storm.LocalCluster
import com.twitter.tormenta.spout.Spout
import backtype.storm.testing.CompleteTopologyParam
import backtype.storm.Testing
import org.specs._

class TopologyTest extends Specification {
  val spout: Spout[Int] = Spout.fromTraversable(List(1,2,3,4,5))

  val builder = new TopologyBuilder
  val localCluster = new LocalCluster
  val completeTopologyParam = {
    val ret = new CompleteTopologyParam()
    ret.setMockedSources(new MockedSources)
    ret
  }

  builder.setSpout("1", spout.getSpout, 1)
  builder.setBolt("2", new TestGlobalCount()).globalGrouping("1")
  val topo = builder.createTopology

  // The following throws a NPE because we need to have a
  // MockedSources present:
  //
  // Testing.completeTopology(localCluster, topo)
  //
  // So use this instead:
  Testing.completeTopology(localCluster, topo, completeTopologyParam)
}
