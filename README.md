## Tormenta [![Build Status](https://secure.travis-ci.org/twitter/tormenta.png)](http://travis-ci.org/twitter/tormenta)

Scala extensions for the [Storm](https://github.com/nathanmarz/storm) distributed computation system. Tormenta adds a type-safe wrapper over Storm's Kafka and Kestrel spouts. This type safety allows the user to push mapping and filtering transformations down to the level of the spout itself:

```scala
// produces strings:
val scheme: ScalaScheme[String] = new StringScheme

// produces integers w/ string length:
val mappedScheme: ScalaScheme[Int] = scheme map { _.length }

// filters out all tuples less than 5:
val filteredScheme: ScalaScheme[Int] = mappedScheme filter { _ > 5 }

// produces lengths for input strings > length of 5
val spout: KestrelSpout[Int] = new KestrelSpout(filteredScheme, hostSeq, "spout")
```

To use a `ScalaSpout[T]` in a Storm topology, call the `getSpout` method:

```scala
topologyBuilder.setSpout("spoutName", spout.getSpout, 10)
```

Now you're cooking with gas.

## Maven

Current version is 0.1.1. groupid="com.twitter" artifact="tormenta_2.9.2".

## Authors

* Oscar Boykin <https://twitter.com/posco>
* Sam Ritchie <https://twitter.com/sritchie>

## License

Copyright 2012 Twitter, Inc.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
