name := "Pong"
ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.13.7"
ThisBuild / organization := "dev.susliko"

scalacOptions in Global += "-Ymacro-annotations"

lazy val root = (project in file(".")).settings(
  libraryDependencies ++= Seq(
    "io.monix" %% "monix" % "3.4.0",
    "org.typelevel" %% "cats-core" % "2.7.0",
    "org.typelevel" %% "cats-effect" % "2.5.1",
    "co.fs2" %% "fs2-core" % "2.5.4",
    "co.fs2" %% "fs2-io" % "2.5.4",
    "com.beachape" %% "enumeratum" % "1.7.0",
    "dev.optics" %% "monocle-core" % "3.1.0",
    "dev.optics" %% "monocle-macro" % "3.1.0"
  ),
  assembly / mainClass := Some("Main"),
  assembly / assemblyJarName := "pong.jar"
)
