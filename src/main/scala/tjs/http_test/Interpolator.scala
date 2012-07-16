package tjs.http_test.utils

import scala.util.matching.Regex
import tjs.http_test.model._

case class InterpolationResult[T](
  result:          T,
  undefinedValues: Seq[String])

class Interpolator(
  val config: Seq[IConfig]
) {

  def interpolate(text: String): InterpolationResult[String] = {
    val re = """\$\{([a-zA-Z0-9_]+)\}""".r

    val variables = re
      .findAllIn(text)
      .matchData
      .toList
      .map(x=> x.group(1))
      .distinct

    interpolate_(variables, text, Seq())
  }

  private def interpolate_(
    variables:       List[String], 
    text:            String, 
    undefinedValues: Seq[String]
  ): InterpolationResult[String] = variables match {
    case first :: rest => lookupVariable(first) match {
      case Some(value) => interpolate_(rest, text.replace("${%s}".format(first), value.toString), undefinedValues)
      case None => interpolate_(rest, text, undefinedValues ++ Seq(first))
    }

    case _ => InterpolationResult(text, undefinedValues)
  }

  private def lookupVariable(name: String): Option[Any] = 
    lookupVariable_(name, config.toList)

  private def lookupVariable_(name: String, config: List[IConfig]): Option[Any] = config match {
    case first :: rest => first.get(name) match {
      case Some(value) => Some(value)
      case None => lookupVariable_(name, rest)
    }
    case _ => None
  }

}
