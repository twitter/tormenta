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

package com.twitter.tormenta;

/**
 *  @author Oscar Boykin
 *  @author Sam Ritchie
 *
 * This class is just to get around raw-type issues, etc from Scala
 * Raw types, or some non-fully type parameterized generics cause problems in scala.
 * This class helps deal with this.
 */
public class ScalaInterop {
  protected ScalaInterop() { }
  public static backtype.storm.drpc.DRPCSpout makeDRPC(String function) {
    return new backtype.storm.drpc.DRPCSpout(function);
  }
  public static storm.trident.Stream newDRPCStream(storm.trident.TridentTopology top,
    String streamName) {
    return top.newDRPCStream(streamName);
  }
}
