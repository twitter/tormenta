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

package com.twitter.tormenta.scheme.avro.specific

import org.apache.avro.Schema
import com.twitter.tormenta.scheme.avro.AvroScheme
import com.twitter.tormenta.scheme.Scheme
import com.twitter.bijection.avro.SpecificAvroCodecs
import com.twitter.bijection.Injection._
import java.nio.ByteBuffer
import org.apache.avro.specific.SpecificRecordBase

/**
 * @author Mansur Ashraf
 * @since 9/14/13
 */
object SpecificAvroScheme {
  def apply[T <: SpecificRecordBase: Manifest] = new SpecificAvroScheme[T]
}

class SpecificAvroScheme[T <: SpecificRecordBase: Manifest] extends Scheme[T] with AvroScheme[T] {
  def decode(bytes: ByteBuffer): TraversableOnce[T] = {
    implicit val inj = SpecificAvroCodecs[T]
    decodeRecord(bytes)
  }
}

object BinaryAvroScheme {
  def apply[T <: SpecificRecordBase: Manifest] = new BinaryAvroScheme[T]
}

class BinaryAvroScheme[T <: SpecificRecordBase: Manifest] extends Scheme[T] with AvroScheme[T] {
  def decode(bytes: ByteBuffer): TraversableOnce[T] = {
    implicit val inj = SpecificAvroCodecs.toBinary[T]
    decodeRecord(bytes)
  }
}

object JsonAvroScheme {
  def apply[T <: SpecificRecordBase: Manifest](schema: Schema) = new JsonAvroScheme[T](schema)
}

class JsonAvroScheme[T <: SpecificRecordBase: Manifest](schema: Schema) extends Scheme[T] with AvroScheme[T] {
  def decode(bytes: ByteBuffer): TraversableOnce[T] = {
    implicit val avroInj = SpecificAvroCodecs.toJson[T](schema)
    implicit val inj = connect[T, String, Array[Byte]]
    decodeRecord(bytes)
  }
}

