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

package com.twitter.tormenta.serialization

import backtype.storm.serialization.IKryoFactory
import com.esotericsoftware.kryo.{ Kryo, Serializer }
import com.twitter.chill.{ KryoSerializer, ObjectSerializer }
import java.util.{ HashMap, Map => JMap }
import org.objenesis.strategy.StdInstantiatorStrategy

/**
 *  @author Oscar Boykin
 *  @author Sam Ritchie
 */

class ScalaKryoFactory extends IKryoFactory {
  override def getKryo(conf: JMap[_,_]): Kryo = getKryo
  override def preRegister(k: Kryo, conf: JMap[_,_]) { populate(k) }
  override def postRegister(k: Kryo, conf: JMap[_,_]) { }
  override def postDecorate(k: Kryo, conf: JMap[_,_]) { }

  def getKryo() = {
    val k = new Kryo {
      lazy val objSer = new ObjectSerializer[AnyRef]
      override def newDefaultSerializer(cls: Class[_]): Serializer[_] = {
        if(objSer.accepts(cls))
          objSer
        else
          super.newDefaultSerializer(cls)
      }
    }
    k.setInstantiatorStrategy(new StdInstantiatorStrategy());
    k
  }

  def populate(k: Kryo) {
    // Register all the chill serializers:
    KryoSerializer.registerAll(k)

    //Add commonly used types with Fields serializer:
    registeredTypes.foreach { cls => k.register(cls) }
  }

  // returns true if the supplied class has already been registered
  // with the supplied kryo instance, false otherwise.
  def alreadyRegistered(k: Kryo, klass: Class[_]) =
    k.getClassResolver.getRegistration(klass) != null

  // TODO: this was cargo-culted from
  // [Scalding](https://github.com/twitter/scalding/blob/develop/src/main/scala/com/twitter/scalding/serialization/KryoHadoop.scala),
  // which in turn cargo-culted from Spark.
  //
  // Types to pre-register.
  def registeredTypes: List[Class[_]] = {
    List(
      // Arrays
      Array(1), Array(1.0), Array(1.0f), Array(1L), Array(""), Array(("", "")),
      Array(new java.lang.Object), Array(1.toByte), Array(true), Array('c'),
      // Options and Either
      Some(1), Left(1), Right(1)
    ).map { _.getClass }
  }
}
