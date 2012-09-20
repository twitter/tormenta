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
