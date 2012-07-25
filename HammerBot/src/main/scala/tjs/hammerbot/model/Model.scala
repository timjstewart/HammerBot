package tjs.hammerbot.model

import scala.util.matching.Regex
import scala.xml.{ XML, Elem }
import com.google.gson.{ JsonElement }

/** A named hierarchy of Tests.
  *
  * @param name the name of the hierarchy
  */
sealed abstract class Suite(
  val name: String) 

/** A named collection of Suites.
  *
  * @param name the name of the collection of Suites.
  * @param suites the suites that belong to this collection.
  */
case class SuiteGroup(
  override val name: String,
  val suites:        Seq[Suite]) extends Suite(name)

/** A named collection of Tests along with some configuration values specific
  * to the collection of Tests. 
  *
  * @param name the name of the TestGroup.
  * @param suiteConfig immutable configuration values used by this TestGroup.
  * @param tests the tests that make up this TestGroup
  */
case class TestGroup(
  override val name: String,
  val suiteConfig:   Config,
  tests:             Seq[Test]) extends Suite(name)

/** A Test which is comprised of a collection of Calls to be made to one or
  * more servers.
  *
  * @param name the Test's name
  * @param calls the Calls to one or more HTTP servers that make up this Test.
  */
case class Test(
  val name:  String,
  val calls: Seq[Call])

/** A trait that can be mixed in to any object that has a collection of
  * Headers.
  */
trait HasHeaders {

  /** @return the Headers that the extended object contains */
  def headers: Seq[Header]

  /** Looks up a Header named 'name' in the collection of Headers. */
  def lookupHeader(name: String): Option[Header] =
    headers.find(_.name == name)

}

/** A trait that can be mixed in to any object that has a collection of
  * Cookies.
  */
trait HasCookies {

  /** @return the Cookies that the extended object contains */
  def cookies: Seq[Cookie]

  /** Looks up a Cookie named 'name' in the collection of Cookies. */
  def lookupCookie(name: String): Option[Cookie] =
    cookies.find(_.name == name)

}


