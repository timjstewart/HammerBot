package tjs.hammerbot.cli

import tjs.hammerbot.model._
import tjs.hammerbot.utils._

/** Prints a hierarchy of tests and test suites */
class Printer(
  val debug: Boolean
) {

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
    printCalls(test.calls)
  }

  private def printCalls(calls: Seq[Call]): Unit = {
    writer.indent {
      calls.foreach {
        call => 
          writer.println("Call: %s %s".format(call.request.method, call.request.uri))
          if (debug) printOperations(call.operations)
      }
    }
  }

  private def printOperations(operations: Seq[Operation]): Unit = {
    writer.indent {
      operations.foreach(operation => writer.println("Operation: %s".format(operation.description)))
    }
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
      suiteGroup.suites.foreach(x => print(x))
    }
  }

}
