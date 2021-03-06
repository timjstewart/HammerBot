package tjs.hammerbot.reporters

case class CompositeReporter(
  val reporters: Reporter*
) extends Reporter {
  import tjs.hammerbot.model._

  def suiteStarting(suiteName: String): Unit = 
    reporters.foreach(r => r.suiteStarting(suiteName))
    
  def suiteComplete(suiteName: String): Unit = 
    reporters.foreach(r => r.suiteComplete(suiteName))

  def testStarting(testName: String): Unit = 
    reporters.foreach(r => r.testStarting(testName))
    
  def testComplete(testName: String, succeeded: Boolean): Unit = 
    reporters.foreach(r => r.testComplete(testName, succeeded))

  def requestSending(request: Request): Unit = 
    reporters.foreach(r => r.requestSending(request))
    
  def requestNotSent(request: Request, reason: String, config: IConfig): Unit = 
    reporters.foreach(r => r.requestNotSent(request, reason, config))

  def requestFailed(request: Request, reason: String, elapsedMillis: Int): Unit = 
    reporters.foreach(r => r.requestFailed(request, reason, elapsedMillis))
    
  def responseReceived(request: Request, response: Response, config: IConfig, elapsedMillis: Int): Unit = 
    reporters.foreach(r => r.responseReceived(request, response, config, elapsedMillis))

  def operationStarting(operation: Operation): Unit = 
    reporters.foreach(r => r.operationStarting(operation))
    
  def operationSucceeded(operation: Operation): Unit = 
    reporters.foreach(r => r.operationSucceeded(operation))
    
  def operationFailed(operation: Operation, message: String): Unit = 
    reporters.foreach(r => r.operationFailed(operation, message))
  
  def operationSkipped(operation: Operation, message: String): Unit =
    reporters.foreach(r => r.operationSkipped(operation, message))

  def operationsComplete(): Unit = 
    reporters.foreach(r => r.operationsComplete())

  def testSetUpStarting(testName: String): Unit = 
    reporters.foreach(r => r.testSetUpStarting(testName))

  def testSetUpComplete(testName: String, succeeded: Boolean): Unit =
    reporters.foreach(r => r.testSetUpComplete(testName, succeeded))

  def testTearDownStarting(testName: String): Unit =
    reporters.foreach(r => r.testTearDownStarting(testName))
    
  def testTearDownComplete(testName: String, succeeded: Boolean): Unit =
    reporters.foreach(r => r.testTearDownComplete(testName, succeeded))
 
}
