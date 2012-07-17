package tjs.http_test.reporters

import tjs.http_test.model._

class MetricsReporter(
) extends Reporter {

  var totalTests: Int = 0
  var testsFailed: Int = 0
  var testsPassed: Int = 0

  var totalCalls: Int = 0
  var callsSucceeded: Int = 0
  var callsFailed: Int = 0
  var callsSkipped: Int = 0

  var totalAssertions: Int = 0
  var assertionsFailed: Int = 0
  var assertionsSucceeded: Int = 0
  var assertionsSkipped: Int = 0

  def suiteStarting(suiteName: String): Unit = Unit
  def suiteComplete(suiteName: String): Unit = Unit

  def testStarting(testName: String): Unit = totalTests = totalTests + 1
    
  def testComplete(testName: String, succeeded: Boolean): Unit = 
    if (succeeded)
      testsPassed = testsPassed + 1
    else
      testsFailed = testsFailed + 1

  def requestSending(request: Request): Unit = 
    totalCalls = totalCalls + 1
    
  def requestNotSent(request: Request, reason: String, config: IConfig): Unit = {
    totalCalls = totalCalls + 1
    callsSkipped = callsSkipped + 1
  }

  def requestFailed(request: Request, reason: String, elapsedMillis: Int): Unit = 
    callsFailed = callsFailed + 1
    
  def responseReceived(request: Request, response: Response, config: IConfig, elapsedMillis: Int): Unit = 
    callsSucceeded = callsSucceeded + 1

  def operationStarting(operation: Operation): Unit = 
    totalAssertions = totalAssertions + 1
    
  def operationSkipped(operation: Operation, message: String): Unit =
    assertionsSkipped = assertionsSkipped + 1

  def operationSucceeded(operation: Operation): Unit = 
    assertionsSucceeded = assertionsSucceeded + 1
    
  def operationFailed(operation: Operation, message: String): Unit = 
    assertionsFailed = assertionsFailed + 1

  def operationsComplete(): Unit = Unit

  override def toString: String = {
    ("\nResults: %s\n" +
     "  Assertions - total: %3d, succeeded: %3d, failed: %3d,  skipped: %3d\n" + 
     "  Calls      - total: %3d, succeeded: %3d, failed: %3d,  skipped: %3d\n" +
     "  Tests      - total: %3d, succeeded: %3d, failed: %3d").format(
      getTestResultString(),
      totalAssertions, assertionsSucceeded, assertionsFailed, assertionsSkipped, 
      totalCalls, callsSucceeded, callsFailed, callsSkipped, 
      totalTests, testsPassed, testsFailed)
  }

  private def getTestResultString(): String = 
    if (assertionsFailed > 0 || assertionsSkipped > 0 || callsFailed > 0 || callsSkipped > 0 || testsFailed > 0)
      "FAILED"
    else
      "PASSED"
}
