package tjs.hammerbot.cli

/** Prints out the usage/help for the CommandLineProcessor */
class Help() {
  def printHelp(): Unit = {
    println("usage: <program> COMMAND [FLAGS] [NAME ...]")
    println("Commands:")
    println("    run       runs all tests or only the tests specified by NAME")
    println("    tap       runs all tests and shows TAP report for them")
    println("    print     prints out all suite and test names.")
    println("    help      prints this helpful text")
    println("Flags:")
    println("    -d        print out debug information")
    println("    -v        print version information")
  }
}
