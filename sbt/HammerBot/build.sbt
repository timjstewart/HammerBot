name := "HammerBot"

version := "0.1"

scalaVersion := "2.10.0-M5"

scalacOptions ++= Seq("-deprecation")

libraryDependencies ++= Seq( 
   "com.google.code.gson" % "gson" % "2.1",
   "org.apache.httpcomponents" % "httpclient" % "4.2.1"
)
