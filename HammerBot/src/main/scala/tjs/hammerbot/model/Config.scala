package tjs.hammerbot.model

trait IConfig {
  def get(name: String): Option[Any]
  def toMap: Map[String, Any]
  def +(other: IConfig): IConfig
}

case class Config(values: Map[String, Any]) extends IConfig {
  override def get(name: String): Option[Any] = values.get(name)
  override def toMap: Map[String, Any] = values
  override def +(other: IConfig): IConfig = Config(values ++ other.toMap)
}

object Config {
  val empty = Config(Map[String, Any]())
}

case class MutableConfig(var values: Map[String, Any]) extends IConfig {
  override def get(name: String): Option[Any] = values.get(name)
  override def toMap: Map[String, Any] = values
  override def +(other: IConfig): IConfig = MutableConfig(values ++ other.toMap)
  def put(name: String, value: Any): Unit = values = values + (name -> value)
}

object MutableConfig {
  def empty() = new MutableConfig(Map[String, Any]())
}


