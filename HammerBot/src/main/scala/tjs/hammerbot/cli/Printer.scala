package tjs.hammerbot.cli

import tjs.hammerbot.model._
import tjs.hammerbot.utils._

class Printer() {
  val writer: ConsoleWriter = new ConsoleWriter()

  def print(suite: Suite): Unit = {
    suite match {
      case s@SuiteGroup(_,_) => printSuiteGroup(s)
      case t@TestGroup(_,_,_) => printTestGroup(t)
    }
  }

  def printTest(test: Test): Unit = {
    writer.println("Test: %s".format(test.name))
  }

  def printTestGroup(testGroup: TestGroup): Unit = {
    writer.println("Tests: %s".format(testGroup.name))
    writer.indent {
      testGroup.tests.foreach(x => printTest(x))
    }
  }

  def printSuiteGroup(suiteGroup: SuiteGroup): Unit = {
    writer.println("Suite: %s".format(suiteGroup.name))
    writer.indent {
      suiteGroup.branches.foreach(x => print(x))
    }
  }

}
