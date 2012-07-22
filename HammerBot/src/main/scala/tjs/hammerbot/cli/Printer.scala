package tjs.hammerbot.cli

import tjs.hammerbot.model._
import tjs.hammerbot.utils._

/** Prints a hierarchy of tests and test suites */
class Printer() {

  /** the ConsoleWriter used to print the structure of the
    * suite. 
    */
  val writer: ConsoleWriter = new ConsoleWriter()

  /** prints the Suite and all of its nested Suites and Tests
    * 
    * @param suite the suite to print.
    */
  def print(suite: Suite): Unit = {
    suite match {
      case s@SuiteGroup(_,_) => printSuiteGroup(s)
      case t@TestGroup(_,_,_) => printTestGroup(t)
    }
  }

  /** prints a Test */
  private def printTest(test: Test): Unit = {
    writer.println("Test: %s".format(test.name))
  }

  /** prints a TestGroup and all of its nested Test objects.
    *
    * @param testGroup the TestGroup to print
    */
  private def printTestGroup(testGroup: TestGroup): Unit = {
    writer.println("Tests: %s".format(testGroup.name))
    writer.indent {
      testGroup.tests.foreach(x => printTest(x))
    }
  }

  /** prints a SuiteGroup and all of its nested Suites and Tests
    *
    * @param suiteGroup the SuiteGroup to print
    */
  private def printSuiteGroup(suiteGroup: SuiteGroup): Unit = {
    writer.println("Suite: %s".format(suiteGroup.name))
    writer.indent {
      suiteGroup.branches.foreach(x => print(x))
    }
  }

}
