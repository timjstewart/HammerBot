package tjs.hammerbot.cli

import tjs.hammerbot.model._
import tjs.hammerbot.runner._
import tjs.hammerbot.reporters._

/** Runs different commands basded on the provided arguments */
object CommandLineProcessor {
  def run(args: Array[String], suite: Suite, config: IConfig): Unit = {
    Arguments.parse(args) match {
      case Left(error) => println("error: %s".format(error))
      case Right(args) => run(args, suite, config)
    }
  }

  private def run(args: Arguments, suite: Suite, config: IConfig = Config.empty): Unit = args.command match {
    case "run"     => runCommand(args, suite, config)
    case "print"   => printCommand(args, suite)
    case "version" => versionCommand()
    case _         => printHelp()
  }

  private def versionCommand(): Unit = {
    println("HammerBot v%s".format("0.1"))
  }

  private def printHelp(): Unit = {
    new Help().printHelp()
  }

  private def printCommand(args: Arguments, suite: Suite): Unit = {
    new Printer(args.debug).print(suite)
  }

  private def runCommand(args: Arguments, suite: Suite, config: IConfig): Unit = {
    val metrics = new MetricsReporter()
    val console = getReporter(args)
    val reporter = new CompositeReporter(metrics, console)
    val runner = new Runner(config, reporter)
    runner.run(suite)
    println
    println(metrics.toString)
  }

  private def getReporter(args: Arguments): Reporter = {
    args.debug match { 
      case true  => new ConsoleReporter(ConsoleReporter.debug)
      case false => new ConsoleReporter(ConsoleReporter.default)
    }
  }
}
