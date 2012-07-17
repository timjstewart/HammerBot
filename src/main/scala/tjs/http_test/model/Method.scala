package tjs.http_test.model

sealed abstract class Method(val text: String) {
  override def toString = text
}

case class Get()     extends Method("GET")
case class Put()     extends Method("PUT")
case class Post()    extends Method("POST")
case class Options() extends Method("OPTIONS")
case class Head()    extends Method("HEAD")
case class Delete()  extends Method("DELETE")


