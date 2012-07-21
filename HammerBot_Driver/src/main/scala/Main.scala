package tests

import tjs.hammerbot._
import tjs.hammerbot.runner._

object Main extends App {

  val socialSuite = suites("Social", 
    Blogs.getSuite(), 
    Tags.getSuite())

  val hosts = config(
    "blogHost" -> "localhost:9000",
    "tagsHost" -> "localhost:8000"
  )

  val keys = config(
    "system_a" -> "FEEDBEEF",
    "system_b" -> "314159"
  )

  val conf = hosts + keys

  DefaultConsoleRunner(conf).run(socialSuite)
}
