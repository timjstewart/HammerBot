package tjs.hammerbot.model

import scala.xml.{ XML, Elem }
import com.google.gson.{ JsonElement, JsonSyntaxException, JsonParser  }

case class Response(
  val statusCode:   Int,
  val headers:      Seq[Header],
  val cookies:      Seq[Cookie],
  val body:         String
) extends HasHeaders with HasCookies {
  lazy val asJson: Either[String,JsonElement] = 
    try {
      Right(new JsonParser().parse(body))
    } catch {
      case ex:JsonSyntaxException => Left(ex.getMessage)
    }

  lazy val asXml:  Either[String,Elem] =
    try {
      Right(XML.loadString(body))
    } catch {
      case ex: Throwable => Left(ex.getMessage)
    }
}

