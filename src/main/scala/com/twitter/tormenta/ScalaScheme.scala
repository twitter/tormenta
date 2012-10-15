package com.twitter.tormenta

import backtype.storm.tuple.{ Fields, Values }
import backtype.storm.spout.Scheme

import com.twitter.util.Decoder

/**
 *  @author Oscar Boykin
 *  @author Sam Ritchie
 */

trait ScalaScheme[T] extends Scheme with java.io.Serializable {

  def decode(bytes: Array[Byte]): Option[T]

  def filter(fn: (T) => Boolean): ScalaScheme[T] = {
    val outerDecode = this.decode _
    new ScalaScheme[T] {
      override def decode(bytes: Array[Byte]) = outerDecode(bytes) filter { fn(_) }
    }
  }
  def map[R](fn: (T) => R): ScalaScheme[R] = {
    val outerDecode = this.decode _
    new ScalaScheme[R] {
      override def decode(bytes: Array[Byte]) = outerDecode(bytes) map { fn(_) }
    }
  }

  // TODO: Think of a more elegant way to handle exceptions vs
  // catching all. Can we expose this error handling to the user?
  override def deserialize(bytes: Array[Byte]) = {
    try {
      decode(bytes)
      .map { t: T => new Values(t.asInstanceOf[AnyRef]) }
      .getOrElse(null)
    } catch {
      case _ => null
    }
  }

  override lazy val getOutputFields = new Fields("summingEvent")
}

object DecoderScheme {
  implicit def apply[T](decoder: Decoder[T,Array[Byte]]) = new DecoderScheme(decoder)
}

class DecoderScheme[T](decoder: Decoder[T,Array[Byte]]) extends ScalaScheme[T] {
  override def decode(bytes: Array[Byte]) = Some(decoder.decode(bytes))
}
