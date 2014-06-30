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
 *  @author Ian O Connell
 */

object SchemeTransformer {
  def identity[T] = SchemeTransformer[T, T](Some(_))
  def apply[T, U](baseFn: T => TraversableOnce[U]): SchemeTransformer[T, U] = new SchemeTransformer[T, U] {
    def apply(t: T) = baseFn(t)
  }
}

// This is Isomorphic to FlatMappedFn in Scalding
// https://github.com/twitter/scalding/blob/develop/scalding-core/src/main/scala/com/twitter/scalding/typed/FlatMappedFn.scala
trait SchemeTransformer[-T, +U] {
  def apply(t: T): TraversableOnce[U]

  def filter(fn: U => Boolean): SchemeTransformer[T, U] =
    SchemeTransformer(apply(_).filter(fn))

  def map[R](fn: U => R): SchemeTransformer[T, R] =
    SchemeTransformer(apply(_).map(fn))

  def collect[R](fn: PartialFunction[U, R]): SchemeTransformer[T, R] =
    SchemeTransformer(apply(_).filter(fn.isDefinedAt(_)).map(fn(_)))

  def flatMap[R](fn: U => TraversableOnce[R]): SchemeTransformer[T, R] =
    SchemeTransformer(apply(_).flatMap(fn))
}
