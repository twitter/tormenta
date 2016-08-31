## Tormenta [![Build Status](https://secure.travis-ci.org/twitter/tormenta.png)](http://travis-ci.org/twitter/tormenta)

Scala extensions for the [Storm](https://github.com/nathanmarz/storm) distributed computation system. Tormenta adds a type-safe wrapper over Storm's Kafka and Kestrel spouts. This type safety allows the user to push mapping and filtering transformations down to the level of the spout itself:

```scala
import com.twitter.tormenta.scheme._
import com.twitter.tormenta.spout._

// produces strings:
val scheme: Scheme[String] = Scheme { bytes => Some(new String(bytes)) }

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

## Documentation

To learn more and find links to tutorials and information around the web, check out the [Tormenta Wiki](https://github.com/twitter/tormenta/wiki).

The latest ScalaDocs are hosted on Tormenta's [Github Project Page](http://twitter.github.io/tormenta).

## Contact

Discussion occurs primarily on the [Tormenta mailing list](https://groups.google.com/forum/#!forum/tormenta-user). Issues should be reported on the [GitHub issue tracker](https://github.com/twitter/tormenta/issues).

## Get Involved + Code of Conduct
Pull requests and bug reports are always welcome!

We use a lightweight form of project governence inspired by the one used by Apache projects.
Please see [Contributing and Committership](https://github.com/twitter/analytics-infra-governance#contributing-and-committership) for our code of conduct and our pull request review process.
The TL;DR is send us a pull request, iterate on the feedback + discussion, and get a +1 from a [Committer](COMMITTERS.md) in order to get your PR accepted.

The current list of active committers (who can +1 a pull request) can be found here: [Committers](COMMITTERS.md)

A list of contributors to the project can be found here: [Contributors](https://github.com/twitter/tormenta/graphs/contributors)

## Maven

Tormenta modules are available on Maven Central. The current groupid and version for all modules is, respectively, `"com.twitter"` and  `0.11.1`.

Current published artifacts are

* `tormenta-core_2.11`
* `tormenta-core_2.10`
* `tormenta-kafka_2.11`
* `tormenta-kafka_2.10`
* `tormenta-kestrel_2.11`
* `tormenta-kestrel_2.10`
* `tormenta-twitter_2.11`
* `tormenta-twitter_2.10`

The suffix denotes the scala version.

## Authors

* Oscar Boykin <https://twitter.com/posco>
* Sam Ritchie <https://twitter.com/sritchie>

## License

Copyright 2015 Twitter, Inc.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
