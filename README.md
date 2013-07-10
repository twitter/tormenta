## Tormenta [![Build Status](https://secure.travis-ci.org/twitter/tormenta.png)](http://travis-ci.org/twitter/tormenta)

Scala extensions for the [Storm](https://github.com/nathanmarz/storm) distributed computation system. Tormenta adds a type-safe wrapper over Storm's Kafka and Kestrel spouts. This type safety allows the user to push mapping and filtering transformations down to the level of the spout itself:

```scala
// produces strings:
val scheme: Scheme[String] = new StringScheme

// produces integers w/ string length:
val mappedScheme: Scheme[Int] = scheme.map(_.length)

// filters out all tuples less than 5:
val filteredScheme: Scheme[Int] = mappedScheme.filter(_ > 5)

// produces lengths for input strings > length of 5
val spout: KestrelSpout[Int] = new KestrelSpout(filteredScheme, hostSeq, "spout")
```

To use a `Spout[T]` in a Storm topology, call the `getSpout` method:

```scala
topologyBuilder.setSpout("spoutName", spout.getSpout, 10)
```

Now you're cooking with gas.

## Maven

Tormenta modules are available on Maven Central. The current groupid and version for all modules is, respectively, `"com.twitter"` and  `0.5.1`.

Current published artifacts are

* `tormenta-core_2.9.2`
* `tormenta-core_2.10`
* `tormenta-twitter_2.9.2`
* `tormenta-twitter_2.10`

The suffix denotes the scala version.

## Authors

* Oscar Boykin <https://twitter.com/posco>
* Sam Ritchie <https://twitter.com/sritchie>

## License

Copyright 2012 Twitter, Inc.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
