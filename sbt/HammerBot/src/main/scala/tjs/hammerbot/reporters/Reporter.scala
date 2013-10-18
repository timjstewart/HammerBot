package tjs.hammerbot.reporters

import tjs.hammerbot.model._

/** Trait that the [[tjs.hammerbot.runner.Runner]] uses to report events to */
trait Reporter {

  /** Called when the Runner is about to run a Suite */
  def suiteStarting(suiteName: String): Unit

  /** Called when the Runner has finished running a Suite */
  def suiteComplete(suiteName: String): Unit

  /** Called when the Runner is about to run a Test */
  def testStarting(testName: String): Unit

  /** Called when the Runner has finished running a Test */
  def testComplete(testName: String, succeeded: Boolean): Unit

  /** Called when the Runner is about to run a Test Setup */
  def testSetUpStarting(testName: String): Unit

  /** Called when the Runner has finished running a Test SetUp */
  def testSetUpComplete(testName: String, succeeded: Boolean): Unit

  /** Called when the Runner is about to run a Test TearDown */
  def testTearDownStarting(testName: String): Unit
  
  /** Called when the Runner has finished running a Test TearDown */
  def testTearDownComplete(testName: String, succeeded: Boolean): Unit

  /** Called when the Runner is about to send an HTTP Request */
  def requestSending(request: Request): Unit

  /** Called when the Runner is unable send an HTTP Request */
  def requestNotSent(request: Request, reason: String, config: IConfig): Unit

  /** Called when the Runner was able to send an HTTP Request but no Response was received (e.g. connection refused) */
  def requestFailed(request: Request, reason: String, elapsedMillis: Int): Unit

  /** Called when the Runner received an HTTP Response.  This includes responses with 4xx and 5xx status codes */
  def responseReceived(request: Request, response: Response, config: IConfig, elapsedMillis: Int): Unit

  /** Called before the Runner runs an Operation */
  def operationStarting(operation: Operation): Unit

  /** Called after the Runner successfully runs an Operation */
  def operationSucceeded(operation: Operation): Unit

  /** Called after the Runner unsuccessfully runs an Operation */
  def operationFailed(operation: Operation, message: String): Unit

  /** Called when the Runner cannot run an operation (e.g. due to a failed Request) */
  def operationSkipped(operation: Operation, message: String): Unit

  /** Called after all Operations for a Call have completed */
  def operationsComplete(): Unit

}
