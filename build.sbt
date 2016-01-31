
name := "owLint"

version := "0.2.1"

scalaVersion := "2.10.2"

resolvers += "spray" at "http://repo.spray.io/"

libraryDependencies ++= Seq(
  "net.sourceforge.owlapi" % "owlapi-distribution" % "3.4.5",
  "io.spray" %%  "spray-json" % "1.2.6",
  "org.scalatest" %% "scalatest" % "2.2.0" % "test"
)

lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "owLint"
  )
