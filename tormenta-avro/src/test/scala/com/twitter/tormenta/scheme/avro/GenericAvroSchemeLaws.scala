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

package com.twitter.tormenta.scheme.avro

import org.scalacheck.Properties
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericData, GenericRecord}
import com.twitter.bijection.Injection
import com.twitter.bijection.avro.AvroCodecs
import com.twitter.tormenta.scheme.avro.generic.{JsonAvroScheme, BinaryAvroScheme, GenericAvroScheme}
import com.twitter.bijection.Injection._
import com.twitter.tormenta.scheme.Scheme
import com.twitter.tormenta.AvroTestHelper
import scala.util.{Success, Failure}

/**
 * @author Mansur Ashraf
 * @since 9/14/13
 */
object GenericAvroSchemeLaws extends Properties("GenericAvroScheme") with BaseAvroProperties with AvroTestHelper {

  implicit val testGenericRecord = arbitraryViaFn {
    is: (String, Int, Int) => buildGenericAvroRecord(is)
  }

  implicit val failedGenericRecord = buildGenericAvroRecord("failed", -99, -99)

  def roundTripsGenericRecord[S <: Scheme[GenericRecord]](implicit injection: Injection[GenericRecord, Array[Byte]], scheme: S) = {
    isAvroRecordDecoded[GenericRecord]
  }

  def simulateGenericRecordFailure[S <: Scheme[GenericRecord]](implicit injection: Injection[GenericRecord, Array[Byte]], scheme:S) = {
    isAvroRecordNotDecoded[GenericRecord]
  }

  property("round trips Generic Record using Generic Avro Scheme") = {
    implicit val inj = AvroCodecs[GenericRecord](testSchema)
    implicit val scheme = GenericAvroScheme[GenericRecord](testSchema)
    roundTripsGenericRecord
  }

  property("round trips Generic Record using Binary Avro Scheme") = {
    implicit val inj = AvroCodecs.toBinary[GenericRecord](testSchema)
    implicit val scheme = BinaryAvroScheme[GenericRecord](testSchema)
    roundTripsGenericRecord
  }

  property("round trips Generic Record using Json Avro Scheme") = {
    implicit val avinj = AvroCodecs.toJson[GenericRecord](testSchema)
    implicit val scheme = JsonAvroScheme[GenericRecord](testSchema)
    implicit val inj = connect[GenericRecord, String, Array[Byte]]
    roundTripsGenericRecord
  }

  property("Simulates Generic Avro Scheme failure") = {
    implicit val jinj = AvroCodecs.toJson[GenericRecord](testSchema) //passing wrong injection to produce incorrect bytes
    implicit val inj = connect[GenericRecord, String, Array[Byte]]
    implicit val scheme = BinaryAvroScheme[GenericRecord](testSchema).withHandler(t=> Success(List(failedGenericRecord)))
    simulateGenericRecordFailure
  }

    property("simulate Binary Avro Scheme") = {
      implicit val inj = AvroCodecs[GenericRecord](testSchema)   //passing wrong injection to produce incorrect bytes
      implicit val scheme = BinaryAvroScheme[GenericRecord](testSchema).withHandler(t=> Success(List(failedGenericRecord)))
      simulateGenericRecordFailure
    }

    property("round trips Generic Record using Json Avro Scheme") = {
      implicit val scheme = JsonAvroScheme[GenericRecord](testSchema).withHandler(t=> Success(List(failedGenericRecord)))
      implicit val inj = AvroCodecs[GenericRecord](testSchema)     //passing wrong injection to produce incorrect bytes
      simulateGenericRecordFailure
    }


}
