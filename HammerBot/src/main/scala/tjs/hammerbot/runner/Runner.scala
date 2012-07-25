package tjs.hammerbot.runner

import scala.util.matching.Regex
import com.google.gson._
import tjs.hammerbot.model._
import tjs.hammerbot.utils._
import tjs.hammerbot.reporters.{ Reporter, ConsoleReporter }

class Runner(
  globals:  IConfig,
  reporter: Reporter = new ConsoleReporter()
) {

  def run(tree: Suite): Seq[Result] = tree match {
    case SuiteGroup(name, suites)            => runSuiteGroup(name, suites)
    case TestGroup(name, suiteConfig, tests) => runTests(name, suiteConfig, tests)
  }

  def runTests(name: String, suiteConfig: Config, tests: Seq[Test]): Seq[Result] ={
    reporter.suiteStarting(name)
    val result = tests.flatMap(test => runTest(test, suiteConfig))
    reporter.suiteComplete(name)
    result
  }

  def runSuiteGroup(name: String, suites: Seq[Suite]): Seq[Result] = {
    reporter.suiteStarting(name)
    val result = suites.flatMap(run)
    reporter.suiteComplete(name)
    result
  }

  def runTest(test: Test, suiteConfig: Config): Seq[Result] = {
    reporter.testStarting(test.name)
    val testConfig = MutableConfig.empty
    val results = test.calls.flatMap(call => runCall(call, suiteConfig, testConfig))
    reporter.testComplete(test.name, allPassed(results))
    results
  }

  private def allPassed(results: Seq[Result]): Boolean = {
    def passed_(result: Result): Boolean = result match {
      case Failure(_) => false
      case Success()  => true
    }
    results.forall(passed_)
  }

  private def runCall(call: Call, suiteConfig: Config, testConfig: MutableConfig): Seq[Result] = {
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
     case op@StatusCodeEquals(_)           => expectStatusCode(op, response)
     case op@StatusCodeIsInRange(_,_)      => expectStatusCodeInRange(op, response)

     case op@HeaderEquals(_)               => expectHeaderEquals(op, response)
     case op@ContentTypeEquals(_)          => expectContentTypeEquals(op, response)
     case op@ContentTypeMatches(_)         => expectContentTypeMatches(op, response)
     case op@ContentTypeContains(_)        => expectContentTypeContains(op, response)

     case op@CookieIsPresent(_)            => expectCookieIsPresent(op, response)
     case op@CookieHasValue(_,_)           => expectCookieEquals(op, response)

     case op@JsonPropertyEquals(_,_)       => expectJsonPropertyEquals(op, response)
     case op@JsonPropertyMatches(_,_)      => expectJsonPropertyMatches(op, response)

     case op@BodyShouldContain(_)          => expectBodyContains(op, response)
     case op@BodyShouldMatch(_)            => expectBodyMatches(op, response)
     case op@BodyShouldEqual(_)            => expectBodyEquals(op, response)

     case op@SaveBodyContents(_)           => saveBodyContents(op, response, testConfig)
     case op@SaveBodyMatch(_,_)            => saveBodyMatch(op, response, testConfig)
     case op@SaveJsonProperty(_,_)         => saveJsonProperty(op, response, testConfig)

     case op@CustomOperationHolder(_)      => runCustom(op, response, testConfig)

     case op@StatusCodeDoesNotEqual(_)     => expectStatusCodeDoesNotEqual(op, response)
     case op@StatusCodeIsNotInRange(_,_)   => expectStatusCodeNotInRange(op, response)

     case op@HeaderDoesNotEqual(_)         => expectHeaderDoesNotEqual(op, response)

     case op@CookieDoesNotHaveValue(_,_)   => expectCookieDoesNotEqual(op, response)

     case op@JsonPropertyDoesNotEqual(_,_) => expectJsonPropertyDoesNotEqual(op, response)
     case op@JsonPropertyDoesNotMatch(_,_) => expectJsonPropertyDoesNotMatch(op, response)

     case op@BodyShouldNotContain(_)       => expectBodyDoesNotContain(op, response)
     case op@BodyShouldNotMatch(_)         => expectBodyDoesNotMatch(op, response)
     case op@BodyShouldNotEqual(_)         => expectBodyDoesNotEqual(op, response)

     case op                               => Failure("Unknown Operation")
    }

    result match {
      case Success()    => reporter.operationSucceeded(operation)
      case Failure(err) => reporter.operationFailed(operation, err)
    }

    result
  }

}
