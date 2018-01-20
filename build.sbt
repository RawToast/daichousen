import sbt.Keys.libraryDependencies

name := "daibouken"

version := "0.5.0-SNAPSHOT"

mainClass in(Compile, run) := Some("chousen.Http4sServer")
parallelExecution in Test:= false

val SCALA_VERSION = "2.12.4"
val CATS_VERSION = "1.0.1"
val MONOCLE_VERSION = "1.5.0-cats"

scalaVersion := SCALA_VERSION
scalaVersion in ThisBuild := SCALA_VERSION

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots"),
  "Bintary JCenter" at "http://jcenter.bintray.com"
)

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % Test
libraryDependencies += "org.mockito" % "mockito-core" % "2.9.0" % Test
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.1"

libraryDependencies ++= cats
libraryDependencies ++= monocle


def monocle = Seq(
  "com.github.julien-truffaut" %% "monocle-core" % MONOCLE_VERSION,
  "com.github.julien-truffaut" %%  "monocle-macro" % MONOCLE_VERSION
)

def cats = Seq(
  "org.typelevel" %% "cats-core" % CATS_VERSION,
  "org.typelevel" %% "cats-free" % CATS_VERSION
)

// Code coverage
// addCommandAlias("validate", ";coverage;test;coverageReport")
// coverageMinimum := 70
// coverageFailOnMinimum := false // Off for initialisation

// Compiler options
val compilerOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:_",
  "-unchecked",
  "-Xlint:_",
   "-Xfatal-warnings",
  "-Xfuture",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused-import",
  "-Ywarn-value-discard",
  "-Ypartial-unification"
)
val additionalOptions = Seq(
  //"-Ywarn-unused",
  "-Ywarn-dead-code",
  "-Ywarn-value-discard"
)

scalacOptions in Compile ++= compilerOptions ++ additionalOptions
scalacOptions in Test ++= compilerOptions
