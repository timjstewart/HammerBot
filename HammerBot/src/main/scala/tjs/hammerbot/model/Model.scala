package tjs.hammerbot.model

import scala.util.matching.Regex
import scala.xml.{ XML, Elem }
import com.google.gson.{ JsonElement }

sealed abstract class Suite(
  val name: String) 

case class SuiteGroup(
  override val name: String,
  val branches:      Seq[Suite]) extends Suite(name)

case class TestGroup(
  override val name: String,
  val suiteConfig:   Config,
  tests:             Seq[Test]) extends Suite(name)

case class Test(
  val name:  String,
  val calls: Seq[Call])

trait HasHeaders {
  def headers: Seq[Header]
  def lookupHeader(name: String): Option[Header] = {
    headers.find(h => h.name == name)
  }
}

trait HasCookies {
  def cookies: Seq[Cookie]
  def lookupCookie(name: String): Option[Cookie] = {
    cookies.find(c => c.name == name)
  }
}


