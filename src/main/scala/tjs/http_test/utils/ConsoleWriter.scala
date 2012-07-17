package tjs.http_test.utils

class ConsoleWriter() {

  var level: Int = 0

  var stdinRedirected: Boolean = System.console == null
  
  def indent() = level = level + 1

  def dedent() = level = level - 1

  def indent(func: => Unit): Unit = {
    indent() ; func ; dedent()
  }

  def bold(text: String)   = markup(text, Console.BOLD)
  def red(text: String)    = markup(text, Console.RED)
  def green(text: String)  = markup(text, Console.GREEN)
  def yellow(text: String) = markup(text, Console.YELLOW)

  private def markup(text: String, escape: String) =  
    stdinRedirected match {
      case false => "%s%s%s".format(escape, text, Console.RESET)
      case true  => text
    }

  def println(text: String): Unit = 
    Console.println("%s%s".format("  " * level, text))

  def print(text: String): Unit = 
    Console.print("%s%s".format("  " * level, text))

  
}
