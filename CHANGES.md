# tormenta #

### Version 0.5.3 ###

* Add `tormenta-avro` module: https://github.com/twitter/tormenta/pull/36

### Version 0.5.2 ###

* Break out `tormenta-kestrel` and `tormenta-kafka`: https://github.com/twitter/tormenta/pull/31
* Add mailing list to README: https://github.com/twitter/tormenta/pull/34
* Add proper `TraversableSpout` (usable within topologies) and first test: https://github.com/twitter/tormenta/pull/35

### Version 0.5.1 ###

* Fixed missing `open` call to `TwitterSpout`
* Moved `KafkaSpout` to `tormenta-kafka`
* Moved `KestrelSpout` to `tormenta-kestrel`

### Version 0.5.0 ###

* tormenta becomes tormenta-core
* Add tormenta-twitter
* Spouts gain map, filter, flatMap
* ScalaSpout -> Spout
* ScalaScheme -> Scheme
* Schemes and Spouts now have proper variance

### Version 0.4.0 ###

* More KafkaSpout customization.

### Version 0.3.2 ###

* Add ability to customize zkRoot in KafkaSpout.

### Version 0.3.1 ###

* Republish using jdk6.

### Version 0.3.0 ###

* Remove bijection dependency and `BijectionScheme` (This should just be a FunctionScheme anyway.)
* Add scala 2.10 cross build

### Version 0.2.1 ###

* Added Conjars repo back in. Depend on public version of `bijection`.

### Version 0.2.0 ###

* Modified `ScalaKryoFactory` for easier extensions.
* Use `bijection-core` instead of `util-core`

### Version 0.1.1 ###

* Added base `ScalaKryoFactory` implementation.

### Version 0.0.2 ###

* Added `ScalaScheme` and `ScalaSpout` with `chill` dependency for serialization.

### Version 0.0.1 ###

* Added `ScalaInterop` java class for DRPC.
