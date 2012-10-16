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

import backtype.storm.topology.IRichSpout
import com.twitter.tormenta.scheme.ScalaScheme

/**
 *  @author Oscar Boykin
 *  @author Sam Ritchie
 */

trait ScalaSpout[T] extends java.io.Serializable {
  def getSpout: IRichSpout = getSpout(identity _)
  def getSpout[R](transformer: (ScalaScheme[T]) => ScalaScheme[R]): IRichSpout
  def parallelism: Int
}
