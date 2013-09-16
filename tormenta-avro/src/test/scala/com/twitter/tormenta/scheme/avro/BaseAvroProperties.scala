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

import org.scalacheck.Arbitrary
import com.twitter.bijection.Injection
import scala.math.Equiv
import org.scalacheck.Prop._
import org.apache.avro.generic.GenericRecord
import org.apache.avro.specific.SpecificRecordBase
import scala.collection.JavaConverters._
import com.twitter.tormenta.scheme.Scheme

/**
 * @author Mansur Ashraf
 * @since 9/14/13
 */
trait BaseAvroProperties {

  implicit val genericRecordEq = new Equiv[GenericRecord] {
    def equiv(x: GenericRecord, y: GenericRecord): Boolean = x == y
  }

  implicit val specificRecordEq = new Equiv[SpecificRecordBase] {
    def equiv(x: SpecificRecordBase, y: SpecificRecordBase): Boolean = x == y
  }

  def arbitraryViaFn[A, B](fn: A => B)(implicit arb: Arbitrary[A]): Arbitrary[B] =
    Arbitrary {
      arb.arbitrary.map {
        fn
      }
    }

  def isAvroRecordDecoded[A](implicit arba: Arbitrary[A], scheme: Scheme[A],
                             inj: Injection[A, Array[Byte]], eqa: Equiv[A]) =
    forAll {
      (a: A) =>
        val b = inj(a)
        val deserialize = scheme.deserialize(b)
        val c = deserialize.asScala
        !c.isEmpty && c.size == 1 && eqa.equiv(c.head.get(0).asInstanceOf[A], a)
    }

  def isAvroRecordNotDecoded[A](implicit arba: Arbitrary[A], scheme: Scheme[A],
                                failedRecord: A, inj: Injection[A, Array[Byte]], eqa: Equiv[A]) =
    forAll {
      (a: A) =>
        val b = inj(a)
        val c = scheme.deserialize(b).asScala
        !c.isEmpty && c.size == 1 && eqa.equiv(c.head.get(0).asInstanceOf[A], failedRecord)
    }
}
