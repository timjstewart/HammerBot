package tjs.hammerbot.model

import scala.util.matching.Regex

case class Call(
  val request:    Request,
  val operations: Seq[Operation],
  val timeOut:    Option[Int]
) {
  def this(request: Request) = this(request, Seq(), None)

  // Methods that modify the Call

  def timeOut(milliseconds: Int) = Call(request, operations, Some(milliseconds))
  def noTimeOut() = Call(request, operations, None)

  // Methods that modify the request

  def withHeader(name: String, value: String): Call = Call(request.withHeader(name, value), operations, None)
  def withCookie(name: String, value: String): Call = Call(request.withCookie(name, value), operations, None)
  def withBody(body: String): Call = Call(request.withBody(body), operations, None)

  // Methods that add operations to the Call object.

  def withOperations(newOperations: Seq[Operation]): Call = 
    Call(request, operations ++ newOperations, None)

  def bodyContains(text: String): Call = 
    Call(request, operations ++ Seq(BodyShouldContain(text)), None)

  def bodyMatches(regEx: Regex): Call = 
    Call(request, operations ++ Seq(BodyShouldMatch(regEx)), None)

  def contentTypeIs(contentType: String): Call = 
    Call(request, operations ++ Seq(ContentTypeEquals(contentType)), None)

  def contentTypeMatches(contentType: Regex): Call = 
    Call(request, operations ++ Seq(ContentTypeMatches(contentType)), None)

  def contentTypeContains(contentType: String): Call = 
    Call(request, operations ++ Seq(ContentTypeContains(contentType)), None)

  def jsonPropertyEquals(path: String, value: Any): Call = 
    Call(request, operations ++ Seq(JsonPropertyEquals(path, value)), None)

  def statusCodeEquals(n: Int): Call = 
    Call(request, operations ++ Seq(StatusCodeEquals(n)), None)

  def statusCodeIsInRange(low: Int, high: Int): Call = 
    Call(request, operations ++ Seq(StatusCodeIsInRange(low, high)), None)

  def headerEquals(name: String, value: String): Call = 
    Call(request, operations ++ Seq(HeaderEquals(Header(name, value))), None)

  def cookieExists(name: String): Call = 
    Call(request, operations ++ Seq(CookieIsPresent(name)), None)

  def cookieEquals(name: String, value: String): Call = 
    Call(request, operations ++ Seq(CookieHasValue(name, value)), None)

  def bodyEquals(text: String): Call = 
    Call(request, operations ++ Seq(BodyShouldEqual(text)), None)

  def saveBody(key: String): Call = 
    Call(request, operations ++ Seq(SaveBodyContents(key)), None)

  def saveBodyMatch(regex: Regex, key: String): Call = 
    Call(request, operations ++ Seq(SaveBodyMatch(regex, key)), None)

  def saveJsonProperty(path: String, key: String): Call = 
    Call(request, operations ++ Seq(SaveJsonProperty(path, key)), None)

  def withCustom(description: String, func: (Response, IConfig) => Result): Call = 
    Call(request, operations ++ Seq(Custom(description, func)), None)

  def expect(func: Call => Call) = func(this)
}


