ThisBuild / scalaVersion           := "2.13.13"
ThisBuild / crossScalaVersions     := Seq("2.13.13", "3.3.3")
ThisBuild / organization           := "com.permutive"
ThisBuild / versionPolicyIntention := Compatibility.BinaryAndSourceCompatible

addCommandAlias("ci-test", "fix --check; versionPolicyCheck; mdoc; publishLocal; +test")
addCommandAlias("ci-docs", "github; mdoc; headerCreateAll")
addCommandAlias("ci-publish", "versionCheck; github; ci-release")

val Cats = "2.9.0"

val CatsEffect = "3.4.2"

val Odin = "0.13.0"

lazy val dynamic = project
  .in(file("odin-dynamic"))
  .settings(
    name := "odin-dynamic",
    libraryDependencies ++= Seq(
      "org.typelevel"        %% "cats-core"               % Cats,
      "org.typelevel"        %% "kittens"                 % "3.0.0",
      "org.typelevel"        %% "cats-effect"             % CatsEffect,
      "com.github.valskalla" %% "odin-core"               % Odin,
      "org.typelevel"        %% "cats-effect-testkit"     % CatsEffect % Test,
      "org.typelevel"        %% "scalacheck-effect-munit" % "1.0.4"    % Test,
      "org.typelevel"        %% "munit-cats-effect-3"     % "1.0.7"    % Test
    )
  )
  .dependsOn(testing % "test->compile")

lazy val testing = project
  .in(file("odin-testing"))
  .settings(
    name := "odin-testing",
    libraryDependencies ++= Seq(
      "org.typelevel"        %% "cats-core"   % Cats,
      "org.typelevel"        %% "cats-effect" % CatsEffect,
      "com.github.valskalla" %% "odin-core"   % Odin
    )
  )

lazy val log4cats = project
  .in(file("log4cats-odin"))
  .settings(
    name := "log4cats-odin",
    libraryDependencies ++= Seq(
      "org.typelevel"        %% "cats-core"     % Cats,
      "org.typelevel"        %% "cats-effect"   % CatsEffect,
      "org.typelevel"        %% "log4cats-core" % "2.5.0",
      "com.github.valskalla" %% "odin-core"     % Odin
    )
  )

lazy val slf4JBridge = project
  .in(file("odin-slf4j-bridge"))
  .settings(
    name := "odin-slf4j-bridge",
    libraryDependencies ++= Seq(
      "org.typelevel"        %% "cats-core"   % Cats,
      "org.typelevel"        %% "cats-effect" % CatsEffect,
      "com.github.valskalla" %% "odin-core"   % Odin,
      "org.slf4j"             % "slf4j-api"   % "1.7.36"
    )
  )

lazy val slf4JBridgeBenchmarks = project
  .in(file("odin-slf4j-bridge-benchmarks"))
  .enablePlugins(JmhPlugin)
  .settings(
    name := "odin-slf4j-bridge-benchmarks"
  )
  .dependsOn(slf4JBridge)
