package tjs.hammerbot.runner

import java.net.{ UnknownHostException, ConnectException, SocketTimeoutException }
import javax.net.ssl.{ SSLContext, HttpsURLConnection, HostnameVerifier, X509TrustManager }
import java.security.cert.{ X509Certificate }
import org.apache.http.impl.client.{ DefaultHttpClient }
import org.apache.http.client.methods.{ HttpGet, HttpPost, HttpPut, HttpOptions, HttpHead, HttpDelete }
import org.apache.http.params.{ HttpParams, HttpConnectionParams }
import org.apache.http.conn.ssl.{ SSLSocketFactory, X509HostnameVerifier }
import org.apache.http.conn.scheme.{ Scheme, SchemeRegistry, PlainSocketFactory }
import org.apache.http.impl.conn.{ PoolingClientConnectionManager }
import scala.io.{ Source }

import tjs.hammerbot.model._

class TrustingTrustManager extends X509TrustManager {
  def checkClientTrusted(xcs: Array[X509Certificate], string: String): Unit = Unit
  def checkServerTrusted(xcs: Array[X509Certificate], string: String): Unit = Unit
  def getAcceptedIssuers(): Array[X509Certificate] = null
}

case class CallResult(
  val elapsedMilliseconds: Int,
  val result: Either[String, Response])

class HttpRequest(
  val request: Request,
  val timeOut: Option[Int]
) {
  def send(): CallResult = {
    val httpClient = createHttpClient()

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
      case ex => Left("Unexpected Exception: %s".format(ex.getMessage))
    }

    CallResult(elapsedTime, result)
  }

  private def createHttpClient(): DefaultHttpClient = {
    val trustManager: X509TrustManager = new TrustingTrustManager()
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

  private def checkTimeOut(timeOut: Option[Int], elapsedTime: Int): Unit = timeOut match {
    case Some(millis) => if (elapsedTime > millis) throw new TimeOutException(millis, elapsedTime)
    case None => Unit
  }

}
