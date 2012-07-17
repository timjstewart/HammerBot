package tjs.http_test.model

case class Request(
  val method:       Method,
  val uri:          String,
  val headers:      Seq[Header],
  val cookies:      Seq[Cookie],
  val body:         Option[String]
) extends HasHeaders with HasCookies {

  def this(method: Method, uri: String) = 
    this(method, uri, List(), List(), None)

  def withHeader(name: String, value: String) = 
    new Request(method, uri, headers ++ Seq(Header(name, value)), cookies, body)

  def withCookie(name: String, value: String) = 
    new Request(method, uri, headers, cookies ++ Seq(new Cookie(name, value)), body)

  def withBody(body: String) = 
    new Request(method, uri, headers, cookies, Some(body))
}
