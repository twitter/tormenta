name := "tormenta"

version := "0.3.1"

organization := "com.twitter"

crossScalaVersions := Seq("2.9.2", "2.10.0")

javacOptions ++= Seq("-target", "1.6", "-source", "1.6")

scalacOptions += "-Yresolve-term-conflict:package"

resolvers ++= Seq(
  "sonatype-snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "sonatype-releases"  at "http://oss.sonatype.org/content/repositories/releases",
  "Clojars Repository" at "http://clojars.org/repo",
  "Conjars Repository" at "http://conjars.org/repo"
)

libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
  "org.scala-tools.testing" %% "specs" % "1.6.9" % "test",
  "storm" % "storm" % "0.9.0-wip9",
  "storm" % "storm-kafka" % "0.9.0-wip6-scala292-multischeme",
  "storm" % "storm-kestrel" % "0.9.0-wip5-multischeme",
  "com.twitter" %% "chill" % "0.2.1"
)

parallelExecution in Test := true

// Publishing options:

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
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
