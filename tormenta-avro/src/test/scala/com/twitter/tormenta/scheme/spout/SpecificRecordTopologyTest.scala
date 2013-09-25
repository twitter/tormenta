package com.twitter.tormenta.scheme.spout

import org.specs.Specification
import com.twitter.tormenta.AvroTestHelper
import com.twitter.bijection.avro.AvroCodecs
import com.twitter.tormenta.spout.avro.SpecificRecordTraversableSpout
import com.twitter.tormenta.spout.Spout
import backtype.storm.topology.TopologyBuilder
import backtype.storm.{Testing, LocalCluster}
import backtype.storm.testing.{TestGlobalCount, MockedSources, CompleteTopologyParam}
import backtype.storm.tuple.Values
import avro.FiscalRecord
import scala.collection.JavaConverters._

/**
 * @author Mansur Ashraf
 * @since 9/25/13
 */
object SpecificRecordTopologyTest extends Specification with AvroTestHelper {
  val inj = AvroCodecs[FiscalRecord]

  val specificSpout: Spout[Array[Byte]] = SpecificRecordTraversableSpout(List(
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
