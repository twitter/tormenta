import ReleaseTransformations._
import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform._

val avroVersion = "1.7.5"
val bijectionVersion = "0.9.5"
val chillVersion = "0.8.3"
val scalacheckVersion = "1.13.4"
val scalaTestVersion = "3.0.1"
val slf4jVersion = "1.6.6"
val stormVersion = "1.0.2"
val twitter4jVersion = "3.0.3"

val extraSettings = mimaDefaultSettings ++ scalariformSettings

def ciSettings: Seq[Def.Setting[_]] =
  if (sys.env.getOrElse("TRAVIS", "false").toBoolean) Seq(
    ivyLoggingLevel := UpdateLogging.Quiet,
    logLevel in Global := Level.Warn,
    logLevel in Compile := Level.Warn,
    logLevel in Test := Level.Info
  ) else Seq.empty[Def.Setting[_]]

val sharedSettings = extraSettings ++ ciSettings ++ Seq(
  organization := "com.twitter",
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.1"),
  javacOptions ++= Seq("-source", "1.6", "-target", "1.6"),
  javacOptions in doc := Seq("-source", "1.6"),
  libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-api" % slf4jVersion,
    "org.apache.storm" % "storm-core" % stormVersion % "provided",
    "org.scalacheck" %% "scalacheck" % scalacheckVersion % "test",
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
  ),
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-Yresolve-term-conflict:package"),
  resolvers ++= Seq(
    Opts.resolver.sonatypeSnapshots,
    Opts.resolver.sonatypeReleases
  ),

  parallelExecution in Test := false,

  scalacOptions ++= Seq("-unchecked", "-deprecation", "-language:implicitConversions", "-language:higherKinds", "-language:existentials"),

  scalacOptions ++= (if (scalaVersion.value startsWith "2.10")
      Seq("-Xdivergence211")
    else
      Seq()),

  // Publishing options:
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  releaseVersionBump := sbtrelease.Version.Bump.Minor, // need to tweak based on mima results
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { x => false },

  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion,
    ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
    pushChanges),

  publishTo := Some(
      if (version.value.trim.toUpperCase.endsWith("SNAPSHOT"))
        Opts.resolver.sonatypeSnapshots
      else
        Opts.resolver.sonatypeStaging
    ),

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
  None
// Uncomment after next release
//  Some(subProj)
//    .filterNot(unreleasedModules.contains(_))
//    .map { s => "com.twitter" % ("tormenta-" + s + "_2.10") % "0.11.0" }

/**
  * Empty this each time we publish a new version (and bump the minor number)
  */
val ignoredABIProblems = {
  import com.typesafe.tools.mima.core._
  import com.typesafe.tools.mima.core.ProblemFilters._
  Seq(
    exclude[ReversedMissingMethodProblem](
      "com.twitter.tormenta.spout.SchemeSpout.openHook"
    )
  )
}

lazy val noPublishSettings = Seq(
    publish := (),
    publishLocal := (),
    test := (),
    publishArtifact := false
  )

lazy val tormenta = Project(
  id = "tormenta",
  base = file("."),
  settings = sharedSettings)
  .settings(noPublishSettings)
  .aggregate(
  tormentaCore,
  tormentaKafka,
  tormentaTwitter,
  tormentaAvro
)

def module(name: String) = {
  val id = "tormenta-%s".format(name)
  Project(id = id, base = file(id), settings = sharedSettings ++ Seq(
    Keys.name := id,
    mimaPreviousArtifacts := youngestForwardCompatible(name).toSet,
    mimaBinaryIssueFilters ++= ignoredABIProblems
  ))
}

lazy val tormentaCore = module("core").settings(
  libraryDependencies += "com.twitter" %% "chill" % chillVersion
  exclude("com.esotericsoftware.kryo", "kryo")
)

lazy val tormentaTwitter = module("twitter").settings(
  libraryDependencies += "org.twitter4j" % "twitter4j-stream" % twitter4jVersion
).dependsOn(tormentaCore % "test->test;compile->compile")

lazy val tormentaKafka = module("kafka").settings(
  libraryDependencies += "org.apache.storm" % "storm-kafka" % stormVersion
).dependsOn(tormentaCore % "test->test;compile->compile")

lazy val tormentaAvro = module("avro").settings(
  libraryDependencies ++= Seq(
    "org.apache.avro" % "avro" % avroVersion,
    "com.twitter" %% "bijection-core" % bijectionVersion,
    "com.twitter" %% "bijection-avro" % bijectionVersion)
).dependsOn(tormentaCore % "test->test;compile->compile")
