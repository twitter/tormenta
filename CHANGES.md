# tormenta #

### Version 0.8.0 ###
* Use scalainform: https://github.com/twitter/tormenta/pull/54
* Modifications to Spout code to allow for metric registration: https://github.com/twitter/tormenta/pull/53

### Version 0.7.0 ###
* Fix semantic version (minor API change affected binary compatibiltiy of KafkaSpout)

### Version 0.6.1 ###
* Make the forceStartOffset programmable: https://github.com/twitter/tormenta/pull/51

### Version 0.6.0 ###
* Add logging, make storm provided: https://github.com/twitter/tormenta/pull/47
* Upgrades Avro/Bijection-avro: https://github.com/twitter/tormenta/pull/49

### Version 0.5.4 ###
* Add Scheme "withHandler" Test: https://github.com/twitter/tormenta/pull/38
* Fixes Implicit resolution in GenericAvroSchemeLaws: https://github.com/twitter/tormenta/pull/40
* Changed TopologyTest to object from class: https://github.com/twitter/tormenta/pull/42
* Adds AvroTraversableSpouts for testing Avro Record with SummingBird: https://github.com/twitter/tormenta/pull/43
* Spout metrics: https://github.com/twitter/tormenta/pull/46
* Move to sbt 0.13: https://github.com/twitter/tormenta/pull/45

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
