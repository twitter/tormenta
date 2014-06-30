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

import backtype.storm.testing.CompletableSpout
import backtype.storm.topology.IRichSpout
import backtype.storm.topology.OutputFieldsDeclarer
import backtype.storm.tuple.Fields
import backtype.storm.tuple.Values
import clojure.lang.RT
import java.util.{ List => JList, ArrayList }
import collection.JavaConverters._
import com.twitter.tormenta.scheme.SchemeTransformer

object TraversableSpoutProvider {
  def apply[T](items: TraversableOnce[T], fieldName: String = "item"): TraversableSpoutProvider[T] =
    new TraversableSpoutProvider(items, fieldName)
}

class TraversableSpoutProvider[+T](items: TraversableOnce[T], fieldName: String) extends SpoutProvider[T] {
  private def wrap[T](t: T) = new Values(t.asInstanceOf[AnyRef])

  override def getSpout[R](transform: SchemeTransformer[T, R]) = {

    lazy val mutatedTuples = items.flatMap(transform(_))

    lazy val tupleList = mutatedTuples.toList
    lazy val javaList = new ArrayList(tupleList.map(wrap).asJava)

    new FixedTupleSpout(javaList, fieldName)
  }
}
