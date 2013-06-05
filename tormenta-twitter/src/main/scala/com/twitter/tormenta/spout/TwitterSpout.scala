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

package com.twitter.tormenta.spout

import com.twitter.tormenta.scheme.ScalaScheme
import twitter4j.{ Status, TwitterStreamFactory }

/**
  * ScalaSpout for Twitter's streaming API.
  *
  * @author Sam Ritchie
  */

object TwitterSpout {
  import TwitterStreamingSpout.{ FIELD_NAME, QUEUE_LIMIT }

  def apply(factory: TwitterStreamFactory, limit: Int = QUEUE_LIMIT, fieldName: String = FIELD_NAME): ScalaSpout[Status] =
    new ScalaSpout[Status] {
      override val parallelism = 1
      override def getSpout[R](transformer: ScalaScheme[Status] => ScalaScheme[R]) = {
        TwitterStreamingSpout.of[R](factory, limit, fieldName)(transformer)
      }
    }
}
