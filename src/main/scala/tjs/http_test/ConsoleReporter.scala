package tjs.http_test.reporters

import tjs.http_test.model._

class ConsoleReporter() extends Reporter {

  var level: Int = 0
  var expectationsFailed: Boolean = false
  var lastRequest: Request = null
  var lastResponse: Response = null

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
  
  def markup(text: String, escape: String) =  
    stdinRedirected match {
      case false => "%s%s%s".format(escape, text, Console.RESET)
      case true  => text
    }

  def println(text: String): Unit = 
    Console.println("%s%s".format("  " * level, text))

  def print(text: String): Unit = 
    Console.print("%s%s".format("  " * level, text))

  override def suiteStarting(suiteName: String): Unit = {
    println("Suite: %s".format(bold(suiteName)))
    indent()
  }
    
  override def suiteComplete(suiteName: String): Unit = {
    dedent()
  }

  override def testStarting(testName: String): Unit = {
    println("Test: %s".format(bold(testName)))
    indent()
  }

  override def testComplete(testName: String, succeeded: Boolean): Unit = {
    dedent()
  }

  override def requestSending(request: Request): Unit = {
    print("Send: %s %s".format(request.method, request.uri))
    expectationsFailed = false
  }

  override def requestNotSent(request: Request, reason: String, config: IConfig): Unit = {
    println("%s: %s %s".format(red("FAIL"), request.method, request.uri))
    indent {
      println("Reason: Could not send Request: %s".format(reason))
      dumpRequest(request)
      dumpConfig(config)
    }
  }

  override def requestFailed(request: Request, reason: String, elapsedMillis: Int): Unit = {
    rewriteLine(red("FAIL"), "%s %s".format(request.method, request.uri), elapsedMillis)
    indent { 
      println("Reason: %s".format(reason))
      dumpRequest(request)
    }
  }

  override def responseReceived(request: Request, response: Response, elapsedMillis: Int): Unit = {
    rewriteLine("Complete", "%s %s".format(request.method, request.uri), elapsedMillis)
    lastRequest = request
    lastResponse = response
    indent()
  }

  override def operationStarting(operation: Operation): Unit = {
    print("%s: %s".format(yellow("Test"), operation.description))
  }

  override def operationSucceeded(operation: Operation): Unit = {
    rewriteLine(green("Pass"), operation.description)
  }

  override def operationFailed(operation: Operation, message: String): Unit = {
    rewriteLine(red("FAIL"), message)
    expectationsFailed = true
  }

  def operationSkipped(operation: Operation, message: String): Unit = Unit

  override def operationsComplete(): Unit = {
    if (expectationsFailed) {
      println("Not all expectations were met.  Dumping Request and Response...")
      dump()
    }
    dedent()
  }

  private def dump(): Unit = {
    indent {
      dumpRequest(lastRequest)
      dumpResponse(lastResponse)
    }
  }

  private def dumpResponse(response: Response): Unit = {
    if (response == null) {
      println("No Response")
    } else {
      println("Response:")
      indent {
        println("Status Code: %s".format(response.statusCode))

        if (!response.headers.isEmpty) {
          println("Headers:")
          indent {
            response.headers.foreach {
              h =>
                println("%s: %s".format(h.name, h.value))
            }
          }
        }

        if (!response.cookies.isEmpty) {
          println("Cookies:")
          indent {
            response.cookies.foreach {
              h =>
                println("%s: %s".format(h.name, h.value))
            }
          }
        }

        if (!response.body.isEmpty) {
          println("Body: [%s]".format(response.body))
        }
      }
    }
  }

  private def rewriteLine(status: String, text: String): Unit = stdinRedirected match {
    case false =>
      print("\r")
      println("%s: %s".format(status, text))

    case true =>
      Console.println("... %s".format(status))
  }

  private def rewriteLine(status: String, text: String, elapsed: Int): Unit = stdinRedirected match {
    case false =>
      print("\r")
      println("%s: %s (%d msecs)".format(status, text, elapsed))

    case true =>
      Console.println("... %s (%d msecs)".format(status, elapsed))
  }

  private def dumpConfig(config: IConfig): Unit = {
    println("Configuration:")
    indent {
      config.toMap.foreach {
        pair => println("%s = %s".format(pair._1, pair._2.toString))
      }
    }
  }

  private def dumpRequest(request: Request): Unit = {
    if (request == null) {
      println("No Request")
    } else {
      println("Request: %s %s".format(request.method, request.uri))
      indent {
        if (!request.headers.isEmpty) {
          println("Headers:")
          indent {
            request.headers.foreach {
              h =>
                println("%s: %s".format(h.name, h.value))
            }
          }
        }

        if (!request.cookies.isEmpty) {
          println("Cookies:")
          indent {
            request.cookies.foreach {
              h =>
                println("%s: %s".format(h.name, h.value))
            }
          }
        }

        if (!request.body.isEmpty) {
          println("Body: [%s]".format(request.body))
        }
      }
    }
  }
}
