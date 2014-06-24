package tormenta

import sbt._
import Keys._
import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings
import com.typesafe.tools.mima.plugin.MimaKeys.previousArtifact
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform._

object TormentaBuild extends Build {

  lazy val slf4jVersion = "1.6.6"
  lazy val stormVersion = "0.9.0-wip15"

  val extraSettings =
    Project.defaultSettings ++ mimaDefaultSettings ++ scalariformSettings

  def ciSettings: Seq[Project.Setting[_]] =
    if (sys.env.getOrElse("TRAVIS", "false").toBoolean) Seq(
      ivyLoggingLevel := UpdateLogging.Quiet,
      logLevel in Global := Level.Warn,
      logLevel in Compile := Level.Warn,
      logLevel in Test := Level.Info
    ) else Seq.empty[Project.Setting[_]]

  val sharedSettings = extraSettings ++ ciSettings ++ Seq(
    organization := "com.twitter",
    version := "0.7.0",
    scalaVersion := "2.9.3",
    crossScalaVersions := Seq("2.9.3", "2.10.0"),
    javacOptions ++= Seq("-source", "1.6", "-target", "1.6"),
    javacOptions in doc := Seq("-source", "1.6"),
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % slf4jVersion,
      "storm" % "storm" % stormVersion % "provided",
      "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
      "org.scala-tools.testing" %% "specs" % "1.6.9" % "test"
    ),
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-Yresolve-term-conflict:package"),
    resolvers ++= Seq(
      Opts.resolver.sonatypeSnapshots,
      Opts.resolver.sonatypeReleases,
      "Clojars Repository" at "http://clojars.org/repo",
      "Conjars Repository" at "http://conjars.org/repo"
    ),

    parallelExecution in Test := false,

    scalacOptions ++= Seq(Opts.compile.unchecked, Opts.compile.deprecation),

    // Publishing options:
    publishMavenStyle := true,

    publishArtifact in Test := false,

    pomIncludeRepository := { x => false },

    publishTo <<= version { v =>
      Some(
        if (v.trim.toUpperCase.endsWith("SNAPSHOT"))
          Opts.resolver.sonatypeSnapshots
        else
          Opts.resolver.sonatypeStaging
      )
    },

    pomExtra := (
      <url>https://github.com/twitter/tormenta</url>
      <licenses>
        <license>
          <name>Apache 2</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
          <distribution>repo</distribution>
          <comments>A business-friendly OSS license</comments>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:twitter/tormenta.git</url>
        <connection>scm:git:git@github.com:twitter/tormenta.git</connection>
      </scm>
      <developers>
        <developer>
          <id>oscar</id>
          <name>Oscar Boykin</name>
          <url>http://twitter.com/posco</url>
        </developer>
        <developer>
          <id>sritchie</id>
          <name>Sam Ritchie</name>
          <url>http://twitter.com/sritchie</url>
        </developer>
      </developers>)
  )

  lazy val formattingPreferences = {
   import scalariform.formatter.preferences._
   FormattingPreferences().
     setPreference(AlignParameters, false).
     setPreference(PreserveSpaceBeforeArguments, true)
  }

  /**
    * This returns the youngest jar we released that is compatible
    * with the current.
    */
  val unreleasedModules = Set[String]()

  def youngestForwardCompatible(subProj: String) =
    Some(subProj)
      .filterNot(unreleasedModules.contains(_))
      .map { s => "com.twitter" % ("tormenta-" + s + "_2.9.3") % "0.7.0" }

  lazy val tormenta = Project(
    id = "tormenta",
    base = file("."),
    settings = sharedSettings ++ DocGen.publishSettings
    ).settings(
    test := { },
    publish := { }, // skip publishing for this root project.
    publishLocal := { }
  ).aggregate(
    tormentaCore,
    tormentaKestrel,
    tormentaKafka,
    tormentaTwitter,
    tormentaAvro
  )

  def module(name: String) = {
    val id = "tormenta-%s".format(name)
    Project(id = id, base = file(id), settings = sharedSettings ++ Seq(
      Keys.name := id,
      previousArtifact := youngestForwardCompatible(name))
    )
  }

  lazy val tormentaCore = module("core").settings(
    libraryDependencies += "com.twitter" %% "chill" % "0.3.6"
  )

  lazy val tormentaTwitter = module("twitter").settings(
    libraryDependencies += "org.twitter4j" % "twitter4j-stream" % "3.0.3"
  ).dependsOn(tormentaCore % "test->test;compile->compile")

  lazy val tormentaKafka = module("kafka").settings(
    libraryDependencies += "storm" % "storm-kafka" % "0.9.0-wip6-scala292-multischeme"
  ).dependsOn(tormentaCore % "test->test;compile->compile")

  lazy val tormentaKestrel = module("kestrel").settings(
    libraryDependencies += "storm" % "storm-kestrel" % "0.9.0-wip5-multischeme"
  ).dependsOn(tormentaCore % "test->test;compile->compile")

  lazy val tormentaAvro = module("avro").settings(
    libraryDependencies ++= Seq(
      "org.apache.avro" % "avro" % "1.7.5",
      "com.twitter" %% "bijection-core" % "0.6.0",
      "com.twitter" %% "bijection-avro" % "0.6.0")
  ).dependsOn(tormentaCore % "test->test;compile->compile")
}
