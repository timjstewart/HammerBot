package tjs.http_test.runner

import tjs.http_test.model._
import tjs.http_test.reporters._

case class DefaultConsoleRunner(
  val config: IConfig
) {
  val metrics = new MetricsReporter()
  val console = new ConsoleReporter(ConsoleReporter.default)

  def run(tree: Tree): Unit = {
    val runner = new Runner(config, getReporter())
    runner.run(tree)
    println(metrics.toString)
  }

  private def getReporter(): Reporter = {
    new CompositeReporter(metrics, console)
  }
}
