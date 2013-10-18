package tjs.hammerbot.cli

/** Parse command line arguments into an Arguments object.  
  *
  * Used by the CommandLineProcessor 
  */
object Arguments {

  /** parse the command line and return either an error or an Arguments object. */
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

/** Arguments recognized by the CommandLineProcessor.
  *
  * @param command the command to run (e.g. run, print, help)
  * @param debug whether or not to run in debug mode
  * @param a list of test/suite names to operate on
  */
case class Arguments(
  val command: String,
  val debug:   Boolean,
  val names:   List[String]
) {
  /** turn on debug mode */
  def withDebug = Arguments(command, true, names)

  /** set the command to be run */
  def withCommand(command: String) = Arguments(command, debug, names)

  /** add a test/suite name to the list of tests/suites to operate on */
  def withName(name: String) = Arguments(command, debug, names ::: List(name))
}

