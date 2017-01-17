/*
 Copyright 2016 Twitter, Inc.

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

package com.twitter.tormenta.spout

import org.apache.storm.task.TopologyContext
import org.apache.storm.topology.IRichSpout
import com.twitter.tormenta.scheme.Scheme
import org.scalatest._

class CapturingSchemeSpout extends SchemeSpout[Int] {
  var transformer: Scheme[Int] => Scheme[_] = _
  var fn: TopologyContext => Unit = _
  override def getSpout[R](
    transformer: Scheme[Int] => Scheme[R],
    fn: => TopologyContext => Unit): IRichSpout = {
    this.transformer = transformer
    this.fn = fn
    null
  }

  val scheme = Scheme(_ => Seq(1, 2, 3))

  def transformed: Seq[_] = transformer(scheme).decode(null).toSeq
}

class SchemeSpoutTest extends WordSpec with Matchers {
  "SchemeSpout" should {
    "calls open hook" in {
      var hookCalled = false

      val spout = new CapturingSchemeSpout

      val hookedSpout = spout.openHook { _ =>
        assert(!hookCalled)
        hookCalled = true
      }

      val richSpout = hookedSpout.getSpout
      spout.fn(null)

      assert(hookCalled)
      assert(spout.transformed == Seq(1, 2, 3))
    }

    "calls multiple open hooks" in {
      var hook1 = false
      var hook2 = false
      var hook3 = false

      val spout = new CapturingSchemeSpout

      val hookedSpout =
        spout
          .openHook { _ => assert(!hook1); hook1 = true }
          .openHook { _ => assert(!hook2); hook2 = true }
          .openHook { _ => assert(!hook3); hook3 = true }

      val richSpout = hookedSpout.getSpout
      spout.fn(null)

      assert(hook1)
      assert(hook2)
      assert(hook3)
      assert(spout.transformed == Seq(1, 2, 3))
    }

    "calls open hooks when flatMaps are present" in {
      var hook1 = false
      var hook2 = false
      var hook3 = false
      var hook4 = false

      val spout = new CapturingSchemeSpout

      val hookedSpout =
        spout
          .openHook { _ => assert(!hook1); hook1 = true }
          .flatMap { x =>
            if (x % 2 == 1) Some(x) else None
          }
          .flatMap { x => Some(x) }
          .openHook { _ => assert(!hook2); hook2 = true }
          .flatMap { x => Seq(x, 0, x) }
          .openHook { _ => assert(!hook3); hook3 = true }
          .openHook { _ => assert(!hook4); hook4 = true }

      val richSpout = hookedSpout.getSpout
      spout.fn(null)

      assert(hook1)
      assert(hook2)
      assert(hook3)
      assert(hook4)
      assert(spout.transformed == Seq(1, 0, 1, 3, 0, 3))
    }
  }
}
