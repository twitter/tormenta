/*
 * Copyright 2013 Twitter inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.twitter.tormenta.scheme.spout

import org.specs.Specification
import com.twitter.tormenta.AvroTestHelper
import com.twitter.bijection.avro.SpecificAvroCodecs
import com.twitter.tormenta.spout.{ SpoutProvider, TraversableSpoutProvider }
import backtype.storm.topology.TopologyBuilder
import backtype.storm.{ Testing, LocalCluster }
import backtype.storm.testing.{ TestGlobalCount, MockedSources, CompleteTopologyParam }
import backtype.storm.tuple.Values
import avro.FiscalRecord
import scala.collection.JavaConverters._

/**
 * @author Mansur Ashraf
 * @since 9/25/13
 */
object SpecificRecordTopologyTest extends Specification with AvroTestHelper {
  val inj = SpecificAvroCodecs[FiscalRecord]

  val specificSpout: SpoutProvider[Array[Byte]] = TraversableSpoutProvider[FiscalRecord](List(
    buildSpecificAvroRecord("2010-01-01", 1, 1),
    buildSpecificAvroRecord("2010-02-02", 2, 2),
    buildSpecificAvroRecord("2010-04-03", 3, 3),
    buildSpecificAvroRecord("2010-04-04", 4, 4)
  )).flatMap(r => Seq(inj(r)))

  val builder = new TopologyBuilder
  val localCluster = new LocalCluster
  val completeTopologyParam = {
    val ret = new CompleteTopologyParam()
    ret.setMockedSources(new MockedSources)
    ret
  }

  builder.setSpout("1", specificSpout.getSpout, 1)
  builder.setBolt("2", new TestGlobalCount()).globalGrouping("1")
  val topo = builder.createTopology

  "Complete Topology" should {
    val localCluster = new LocalCluster

    "properly complete" in {
      val ret = Testing.completeTopology(localCluster, topo, completeTopologyParam)
      val spoutTuples = Testing.readTuples(ret, "1")
      val result = spoutTuples.asScala.toList.map(_.asInstanceOf[Values].get(0)).map(b => inj.invert(b.asInstanceOf[Array[Byte]]).get)
      result mustEqual List(
        buildSpecificAvroRecord("2010-01-01", 1, 1),
        buildSpecificAvroRecord("2010-02-02", 2, 2),
        buildSpecificAvroRecord("2010-04-03", 3, 3),
        buildSpecificAvroRecord("2010-04-04", 4, 4)
      )

      val countTuples = Testing.readTuples(ret, "2")
      countTuples.asScala.toList.map(_.asInstanceOf[Values].get(0)) mustEqual List(1, 2, 3, 4)
    }
    doLast {
      localCluster.shutdown()
    }
  }
}
