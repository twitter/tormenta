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

import backtype.storm.topology.TopologyBuilder
import backtype.storm.testing.{ MockedSources, TestGlobalCount }
import backtype.storm.LocalCluster
import com.twitter.tormenta.spout.TraversableSpout
import backtype.storm.testing.CompleteTopologyParam
import backtype.storm.tuple.Values
import backtype.storm.Testing

import org.scalatest._
import scala.collection.JavaConverters._
import com.twitter.tormenta.AvroTestHelper
import com.twitter.bijection.avro.GenericAvroCodecs
import org.apache.avro.generic.GenericRecord

/**
 * @author Mansur Ashraf
 * @since 9/25/13
 */

object GenericRecordTopologyTest extends WordSpec with Matchers with BeforeAndAfter with AvroTestHelper {
  val inj = GenericAvroCodecs[GenericRecord](testSchema)

  val genericSpout = TraversableSpout[GenericRecord](List(
    buildGenericAvroRecord("2010-01-01", 1, 1),
    buildGenericAvroRecord("2010-02-02", 2, 2),
    buildGenericAvroRecord("2010-04-03", 3, 3),
    buildGenericAvroRecord("2010-04-04", 4, 4)
  )).flatMap(r => Seq(inj(r)))

  val builder = new TopologyBuilder
  val localCluster = new LocalCluster
  val completeTopologyParam = {
    val ret = new CompleteTopologyParam()
    ret.setMockedSources(new MockedSources)
    ret
  }

  builder.setSpout("1", genericSpout.getSpout, 1)
  builder.setBolt("2", new TestGlobalCount()).globalGrouping("1")
  val topo = builder.createTopology

  "Complete Topology" should {
    val localCluster = new LocalCluster

    "properly complete" in {
      val ret = Testing.completeTopology(localCluster, topo, completeTopologyParam)
      val spoutTuples = Testing.readTuples(ret, "1")
      val result1 = spoutTuples.asScala.toList.map(_.asInstanceOf[Values].get(0)).map(b => inj.invert(b.asInstanceOf[Array[Byte]]).get)
      assert(result1 == List(
        buildGenericAvroRecord("2010-01-01", 1, 1),
        buildGenericAvroRecord("2010-02-02", 2, 2),
        buildGenericAvroRecord("2010-04-03", 3, 3),
        buildGenericAvroRecord("2010-04-04", 4, 4)
      ))

      val countTuples = Testing.readTuples(ret, "2")
      assert(countTuples.asScala.toList.map(_.asInstanceOf[Values].get(0)) == List(1, 2, 3, 4))

    }
    after {
      localCluster.shutdown()
    }
  }
}
