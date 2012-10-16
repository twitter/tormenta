name := "tormenta"

version := "0.0.3-SNAPSHOT"

organization := "com.twitter"

scalaVersion := "2.9.2"

scalacOptions += "-Yresolve-term-conflict:package"

// Use ScalaCheck
resolvers ++= Seq(
  "sonatype-snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "sonatype-releases"  at "http://oss.sonatype.org/content/repositories/releases",
  "Clojars Repository" at "http://clojars.org/repo",
  "Conjars Repository" at "http://conjars.org/repo",
  "Twitter Maven" at "http://maven.twttr.com",
  "Twitter SVN Maven" at "https://svn.twitter.biz/maven-public"
)

libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
  "org.scala-tools.testing" % "specs_2.9.0-1" % "1.6.8" % "test",
  "storm" % "storm" % "0.8.0",
  "storm" % "storm-kafka" % "0.7.3-scala292-dynamic-SNAPSHOT",
  "storm" % "storm-kestrel" % "0.7.2-snap3",
  "com.twitter" % "util-core" % "5.3.7",
  "com.twitter" %% "chill" % "0.0.2"
)

parallelExecution in Test := true

// Publishing options:

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val url = "http://artifactory.local.twitter.com/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at (url + "libs-snapshots-local"))
  else
    Some("releases" at (url + "libs-releases-local"))
}

publishArtifact in Test := false

pomIncludeRepository := { x => false }

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
