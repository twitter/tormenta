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

package com.twitter.tormenta.scheme.avro.generic

import org.apache.avro.generic.GenericRecord
import org.apache.avro.Schema
import com.twitter.bijection.avro.GenericAvroCodecs
import com.twitter.bijection.Injection.connect
import com.twitter.tormenta.scheme.avro.AvroScheme
import com.twitter.tormenta.scheme.Scheme

/**
 * @author Mansur Ashraf
 * @since 9/14/13
 */
object GenericAvroScheme {
  def apply[T <: GenericRecord](schema: Schema) = new GenericAvroScheme[T](schema)
}

class GenericAvroScheme[T <: GenericRecord](schema: Schema) extends Scheme[T] with AvroScheme[T] {
  def decode(bytes: Array[Byte]): TraversableOnce[T] = {
    implicit val inj = GenericAvroCodecs[T](schema)
    decodeRecord(bytes)
  }
}

object BinaryAvroScheme {
  def apply[T <: GenericRecord](schema: Schema) = new BinaryAvroScheme[T](schema)
}

class BinaryAvroScheme[T <: GenericRecord](schema: Schema) extends Scheme[T] with AvroScheme[T] {
  def decode(bytes: Array[Byte]): TraversableOnce[T] = {
    implicit val inj = GenericAvroCodecs.toBinary[T](schema)
    decodeRecord(bytes)
  }
}

object JsonAvroScheme {
  def apply[T <: GenericRecord](schema: Schema) = new JsonAvroScheme[T](schema)
}

class JsonAvroScheme[T <: GenericRecord](schema: Schema)extends Scheme[T] with AvroScheme[T] {
  def decode(bytes: Array[Byte]): TraversableOnce[T] = {
    implicit val avroInj = GenericAvroCodecs.toJson[T](schema)
    implicit val inj = connect[T, String, Array[Byte]]
    decodeRecord(bytes)
  }
}




