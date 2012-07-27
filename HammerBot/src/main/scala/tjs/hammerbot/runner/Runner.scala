package tjs.hammerbot.runner

import scala.util.matching.Regex
import com.google.gson._
import tjs.hammerbot.model._
import tjs.hammerbot.utils._
import tjs.hammerbot.reporters.{ Reporter, ConsoleReporter }

private case class RunCallsResult(
  val results: Seq[Result],
  val config:  IConfig)

private case class SetUpResult(
  val results: Seq[Result],
  val config:  IConfig)

private case class TearDownResult(
  val results: Seq[Result])

private object RunCallsResult {
  val empty = RunCallsResult(Seq(), Config.empty)
}

private object SetUpResult {
  val empty = SetUpResult(Seq(), Config.empty)
}

private object TearDownResult {
  val empty = TearDownResult(Seq())
}

class Runner(
  globals:  IConfig,
  reporter: Reporter = new ConsoleReporter()
) {

  def run(tree: Suite): Seq[Result] = tree match {
    case sg:SuiteGroup => runSuiteGroup(sg)
    case tg:TestGroup => runTestGroup(tg)
  }

  def runTestGroup(testGroup: TestGroup): Seq[Result] ={
    reporter.suiteStarting(testGroup.name)
    val results = testGroup.tests.flatMap(test => runTest(test, testGroup))
    reporter.suiteComplete(testGroup.name)
    results
  }

  def runSuiteGroup(suiteGroup: SuiteGroup): Seq[Result] = {
    reporter.suiteStarting(suiteGroup.name)
    val result = suiteGroup.suites.flatMap(run)
    reporter.suiteComplete(suiteGroup.name)
    result
  }

  def runTest(test: Test, testGroup: TestGroup): Seq[Result] = {
    reporter.testStarting(test.name)

    val setUpResult: SetUpResult = runSetUp(test, testGroup)
    val setUpSucceeded = allPassed(setUpResult.results)

    val config = setUpResult.config + testGroup.suiteConfig

    val RunCallsResult(results, _) = runCalls(test, config)

    val tearDownResults = runTearDown(test, testGroup, config)

    reporter.testComplete(test.name, allPassed(results))

    setUpResult.results ++ results ++ tearDownResults.results
  }

  private def runCalls(hasCalls: HasCalls, suiteConfig: IConfig): RunCallsResult = {
    val testConfig = MutableConfig.empty
    val results = hasCalls.calls.flatMap(call => runCall(call, suiteConfig, testConfig))
    RunCallsResult(results, testConfig)
  }

  private def runSetUp(test: Test, testGroup: TestGroup): SetUpResult = {
    testGroup.setUp match {
      case Some(setUp) => 
        reporter.testSetUpStarting(test.name)
        val result: RunCallsResult = runCalls(setUp, testGroup.suiteConfig)
        reporter.testSetUpComplete(test.name, true)
        SetUpResult(result.results, result.config)
      case None =>
        SetUpResult.empty
    }
  }

  private def runTearDown(test: Test, testGroup: TestGroup, config: IConfig): TearDownResult = {
    testGroup.tearDown match {
      case Some(tearDown) => 
        reporter.testTearDownStarting(test.name)
        val result: RunCallsResult = runCalls(tearDown, config)
        reporter.testTearDownComplete(test.name, true)
        TearDownResult(result.results)
      case None => TearDownResult.empty
    }
  }

  private def allPassed(results: Seq[Result]): Boolean = {
    def passed_(result: Result): Boolean = result match {
      case Failure(_) => false
      case Success()  => true
    }
    results.forall(passed_)
  }

  private def runCall(call: Call, suiteConfig: IConfig, testConfig: MutableConfig): Seq[Result] = {
    // The order that the IConfig objects are added is important
    val config = testConfig + suiteConfig + globals

    val InterpolationResult(request, undefinedValues) = interpolate(call.request, config)

    undefinedValues match {
      case _ :: _ => 
        reporter.requestNotSent(request, "Undefined Value(s): %s".format(undefinedValues.mkString(", ")), config)
        call.operations.foreach(op => reporter.operationSkipped(op, "Request not sent"))
        Seq(Failure("Undefined values: %s".format(undefinedValues.mkString(", "))))

      case _ => 
        reporter.requestSending(request)
        val callResult= sendRequest(request, call.timeOut) 
        callResult.result match {

          case Right(response) => 
            reporter.responseReceived(request, response, config, callResult.elapsedMilliseconds)
            val result = call.operations.map(op => runOperation(op, response, testConfig))
            reporter.operationsComplete()
            result

          case Left(error) => 
            reporter.requestFailed(request, error, callResult.elapsedMilliseconds)
            call.operations.foreach(op => reporter.operationSkipped(op, "Request not sent"))
            reporter.operationsComplete()
            Seq(Failure(error))
        }
    }
  }

  private def sendRequest(request: Request, timeOut: Option[Int]): CallResult = {
    new HttpRequest(request, timeOut).send()
  }
  
  private def interpolate(request: Request, scopes: IConfig*): InterpolationResult[Request] = {
    new RequestInterpolator(scopes).interpolate(request)
  }

  private def runOperation(operation: Operation, response: Response, testConfig: MutableConfig): Result = {
    import Operations._

    reporter.operationStarting(operation)

    val result = operation match {
     case op:StatusCodeEquals           => expectStatusCode(op, response)
     case op:StatusCodeIsInRange        => expectStatusCodeInRange(op, response)

     case op:HeaderEquals               => expectHeaderEquals(op, response)
     case op:ContentTypeEquals          => expectContentTypeEquals(op, response)
     case op:ContentTypeMatches         => expectContentTypeMatches(op, response)
     case op:ContentTypeContains        => expectContentTypeContains(op, response)

     case op:CookieIsPresent            => expectCookieIsPresent(op, response)
     case op:CookieHasValue             => expectCookieEquals(op, response)

     case op:JsonPropertyExists         => expectJsonPropertyExists(op, response)
     case op:JsonPropertyDoesNotExist   => expectJsonPropertyDoesNotExist(op, response)
     case op:JsonPropertyEquals         => expectJsonPropertyEquals(op, response)
     case op:JsonPropertyMatches        => expectJsonPropertyMatches(op, response)

     case op:BodyShouldContain          => expectBodyContains(op, response)
     case op:BodyShouldMatch            => expectBodyMatches(op, response)
     case op:BodyShouldEqual            => expectBodyEquals(op, response)

     case op:SaveBodyContents           => saveBodyContents(op, response, testConfig)
     case op:SaveBodyMatch              => saveBodyMatch(op, response, testConfig)
     case op:SaveJsonProperty           => saveJsonProperty(op, response, testConfig)

     case op:CustomOperationHolder      => runCustom(op, response, testConfig)

     case op:StatusCodeDoesNotEqual     => expectStatusCodeDoesNotEqual(op, response)
     case op:StatusCodeIsNotInRange     => expectStatusCodeNotInRange(op, response)

     case op:HeaderDoesNotEqual         => expectHeaderDoesNotEqual(op, response)

     case op:CookieDoesNotHaveValue     => expectCookieDoesNotEqual(op, response)

     case op:JsonPropertyDoesNotEqual   => expectJsonPropertyDoesNotEqual(op, response)
     case op:JsonPropertyDoesNotMatch   => expectJsonPropertyDoesNotMatch(op, response)

     case op:BodyShouldNotContain       => expectBodyDoesNotContain(op, response)
     case op:BodyShouldNotMatch         => expectBodyDoesNotMatch(op, response)
     case op:BodyShouldNotEqual         => expectBodyDoesNotEqual(op, response)

     case op                            => Failure("Unknown Operation")
    }

    result match {
      case Success()    => reporter.operationSucceeded(operation)
      case Failure(err) => reporter.operationFailed(operation, err)
    }

    result
  }

}
