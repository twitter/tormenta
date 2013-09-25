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
package com.twitter.tormenta.spout.avro

import com.twitter.tormenta.spout.TraversableSpout
import org.apache.avro.generic.GenericRecord
import org.apache.avro.specific.SpecificRecordBase

/**
 * @author Mansur Ashraf
 * @since 9/24/13
 */
class GenericRecordTraversableSpout(items: TraversableOnce[GenericRecord], fieldName: String) extends TraversableSpout[GenericRecord](items, fieldName)

object GenericRecordTraversableSpout {
  def apply(items: TraversableOnce[GenericRecord], fieldName: String = "items") = new GenericRecordTraversableSpout(items, fieldName)
}

class SpecificRecordTraversableSpout[T <: SpecificRecordBase](items: TraversableOnce[T], fieldName: String) extends TraversableSpout[T](items, fieldName)

object SpecificRecordTraversableSpout {
  def apply[T <: SpecificRecordBase](items: TraversableOnce[T], fieldName: String = "items") = new SpecificRecordTraversableSpout(items, fieldName)
}
