name := "HttpTest3"

version := "0.1"

scalaVersion := "2.9.2"

scalacOptions ++= Seq("-deprecation")

libraryDependencies ++= Seq( 
   "com.google.code.gson" % "gson" % "2.1",
   "org.apache.httpcomponents" % "httpclient" % "4.2.1"
)
