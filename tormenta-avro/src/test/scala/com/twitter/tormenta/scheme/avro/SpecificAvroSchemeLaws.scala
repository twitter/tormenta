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
import com.twitter.bijection.Injection
import com.twitter.bijection.avro.AvroCodecs

import com.twitter.bijection.Injection._
import avro.FiscalRecord
import com.twitter.tormenta.scheme.avro.specific.{JsonAvroScheme, BinaryAvroScheme, SpecificAvroScheme}
import org.apache.avro.Schema
import com.twitter.tormenta.scheme.Scheme

/**
 * @author Mansur Ashraf
 * @since 9/15/13
 */
object SpecificAvroSchemeLaws extends Properties("SpecificAvroScheme") with BaseAvroProperties {

  val testSchema = new Schema.Parser().parse( """{
                                                   "type":"record",
                                                   "name":"FiscalRecord",
                                                   "namespace":"avro",
                                                   "fields":[
                                                      {
                                                         "name":"calendarDate",
                                                         "type":"string"
                                                      },
                                                      {
                                                         "name":"fiscalWeek",
                                                         "type":[
                                                            "int",
                                                            "null"
                                                         ]
                                                      },
                                                      {
                                                         "name":"fiscalYear",
                                                         "type":[
                                                            "int",
                                                            "null"
                                                         ]
                                                      }
                                                   ]
                                                }""")

  def buildSpecificAvroRecord(i: (String, Int, Int)): FiscalRecord = {
    FiscalRecord.newBuilder()
      .setCalendarDate(i._1)
      .setFiscalWeek(i._2)
      .setFiscalYear(i._3)
      .build()
  }

  implicit val testSpecificRecord = arbitraryViaFn {
    is: (String, Int, Int) => buildSpecificAvroRecord(is)
  }
  implicit val failedGenericRecord = buildSpecificAvroRecord("failed", -99, -99)

  def roundTripsSpecificRecord[S <: Scheme[FiscalRecord]](implicit injection: Injection[FiscalRecord, Array[Byte]], scheme: S) = {
    isAvroRecordDecoded[FiscalRecord]
  }

  def simulateSpecificRecordFailure[S <: Scheme[FiscalRecord]](implicit injection: Injection[FiscalRecord, Array[Byte]], scheme: S) = {
    isAvroRecordNotDecoded[FiscalRecord]
  }


  property("round trips Specific Record using Specific Avro Scheme") = {
    implicit val inj = AvroCodecs[FiscalRecord]
    implicit val scheme = SpecificAvroScheme[FiscalRecord]
    roundTripsSpecificRecord
  }

  property("round trips Specific Record using Binary Avro Scheme") = {
    implicit val inj = AvroCodecs.toBinary[FiscalRecord]
    implicit val scheme = BinaryAvroScheme[FiscalRecord]
    roundTripsSpecificRecord
  }

  property("round trips Specific Record using Json Avro Scheme") = {
    implicit val avinj = AvroCodecs.toJson[FiscalRecord](testSchema)
    implicit val scheme = JsonAvroScheme[FiscalRecord](testSchema)
    implicit val inj = connect[FiscalRecord, String, Array[Byte]]
    roundTripsSpecificRecord
  }

  property("Simulates Specific Avro Scheme failure") = {
    implicit val jinj = AvroCodecs.toJson[FiscalRecord](testSchema) //passing wrong injection to produce incorrect bytes
    implicit val inj = connect[FiscalRecord, String, Array[Byte]]
    implicit val scheme = BinaryAvroScheme[FiscalRecord].withHandler(t=>List(failedGenericRecord))
    simulateSpecificRecordFailure
  }

  property("simulate Binary Avro Scheme") = {
    implicit val inj = AvroCodecs[FiscalRecord] //passing wrong injection to produce incorrect bytes
    implicit val scheme = BinaryAvroScheme[FiscalRecord].withHandler(t=>List(failedGenericRecord))
    simulateSpecificRecordFailure
  }

  property("round trips Specific Record using Json Avro Scheme") = {
    implicit val scheme = JsonAvroScheme[FiscalRecord](testSchema).withHandler(t=>List(failedGenericRecord))
    implicit val inj = AvroCodecs[FiscalRecord] //passing wrong injection to produce incorrect bytes
    simulateSpecificRecordFailure
  }

}
