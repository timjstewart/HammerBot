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

  def bodyDoesNotContain(text: String): Call = 
    Call(request, operations ++ Seq(BodyShouldNotContain(text)), None)

  def bodyDoesNotMatch(regEx: Regex): Call = 
    Call(request, operations ++ Seq(BodyShouldNotMatch(regEx)), None)

  def contentTypeIs(contentType: String): Call = 
    Call(request, operations ++ Seq(ContentTypeEquals(contentType)), None)

  def contentTypeMatches(contentType: Regex): Call = 
    Call(request, operations ++ Seq(ContentTypeMatches(contentType)), None)

  def contentTypeContains(contentType: String): Call = 
    Call(request, operations ++ Seq(ContentTypeContains(contentType)), None)

  def jsonPropertyEquals(path: String, value: Any): Call = 
    Call(request, operations ++ Seq(JsonPropertyEquals(path, value)), None)

  def jsonPropertyDoesNotEqual(path: String, value: Any): Call = 
    Call(request, operations ++ Seq(JsonPropertyDoesNotEqual(path, value)), None)

  def statusCodeEquals(n: Int): Call = 
    Call(request, operations ++ Seq(StatusCodeEquals(n)), None)

  def statusCodeDoesNotEqual(n: Int): Call = 
    Call(request, operations ++ Seq(StatusCodeDoesNotEqual(n)), None)

  def statusCodeIsNotInRange(low: Int, high: Int): Call = 
    Call(request, operations ++ Seq(StatusCodeIsNotInRange(low, high)), None)

  def headerEquals(name: String, value: String): Call = 
    Call(request, operations ++ Seq(HeaderEquals(Header(name, value))), None)

  def headerDoesNotEqual(name: String, value: String): Call = 
    Call(request, operations ++ Seq(HeaderDoesNotEqual(Header(name, value))), None)

  def cookieDoesNotExist(name: String): Call = 
    Call(request, operations ++ Seq(CookieIsNotPresent(name)), None)

  def cookieDoesNotEqual(name: String, value: String): Call = 
    Call(request, operations ++ Seq(CookieDoesNotHaveValue(name, value)), None)

  def bodyEquals(text: String): Call = 
    Call(request, operations ++ Seq(BodyShouldEqual(text)), None)

  def bodyDoesNotEqual(text: String): Call = 
    Call(request, operations ++ Seq(BodyShouldNotEqual(text)), None)

  def saveBody(key: String): Call = 
    Call(request, operations ++ Seq(SaveBodyContents(key)), None)

  def saveBodyMatch(regex: Regex, key: String): Call = 
    Call(request, operations ++ Seq(SaveBodyMatch(regex, key)), None)

  def saveJsonProperty(path: String, key: String): Call = 
    Call(request, operations ++ Seq(SaveJsonProperty(path, key)), None)

  def withCustom(op: CustomOperation): Call = 
    Call(request, operations ++ Seq(new CustomOperationHolder(op)), None)

  def expect(func: Call => Call) = func(this)
}


