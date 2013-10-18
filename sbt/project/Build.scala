import sbt._
import Keys._

object HammerBotBuild extends Build {

  lazy val hammerBotDriver = Project(
    id = "driver",
    base = file("Driver")) aggregate(hammerBot) dependsOn(hammerBot)

  lazy val hammerBot = Project(
    id = "hammerbot",
    base = file("HammerBot"))

}
