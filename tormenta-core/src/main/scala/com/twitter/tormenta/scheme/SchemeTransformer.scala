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

package com.twitter.tormenta.scheme

import backtype.storm.tuple.{ Fields, Values }
import backtype.storm.spout.MultiScheme
import java.util.{ List => JList }
import scala.collection.JavaConverters._
import java.io.Serializable
import org.slf4j.LoggerFactory

/**
 *  @author Oscar Boykin
 *  @author Sam Ritchie
 */

object SchemeTransformer {
  def identity[T] = SchemeTransformer[T, T](Some(_))
}

case class SchemeTransformer[-T, U](baseFn: T => TraversableOnce[U]) {
  def apply(t: T): TraversableOnce[U] = baseFn(t)

  def filter(fn: U => Boolean): SchemeTransformer[T, U] =
    new SchemeTransformer(baseFn.andThen(_.filter(fn)))

  def map[R](fn: U => R): SchemeTransformer[T, R] =
    new SchemeTransformer(baseFn.andThen(_.map(fn)))

  def flatMap[R](fn: U => TraversableOnce[R]): SchemeTransformer[T, R] =
    new SchemeTransformer(baseFn.andThen(_.flatMap(fn)))

}
