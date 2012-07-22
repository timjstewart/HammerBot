package tests

import tjs.hammerbot._
import tjs.hammerbot.cli._

object Main extends App {

  override def main(args: Array[String]): Unit = {

    val socialSuite = suites("Social Suites", 
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

    CommandLineProcessor.run(args, socialSuite, conf)
  }
}
