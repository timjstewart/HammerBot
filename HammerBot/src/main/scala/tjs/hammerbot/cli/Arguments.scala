package tjs.hammerbot.cli

object Arguments {

  def parse(args: Array[String]): Either[String, Arguments] = args.toList match {
    case arg :: rest => parseCommand(arg, rest, Arguments.empty)
    case _           => Right(Arguments.empty) 
  }

  private def parseCommand(
    command:      String, 
    commandFlags: List[String], 
    result:       Arguments
  ): Either[String, Arguments] = {
    parseFlags(commandFlags, result.withCommand(command))
  }  

  private def parseFlags(
    commandFlags: List[String], 
    result:       Arguments
  ): Either[String, Arguments] = commandFlags match {
    case flag :: rest => isFlag(flag) match {
      case true  => parseFlag(flag, rest, result)
      case false => parseNames(commandFlags, result)
    }
    case _ => Right(result)
  }

  private def isFlag(s: String): Boolean = s.startsWith("-")

  private def parseFlag(
    flag:         String,
    commandFlags: List[String], 
    result:       Arguments
  ): Either[String, Arguments] = flag match {
    case "-d" => parseFlags(commandFlags, result.withDebug)
    case _    => Left("Unrecognized flag: %s".format(flag))
  }

  private def parseNames(
    names:   List[String], 
    result:  Arguments
  ): Either[String, Arguments] = names match {
    case name :: rest => parseNames(rest, result.withName(name))
    case _            => Right(result)
  }

  private def empty = Arguments("help", false, List())
}

case class Arguments(
  val command: String,
  val debug:   Boolean,
  val names:   List[String]
) {
  def withDebug = Arguments(command, true, names)
  def withCommand(command: String) = Arguments(command, debug, names)
  def withName(name: String) = Arguments(command, debug, names ::: List(name))
}

