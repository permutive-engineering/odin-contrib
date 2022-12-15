// https://typelevel.org/sbt-typelevel/faq.html#what-is-a-base-version-anyway
ThisBuild / tlBaseVersion := "0.0" // your current series x.y

ThisBuild / organization := "com.permutive"
ThisBuild / organizationName := "Permutive"
ThisBuild / startYear := Some(2022)
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  // your GitHub handle and name
  tlGitHubDev("janstenpickle", "Chris Jansen")
)

// publish to s01.oss.sonatype.org (set to true to publish to oss.sonatype.org instead)
ThisBuild / tlSonatypeUseLegacyHost := true

val Cats = "2.9.0"
val CatsEffect = "3.4.2"
val Odin = "0.13.0"

val Scala213 = "2.13.10"
ThisBuild / crossScalaVersions := Seq(Scala213, "3.1.1")
ThisBuild / scalaVersion := Scala213 // the default Scala

lazy val root = tlCrossRootProject.aggregate(slf4JBridge)

lazy val slf4JBridge = project
  .in(file("odin-slf4j-bridge"))
  .settings(
    name := "odin-slf4j-bridge",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % Cats,
      "org.typelevel" %% "cats-effect" % CatsEffect,
      "com.github.valskalla" %% "odin-core" % Odin,
      "org.slf4j" % "slf4j-api" % "1.7.36"
    )
  )

lazy val slf4JBridgeBenchmarks = project
  .in(file("odin-slf4j-bridge-benchmarks"))
  .enablePlugins(JmhPlugin)
  .settings(
    name := "odin-slf4j-bridge-benchmarks"
  )
  .dependsOn(slf4JBridge)
