package tjs.hammerbot.cli

object Arguments {

  def parse(args: Array[String]): Either[String, Arguments] = 
    parseCommand(args.toList, Arguments.empty)

  private def parseCommand(
    args:    List[String], 
    result:  Arguments
  ): Either[String, Arguments] = args match {
    case command :: rest => isFlag(command) match {
      case true => flagRequiresCommand(command) match {
        case true  => Left("No command specified")
        case false => parseFlags(args, result)
      }
      case false => parseFlags(rest, result.withCommand(command))
    }
    case _ => Right(Arguments.empty)
  }  

  private def flagRequiresCommand(flag: String): Boolean = flag match {
    case "-v" => false
    case _    => true
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
    case "-v" => Right(Arguments("version", false, List()))
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

