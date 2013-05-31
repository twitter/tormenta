package tormenta

import sbt._
import Keys._
import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings
import com.typesafe.tools.mima.plugin.MimaKeys.previousArtifact

object TormentaBuild extends Build {
  val extraSettings =
    Project.defaultSettings ++ mimaDefaultSettings

  def ciSettings: Seq[Project.Setting[_]] =
    if (sys.env.getOrElse("TRAVIS", "false").toBoolean) Seq(
      ivyLoggingLevel := UpdateLogging.Quiet,
      logLevel in Global := Level.Warn,
      logLevel in Compile := Level.Warn,
      logLevel in Test := Level.Info
    ) else Seq.empty[Project.Setting[_]]


  val sharedSettings = extraSettings ++ ciSettings ++ Seq(
    organization := "com.twitter",
    crossScalaVersions := Seq("2.9.2", "2.10.0"),
    javacOptions ++= Seq("-source", "1.6", "-target", "1.6"),
    javacOptions in doc := Seq("-source", "1.6"),
    libraryDependencies ++= Seq(
      "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
      "org.scala-tools.testing" %% "specs" % "1.6.9" % "test"
    ),
    resolvers ++= Seq(
      Opts.resolver.sonatypeSnapshots,
      Opts.resolver.sonatypeReleases,
      "Clojars Repository" at "http://clojars.org/repo"
    ),

    parallelExecution in Test := true,

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

  /**
    * This returns the youngest jar we released that is compatible
    * with the current.
    */
  val unreleasedModules = Set[String]("twitter")

  def youngestForwardCompatible(subProj: String) =
    Some(subProj)
      .filterNot(unreleasedModules.contains(_))
      .map { s => "com.twitter" % ("tormenta-" + s + "_2.9.2") % "0.3.0" }

  val algebirdVersion = "0.1.13"
  val bijectionVersion = "0.4.0"

  lazy val tormenta = Project(
    id = "tormenta",
    base = file("."),
    settings = sharedSettings ++ DocGen.publishSettings
    ).settings(
    test := { },
    publish := { }, // skip publishing for this root project.
    publishLocal := { }
  ).aggregate(
    tormentaCore
  )

  def module(name: String) = {
    val id = "tormenta-%s".format(name)
    Project(id = id, base = file(id), settings = sharedSettings ++ Seq(
      Keys.name := id,
      previousArtifact := youngestForwardCompatible(name))
    )
  }

  lazy val tormentaCore = module("core").settings(
    libraryDependencies ++= Seq(
      "storm" % "storm" % "0.9.0-wip9",
      "storm" % "storm-kafka" % "0.9.0-wip6-scala292-multischeme",
      "storm" % "storm-kestrel" % "0.9.0-wip5-multischeme",
      "com.twitter" %% "chill" % "0.2.1"
    )
  )
}
