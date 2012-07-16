package tjs.http_test.reporters

trait Reporter {
  import tjs.http_test.model._

  def suiteStarting(suiteName: String): Unit
  def suiteComplete(suiteName: String): Unit

  def testStarting(testName: String): Unit
  def testComplete(testName: String, succeeded: Boolean): Unit

  def requestSending(request: Request): Unit
  def requestNotSent(request: Request, reason: String, config: IConfig): Unit
  def requestFailed(request: Request, reason: String, elapsedMillis: Int): Unit
  def responseReceived(request: Request, response: Response, elapsedMillis: Int): Unit

  def operationStarting(operation: Operation): Unit
  def operationSucceeded(operation: Operation): Unit
  def operationFailed(operation: Operation, message: String): Unit
  def operationSkipped(operation: Operation, message: String): Unit
  def operationsComplete(): Unit

}
