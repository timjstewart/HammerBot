package tjs.hammerbot.reporters

import tjs.hammerbot.model._

/** Reporter that logs using the TAP format */
class TapReporter() extends Reporter {

  var testNumber: Int = 0

  override def suiteStarting(suiteName: String): Unit = Unit
  override def suiteComplete(suiteName: String): Unit = Unit

  override def testStarting(testName: String): Unit = Unit
  override def testComplete(testName: String, succeeded: Boolean): Unit = Unit

  override def requestNotSent(request: Request, reason: String, config: IConfig): Unit = Unit
  override def requestSending(request: Request): Unit = Unit
  override def requestFailed(request: Request, reason: String, elapsedMillis: Int): Unit = Unit
  override def responseReceived(request: Request, response: Response, config: IConfig, elapsedMillis: Int): Unit = Unit

  override def operationStarting(operation: Operation): Unit = Unit

  override def operationSucceeded(operation: Operation): Unit = {
    testNumber = testNumber + 1
    println("%d ok - %s".format(testNumber, operation.description))
  }

  override def operationFailed(operation: Operation, message: String): Unit = {
    testNumber = testNumber + 1
    println("%d not ok - %s".format(testNumber, message))
  }
  
  def operationSkipped(operation: Operation, message: String): Unit = Unit

  override def operationsComplete(): Unit = Unit

  override def testSetUpStarting(testName: String): Unit = 
    println("# Test SetUp: %s".format(testName))

  override def testSetUpComplete(testName: String, succeeded: Boolean): Unit =
    println("# Test SetUp Complete: %s".format(testName))

  override def testTearDownStarting(testName: String): Unit = 
    println("# Test TearDown: %s".format(testName))

  override def testTearDownComplete(testName: String, succeeded: Boolean): Unit = 
    println("# Test TearDown Complete: %s".format(testName))
 
}
