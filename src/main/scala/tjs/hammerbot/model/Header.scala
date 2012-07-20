package tjs.hammerbot.model

case class Header(
  val name:  String,
  val value: String
) {
  def withValue(value: String): Header = Header(name, value)

  def withName(name: String): Header = Header(name, value)
}


