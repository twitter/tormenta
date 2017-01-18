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

import org.apache.storm.topology.TopologyBuilder
import org.apache.storm.testing.{ CompleteTopologyParam, MockedSources, TestGlobalCount, TestJob }
import org.apache.storm.{ ILocalCluster, Testing }
import com.twitter.tormenta.spout.Spout
import org.apache.storm.tuple.Values
import org.scalatest._
import scala.collection.JavaConverters._

class TopologyTest extends WordSpec with Matchers with BeforeAndAfter {
  val spout: Spout[Int] = Spout.fromTraversable(List(1, 2, 3, 4, 5))

  val builder = new TopologyBuilder
  val completeTopologyParam = {
    val ret = new CompleteTopologyParam()
    ret.setMockedSources(new MockedSources)
    ret
  }

  builder.setSpout("1", spout.getSpout, 1)
  builder.setBolt("2", new TestGlobalCount()).globalGrouping("1")
  val topo = builder.createTopology

  "Complete Topology" should {
    "properly complete" in {
      Testing.withSimulatedTimeLocalCluster(new TestJob {
        override def run(cluster: ILocalCluster): Unit = {
          val ret = Testing.completeTopology(cluster, topo, completeTopologyParam)
          val spoutTuples = Testing.readTuples(ret, "1")
          assert(spoutTuples.asScala.toList
            .map(_.asInstanceOf[Values].get(0)) == List(1, 2, 3, 4, 5))

          val countTuples = Testing.readTuples(ret, "2")
          assert(countTuples.asScala.toList
            .map(_.asInstanceOf[Values].get(0)) == List(1, 2, 3, 4, 5))
        }
      })
    }
  }
}
