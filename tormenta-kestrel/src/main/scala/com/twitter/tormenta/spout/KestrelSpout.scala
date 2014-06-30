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

import backtype.storm.spout.KestrelThriftSpout
import com.twitter.tormenta.scheme.{ Scheme, SchemeTransformer }
import scala.collection.JavaConverters._

/**
 *  @author Oscar Boykin
 *  @author Sam Ritchie
 */

class KestrelSpout[+T](scheme: Scheme[T], hosts: List[String], name: String, port: Int = 2229)
    extends SpoutProvider[T] {
  override def getSpout[R](transformer: SchemeTransformer[T, R]) =
    new KestrelThriftSpout(hosts.asJava, port, name, scheme.transform(transformer))
}
