name := "Pong"

version := "0.1"

scalaVersion := "2.12.8"

val monocleVersion = "1.6.0-M4"
libraryDependencies ++= Seq(
  "io.monix" %% "monix" % "3.0.0-RC2",
  "org.typelevel" %% "cats-core" % "1.6.0",
  "org.typelevel" %% "cats-effect" % "1.2.0",
  "co.fs2" %% "fs2-core" % "1.0.4",
  "co.fs2" %% "fs2-io" % "1.0.4",
  "com.beachape" %% "enumeratum" % "1.5.13",
  "com.github.julien-truffaut" %%  "monocle-core" % monocleVersion,
  "com.github.julien-truffaut" %%  "monocle-macro" % monocleVersion,
)
