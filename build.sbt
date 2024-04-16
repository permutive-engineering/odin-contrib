ThisBuild / scalaVersion           := "2.13.13"
ThisBuild / crossScalaVersions     := Seq("2.13.13", "3.3.3")
ThisBuild / organization           := "com.permutive"
ThisBuild / versionPolicyIntention := Compatibility.BinaryAndSourceCompatible

addCommandAlias("ci-test", "fix --check; versionPolicyCheck; mdoc; publishLocal; +test")
addCommandAlias("ci-docs", "github; mdoc; headerCreateAll")
addCommandAlias("ci-publish", "versionCheck; github; ci-release")

lazy val documentation = project
  .enablePlugins(MdocPlugin)

lazy val `odin-dynamic` = module
  .settings(libraryDependencies += "org.typelevel" %% "cats-core" % "2.9.0")
  .settings(libraryDependencies += "org.typelevel" %% "kittens" % "3.0.0")
  .settings(libraryDependencies += "org.typelevel" %% "cats-effect" % "3.4.2")
  .settings(libraryDependencies += "com.github.valskalla" %% "odin-core" % "0.13.0")
  .settings(libraryDependencies += "org.typelevel" %% "cats-effect-testkit" % "3.4.2" % Test)
  .settings(libraryDependencies += "org.typelevel" %% "scalacheck-effect-munit" % "1.0.4" % Test)
  .settings(libraryDependencies += "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test)
  .dependsOn(`odin-testing` % "test->compile")

lazy val `odin-testing` = module
  .settings(libraryDependencies += "org.typelevel" %% "cats-core" % "2.9.0")
  .settings(libraryDependencies += "org.typelevel" %% "cats-effect" % "3.4.2")
  .settings(libraryDependencies += "com.github.valskalla" %% "odin-core" % "0.13.0")

lazy val `log4cats-odin` = module
  .settings(libraryDependencies += "org.typelevel" %% "cats-core" % "2.9.0")
  .settings(libraryDependencies += "org.typelevel" %% "cats-effect" % "3.4.2")
  .settings(libraryDependencies += "org.typelevel" %% "log4cats-core" % "2.5.0")
  .settings(libraryDependencies += "com.github.valskalla" %% "odin-core" % "0.13.0")

lazy val `odin-slf4j-bridge` = module
  .settings(libraryDependencies += "org.typelevel" %% "cats-core" % "2.9.0")
  .settings(libraryDependencies += "org.typelevel" %% "cats-effect" % "3.4.2")
  .settings(libraryDependencies += "com.github.valskalla" %% "odin-core" % "0.13.0")
  .settings(libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.36")

lazy val `odin-slf4j-bridge-benchmarks` = module
  .enablePlugins(JmhPlugin)
  .dependsOn(`odin-slf4j-bridge`)
  .settings(publish / skip := true)
