package tjs.hammerbot.runner

import scala.util.matching.Regex
import com.google.gson._
import tjs.hammerbot.model._
import tjs.hammerbot.utils._

object Operations {
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

}
