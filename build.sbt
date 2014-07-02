
name := "owLint"

version := "0.0"

scalaVersion := "2.10.2"

resolvers += "spray" at "http://repo.spray.io/"

libraryDependencies ++= Seq(
  "net.sourceforge.owlapi" % "owlapi-distribution" % "3.4.5",
  "io.spray" %%  "spray-json" % "1.2.6"
)     
