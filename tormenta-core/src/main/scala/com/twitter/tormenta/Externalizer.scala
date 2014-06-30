/*
Copyright 2014 Twitter, Inc.

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

package com.twitter.tormenta

import com.twitter.chill.{ Externalizer => ChillExtern, KryoInstantiator, ScalaKryoInstantiator }

/**
 * *
 * Need to create our own Externalizer to avoid calling chill Externalizer that uses setReferences,
 * which creates a Kryo version conflict (see https://github.com/twitter/chill/issues/173).
 *
 * TODO: https://github.com/twitter/tormenta/issues/56
 * This has been fixed in storm, but internal Twitter dependencies on the pre-apache classpath
 * prevents the upgrade here. See https://issues.apache.org/jira/browse/STORM-263
 */

object Externalizer {
  def apply[T](t: T): Externalizer[T] = {
    val x = new Externalizer[T]
    x.set(t)
    x
  }
}

class Externalizer[T] extends ChillExtern[T] {
  // Storm is set on 2.17, hack to avoid setReferences
  override protected def kryo: KryoInstantiator = new ScalaKryoInstantiator
}
