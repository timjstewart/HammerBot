package tjs.hammerbot.runner

import java.net.{ UnknownHostException, ConnectException, SocketTimeoutException }
import scala.io._
import scala.util.matching.Regex
import com.google.gson._
import org.apache.http.impl.client._
import org.apache.http.client.methods.{ HttpGet, HttpPost, HttpPut, HttpOptions, HttpHead, HttpDelete }
import org.apache.http.impl.client.BasicResponseHandler
import org.apache.http.params.{ HttpParams, HttpConnectionParams }
import tjs.hammerbot.model._
import tjs.hammerbot.utils._
import tjs.hammerbot.reporters.{ Reporter, ConsoleReporter }

case class CallResult(
  val elapsedMilliseconds: Int,
  val result:              Either[String, Response])

private class TimeOutException(
  val allowed: Int,
  val actual: Int
) extends Throwable

class Runner(
  globals:  IConfig,
  reporter: Reporter = new ConsoleReporter()
) {

  def run(tree: Tree): Seq[Result] = tree match {
    case Branch(name, branches)         => runBranch(name, branches)
    case Leaf(name, suiteConfig, tests) => runTests(name, suiteConfig, tests)
  }

  def runTests(name: String, suiteConfig: Config, tests: Seq[Test]): Seq[Result] ={
    reporter.suiteStarting(name)
    val result = tests.flatMap(test => runTest(test, suiteConfig))
    reporter.suiteComplete(name)
    result
  }

  def runBranch(name: String, branches: Seq[Tree]): Seq[Result] = {
    reporter.suiteStarting(name)
    val result = branches.flatMap(run)
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
      case Success() => true
    }
    results.forall(passed_)
  }

  def runCall(call: Call, suiteConfig: Config, testConfig: MutableConfig): Seq[Result] = {
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
    

    val httpClient = allowInconsistentSslCertificates()// new DefaultHttpClient()


    val req = request.method match {
      case Get()     => new HttpGet(request.uri)
      case Post()    => new HttpPost(request.uri)
      case Put()     => new HttpPut(request.uri)
      case Delete()  => new HttpDelete(request.uri)
      case Head()    => new HttpHead(request.uri)
      case Options() => new HttpOptions(request.uri)
    }

    request.headers.foreach(h => req.addHeader(h.name, h.value))

    timeOut match {
      case Some(milliseconds) => 
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), milliseconds)
        HttpConnectionParams.setSoTimeout(httpClient.getParams(), milliseconds)
      case None => Unit
    }

    // TODO: Add cookies

    val startTime = System.currentTimeMillis()
    var elapsedTime: Int = 0

    val result = try {

      val resp = httpClient.execute(req)

      elapsedTime = (System.currentTimeMillis() - startTime).toInt

      checkTimeOut(timeOut, elapsedTime)

      val body: String = Source.fromInputStream(resp.getEntity().getContent(), "UTF-8").getLines().mkString("\n")

      httpClient.getConnectionManager().shutdown()

      val headers = resp.getAllHeaders().map(h => Header(h.getName, h.getValue))

      Right(Response(resp.getStatusLine().getStatusCode(), headers, Seq(), body))
    } catch {
      case conn: ConnectException => Left(conn.getMessage)
      case host: UnknownHostException => Left("Unknown host: %s".format(host.getMessage))
      case time2: TimeOutException => Left("Request timed out (%d msecs allowed, %d msecs taken).".format(time2.allowed, elapsedTime))
      case time: SocketTimeoutException => 
        elapsedTime = (System.currentTimeMillis() - startTime).toInt
        Left("Request timed out (%s msecs allowed, %s msecs taken).".format(timeOut.get, elapsedTime))
      //case ex => Left("Unexpected Exception: %s".format(ex.getMessage))
    }

    CallResult(elapsedTime, result)
  }
  
  private def checkTimeOut(timeOut: Option[Int], elapsedTime: Int): Unit = timeOut match {
    case Some(millis) => if (elapsedTime > millis) throw new TimeOutException(millis, elapsedTime)
    case None => Unit
  }

  def runOperation(operation: Operation, response: Response, testConfig: MutableConfig): Result = {
    reporter.operationStarting(operation)

    val result = operation match {
     case op@StatusCodeEquals(_)      => expectStatusCode(op, response)
     case op@StatusCodeIsInRange(_,_) => expectStatusCodeInRange(op, response)

     case op@HeaderEquals(_)          => expectHeaderEquals(op, response)
     case op@ContentTypeEquals(_)     => expectContentTypeEquals(op, response)
     case op@ContentTypeMatches(_)    => expectContentTypeMatches(op, response)
     case op@ContentTypeContains(_)   => expectContentTypeContains(op, response)

     case op@CookieIsPresent(_)       => expectCookieIsPresent(op, response)
     case op@CookieHasValue(_,_)      => expectCookieEquals(op, response)

     case op@JsonPropertyEquals(_,_)  => expectJsonPropertyEquals(op, response)
     case op@JsonPropertyMatches(_,_) => expectJsonPropertyMatches(op, response)

     case op@BodyShouldContain(_)     => expectBodyContains(op, response)
     case op@BodyShouldMatch(_)       => expectBodyMatches(op, response)
     case op@BodyShouldEqual(_)       => expectBodyEquals(op, response)

     case op@SaveBodyContents(_)      => saveBodyContents(op, response, testConfig)
     case op@SaveBodyMatch(_,_)       => saveBodyMatch(op, response, testConfig)
     case op@SaveJsonProperty(_,_)    => saveJsonProperty(op, response, testConfig)

     case op                          => Failure("Unknown Operation")
    }

    result match {
      case Success()    => reporter.operationSucceeded(operation)
      case Failure(err) => reporter.operationFailed(operation, err)
    }

    result
  }

  private def expectHeaderContains_(op: Operation, response: Response, name: String, value: String): Result = 
    response.lookupHeader(name) match {
      case Some(actual) => actual.value contains value match {
        case true => Success()
        case false => Failure("%s but the value was %s".format(op.description, actual.value))
      }
      case None => Failure("%s but the header was not present".format(op.description))
    }

  private def expectHeaderMatches_(op: Operation, response: Response, name: String, regex: Regex): Result = 
    response.lookupHeader(name) match {
      case Some(actual) => regex.findFirstIn(actual.value) match {
        case Some(_) => Success()
        case none => Failure("%s but the value was %s".format(op.description, actual.value))
      }
      case None => Failure("%s but the header was not present".format(op.description))
    }

  private def expectHeaderEquals_(op: Operation, response: Response, expected: Header): Result = 
    response.lookupHeader(expected.name) match {
      case Some(actual) => expected.value == actual.value match {
        case true => Success()
        case false => Failure("%s but the value was %s".format(op.description, actual.value))
      }
      case None => Failure("%s but the header was not present".format(op.description))
    }

  // TODO: Make this real
  def saveBodyMatch(op: SaveBodyMatch, response: Response, testConfig: MutableConfig): Result = 
    Success()

  // TODO: Make this real
  def saveBodyContents(op: SaveBodyContents, response: Response, testConfig: MutableConfig): Result = 
    Success()

  def expectContentTypeContains(op: ContentTypeContains, response: Response): Result = 
    expectHeaderContains_(op, response, "Content-Type", op.contentType)

  def expectContentTypeMatches(op: ContentTypeMatches, response: Response): Result = 
    expectHeaderMatches_(op, response, "Content-Type", op.contentType)

  def expectContentTypeEquals(op: ContentTypeEquals, response: Response): Result = 
    expectHeaderEquals_(op, response, Header("Content-Type", op.contentType))

  def expectBodyEquals(op: BodyShouldEqual, response: Response): Result = 
    op.value == response.body match {
      case true => Success()
      case false => Failure("%s but the body is: [%s].".format(op.description, response.body))
    }

  def expectBodyMatches(op: BodyShouldMatch, response: Response): Result = 
    op.regularExpression.findFirstIn(response.body) match {
      case Some(_) => Success()
      case None => Failure("%s but no matches were found.".format(op.description))
    }

  def expectBodyContains(op: BodyShouldContain, response: Response): Result = 
    if (response.body.indexOf(op.value) == -1)
      Failure("%s but it was not found.".format(op.description))
    else
      Success()

  def saveJsonProperty(op: SaveJsonProperty, response: Response, testConfig: MutableConfig): Result = 
    findProperty(response, op.propertyPath) match {
      case Right(value) => 
        testConfig.put(op.key, value)
        Success()
      case Left(failure) => Failure("Could not save JSON property.  %s".format(failure))
    }
    
  def expectJsonPropertyMatches(op: JsonPropertyMatches, response: Response): Result = 
    response.asJson match {
      case Right(json) => 
        findProperty(json, op.propertyPath) match {
          case Some(value) => op.regularExpression.findFirstIn(value) match {
            case Some(_) => Success()
            case None => Failure("%s but its value is: %s.".format(op.description, value))
          }
          case None => Failure("%s but the property was not found.".format(op.description))
      }

      case Left(error) => Failure("%s but the response body could not be parsed as JSON.  (%s)".format(op.description, error))
    }

  def expectJsonPropertyEquals(op: JsonPropertyEquals, response: Response): Result = 
    response.asJson match {
      case Right(json) => 
        findProperty(json, op.propertyPath) match {
          case Some(value) => value.toString == op.value.toString match {
            case true => Success()
            case false => Failure("%s but its value is: %s.".format(op.description, value))
          }
          case None => Failure("%s but the property was not found.".format(op.description))
      }

      case Left(error) => Failure("%s but the response body could not be parsed as JSON.  (%s)".format(op.description, error))
    }

  def expectHeaderEquals(op: HeaderEquals, response: Response): Result = 
    expectHeaderEquals_(op, response, op.header)
    
  def expectCookieEquals(op: CookieHasValue, response: Response): Result = 
    response.lookupCookie(op.name) match {
      case Some(cookie) => cookie.value == op.value match {
        case true => Success()
        case false => Failure("%s but it was: '%s'".format(op.description, cookie.value))
      }
      case None => Failure("%s but was not present".format(op.description))
    }

  def expectCookieIsPresent(op: CookieIsPresent, response: Response): Result = 
    response.lookupCookie(op.name) match {
      case Some(cookie) => Success()
      case None => Failure("%s but was not present".format(op.description))
    }

  def expectStatusCode(op: StatusCodeEquals, response: Response): Result = 
    if (response.statusCode == op.expected)
      Success()
    else
      Failure("%s but was: %s".format(op.description, response.statusCode))

  def expectStatusCodeInRange(op: StatusCodeIsInRange, response: Response): Result = 
    if (response.statusCode >= op.low && response.statusCode <= op.high)
      Success()
    else
      Failure("%s but was: %s".format(op.description, response.statusCode))

  def interpolate(request: Request, scopes: IConfig*): InterpolationResult[Request] = {
    new RequestInterpolator(scopes).interpolate(request)
  }

  private def findProperty(response: Response, path: String): Either[String, String] = {
    response.asJson match {
      case Right(json) => findProperty(json, path) match {
        case Some(value) => Right(value)
        case None        => Left("Could not find property: '%s'".format(path))
      }
      case Left(error) => Left(error)
    }
  }

  private def findProperty(json: JsonElement, path: String): Option[String] = {
    findProperty(json, path.split("/").toList)
  }

  private def findProperty(json: JsonElement, path: List[String]): Option[String] = {
    path match {
      // first is not the last element in the path, so look up first in the JSON object.
      case first :: rest => 
        json.isJsonObject match {
          case true => 
            val obj = json.getAsJsonObject()
            obj.has(first) match {
              case true => findProperty(obj.get(first), rest)
              case false => None
            }
          case false => None
        }

      // We've come to the end of the path.  If we have a primitive, return it.
      case _ => 
        json.isJsonPrimitive match {
          case true => 
            Some(json.toString)
          case false => 
            None
        }
      }
  }

  def allowInconsistentSslCertificates(): DefaultHttpClient = {
    import org.apache.http.conn.ssl.SSLSocketFactory
    import org.apache.http.conn.ssl.X509HostnameVerifier
    import org.apache.http.conn.scheme._
    import org.apache.http.impl.conn._
    import javax.net.ssl._ 
    import java.security.cert._ 

    val trustManager: X509TrustManager = new X509TrustManager() {
      def checkClientTrusted(xcs: Array[X509Certificate], string: String): Unit = {
      }
      def checkServerTrusted(xcs: Array[X509Certificate], string: String): Unit = {
      }
      def getAcceptedIssuers(): Array[X509Certificate] = {
        return null
      }
    }

    val hostnameVerifier: HostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
    HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

    val client: DefaultHttpClient = new DefaultHttpClient()

    val registry: SchemeRegistry = new SchemeRegistry()
    val sslContext: SSLContext = SSLContext.getInstance("TLS")
    sslContext.init(null, Array(trustManager), null)
    val socketFactory: SSLSocketFactory = new SSLSocketFactory(sslContext, hostnameVerifier.asInstanceOf[X509HostnameVerifier])
    registry.register(new Scheme("https", 443, socketFactory))
    registry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()))
    val mgr = new PoolingClientConnectionManager(registry)
    new DefaultHttpClient(mgr, client.getParams())
  }
}
