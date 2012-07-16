package tjs.http_test.model

import java.util.Date

case class Cookie(
  val name:       String,
  val value:      String, 
  val domain:     String,
  val httpOnly:   Boolean,
  val secureOnly: Boolean,
  val expires:    Option[Date]
) {
  def this(name: String, value: String) = this(name, value, "/", false, false, None)

  def withValue(domain: String): Cookie = Cookie(name, value, domain, httpOnly, secureOnly, expires)
  
  def withDomain(domain: String): Cookie = Cookie(name, value, domain, httpOnly, secureOnly, expires)

  def withName(value: String): Cookie = Cookie(name, value, domain, httpOnly, secureOnly, expires)

  def withExpirationDate(expires: Date): Cookie = Cookie(name, value, domain, httpOnly, secureOnly, Some(expires))

  def withHttpOnly(httpOnly: Boolean): Cookie = Cookie(name, value, domain, httpOnly, secureOnly, expires)

  def withSecureOnly(secureOnly: Boolean): Cookie = Cookie(name, value, domain, httpOnly, secureOnly, expires)
}

