package tjs.http_test.model

import scala.util.matching.Regex
import scala.xml.{ XML, Elem }
import com.google.gson.{ JsonElement }

sealed abstract class Tree(
  val name: String) 

case class Branch(
  override val name: String,
  val branches:      Seq[Tree]) extends Tree(name)

case class Leaf(
  override val name: String,
  val suiteConfig:   Config,
  tests:             Seq[Test]) extends Tree(name)

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


