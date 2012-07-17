package tjs.http_test.model

import tjs.http_test._
import scala.util.matching.Regex

sealed abstract class Operation(val description: String)

sealed abstract class Expectation(
  override val description: String
) extends Operation(description)

case class StatusCodeEquals(
  val expected: Int
) extends Expectation("Status Code should be: %d".format(expected))

case class StatusCodeIsInRange(
  val low:  Int,
  val high: Int
) extends Expectation("Status Code should be between %d and %d".format(low, high))

case class HeaderEquals(
  val header: Header
) extends Expectation("Response header: '%s' should be: '%s'".format(header.name, header.value))

case class CookieIsPresent(
  val name: String
) extends Expectation("Response should contain cookie with name: '%s'".format(name))

case class CookieHasValue(
  val name:  String,
  val value: String
) extends Expectation("Response should contain cookie with name: '%s' and value: %s".format(name, quote(value)))

case class BodyShouldEqual(
  val value: String
) extends Expectation("Response body should be equal to: %s".format(quote(value)))

case class BodyShouldContain(
  val value: String
) extends Expectation("Response body should contain: '%s'".format(value))

case class BodyShouldMatch(
  val regularExpression: Regex
) extends Expectation("Response body should match: %s".format(quote(regularExpression)))

case class SaveBodyContents(
  val key: String
) extends Expectation("Save body contents under key: '%s'".format(key))

case class SaveBodyMatch(
  val regularExpression: Regex,
  val key:               String
) extends Operation("Save body contents matching %s under key: '%s'".format(regularExpression, key))

case class ContentTypeMatches(
  val contentType: Regex
) extends Expectation("Content Type should match: %s".format(quote(contentType)))

case class ContentTypeContains(
  val contentType: String
) extends Expectation("Content Type should contain: %s".format(quote(contentType)))

case class ContentTypeEquals(
  val contentType: String
) extends Expectation("Content Type should be: %s".format(quote(contentType)))

case class SaveHeader(
  val name: String,
  val key:  String
) extends Operation("Save header value: %s under key: '%s'".format(name, key))

case class SaveCookie(
  val name: String,
  val key:  String
) extends Operation("Save cookie value: %s under key: %s".format(name, key))

case class ResponseReceivedWithinMilliseconds(
  val milliseconds: Int
) extends Expectation("Response should be received within %d milliseconds.".format(milliseconds))

case class JsonPropertyEquals(
  val propertyPath: String,
  val value:        Any
) extends Expectation("JSON property: %s should equal: %s".format(propertyPath, value))

case class JsonPropertyMatches(
  val propertyPath:      String,
  val regularExpression: Regex
) extends Expectation("Response JSON should have property: %s that matches: %s".format(propertyPath, regularExpression))

case class SaveJsonProperty(
  val propertyPath: String,
  val key:          String
) extends Operation("Save JSON property: %s under key: %s".format(propertyPath, key))

case class SaveJsonPropertyMatching(
  val propertyPath:      String,
  val regularExpression: String,
  val key:               String
) extends Operation("Save JSON property: %s matching: %s under key: %s".format(propertyPath, regularExpression))

