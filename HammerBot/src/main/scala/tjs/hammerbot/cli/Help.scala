package tjs.hammerbot.cli

class Help() {
  def printHelp(): Unit = {
    println("usage: <program> COMMAND [FLAGS] [NAME ...]")
    println("Commands:")
    println("    run       runs all tests or only the tests specified by NAME")
    println("    print     prints out all suite and test names.")
    println("    help      prints this helpful text")
    println("Flags:")
    println("    -d        print out debug information")
    println("    -v        print version information")
  }
}
