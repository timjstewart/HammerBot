package tjs.hammerbot.reporters

import tjs.hammerbot.model._

class MetricsReporter(
) extends Reporter {

  var totalTests: Int = 0
  var testsFailed: Int = 0
  var testsPassed: Int = 0

  var callsSucceeded: Int = 0
  var callsFailed: Int = 0
  var callsSkipped: Int = 0

  var operationsFailed: Int = 0
  var operationsSucceeded: Int = 0
  var operationsSkipped: Int = 0

  def suiteStarting(suiteName: String): Unit = Unit
  def suiteComplete(suiteName: String): Unit = Unit

  def testStarting(testName: String): Unit = totalTests = totalTests + 1
    
  def testComplete(testName: String, succeeded: Boolean): Unit = 
    if (succeeded)
      testsPassed = testsPassed + 1
    else
      testsFailed = testsFailed + 1

  def requestSending(request: Request): Unit = Unit
    
  def requestNotSent(request: Request, reason: String, config: IConfig): Unit = {
    callsSkipped = callsSkipped + 1
  }

  def requestFailed(request: Request, reason: String, elapsedMillis: Int): Unit = 
    callsFailed = callsFailed + 1
    
  def responseReceived(request: Request, response: Response, config: IConfig, elapsedMillis: Int): Unit = 
    callsSucceeded = callsSucceeded + 1

  def operationStarting(operation: Operation): Unit = Unit
    
  def operationSkipped(operation: Operation, message: String): Unit =
    operationsSkipped = operationsSkipped + 1

  def operationSucceeded(operation: Operation): Unit = 
    operationsSucceeded = operationsSucceeded + 1
    
  def operationFailed(operation: Operation, message: String): Unit = 
    operationsFailed = operationsFailed + 1

  def operationsComplete(): Unit = Unit

  override def toString: String = {
    val totalOperations: Int = operationsSucceeded + operationsFailed + operationsSkipped 
    val totalCalls: Int = callsSucceeded + callsFailed + callsSkipped 
    val totalTests: Int = testsPassed + testsFailed

    ("\nResults: %s\n" +
     "  Operations - total: %3d, succeeded: %3d, failed: %3d,  skipped: %3d\n" + 
     "  Calls      - total: %3d, succeeded: %3d, failed: %3d,  skipped: %3d\n" +
     "  Tests      - total: %3d, succeeded: %3d, failed: %3d").format(
      getTestResultString(),
      totalOperations, operationsSucceeded, operationsFailed, operationsSkipped, 
      totalCalls, callsSucceeded, callsFailed, callsSkipped, 
      totalTests, testsPassed, testsFailed)
  }

  private def getTestResultString(): String = 
    if (operationsFailed > 0 || operationsSkipped > 0 || callsFailed > 0 || callsSkipped > 0 || testsFailed > 0)
      "FAILED"
    else
      "PASSED"
}
