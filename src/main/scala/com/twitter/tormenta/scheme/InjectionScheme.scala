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

import com.twitter.chill.MeatLocker
import com.twitter.bijection.Injection

/**
 *  @author Oscar Boykin
 *  @author Sam Ritchie
 */

object InjectionScheme {
  def apply[T](implicit bijection: Injection[T, Array[Byte]]) = new InjectionScheme(bijection)
}

class InjectionScheme[T](@transient injection: Injection[T, Array[Byte]]) extends ScalaScheme[T] {
  val injectionBox = MeatLocker(injection)
  override def decode(bytes: Array[Byte]) = injectionBox.get.invert(bytes)
}
