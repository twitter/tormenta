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
import com.twitter.tormenta.scheme.avro.BufferableScheme

object GenericAvroScheme {
  def apply[T <: GenericRecord](schema: Schema) = BufferableScheme(GenericAvroCodecs[T](schema))
}

object BinaryAvroScheme {
  def apply[T <: GenericRecord](schema: Schema) = BufferableScheme(GenericAvroCodecs.toBinary[T](schema))
}

object JsonAvroScheme {
  def apply[T <: GenericRecord](schema: Schema) =
    BufferableScheme(GenericAvroCodecs.toJson[T](schema) andThen connect[String, Array[Byte]])
}

