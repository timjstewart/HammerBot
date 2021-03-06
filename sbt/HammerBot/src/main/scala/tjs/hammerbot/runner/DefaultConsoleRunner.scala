package tjs.hammerbot.runner

import tjs.hammerbot.model._
import tjs.hammerbot.reporters._

case class DefaultConsoleRunner(
  val config: IConfig
) {
  val metrics = new MetricsReporter()
  val console = new ConsoleReporter(ConsoleReporter.default)

  def run(suite: Suite): Unit = {
    val runner = new Runner(config, getReporter())
    runner.run(suite)
    println(metrics.toString)
  }

  private def getReporter(): Reporter = {
    new CompositeReporter(metrics, console)
  }
}

