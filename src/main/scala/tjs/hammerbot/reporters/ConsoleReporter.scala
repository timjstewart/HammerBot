package tjs.hammerbot.reporters

import tjs.hammerbot.model._
import tjs.hammerbot.utils._

object ConsoleReporter {
  class Parameters(
    val printOperations:  Boolean,
    val printRequests:    Boolean,
    val printResponses:   Boolean,
    val printConfig:      Boolean
  )

  val default = new Parameters(true, false, false, false)
  val quiet   = new Parameters(false, false, false, false)
  val debug   = new Parameters(true, true, true, true)
}

class ConsoleReporter(
  parameters: ConsoleReporter.Parameters = ConsoleReporter.default
) extends Reporter {

  val writer: ConsoleWriter = new ConsoleWriter()

  var expectationsFailed: Boolean = false
  var lastRequest: Request = null
  var lastResponse: Response = null

  override def suiteStarting(suiteName: String): Unit = {
    writer.println("Suite: %s".format(writer.bold(suiteName)))
    writer.indent()
  }
    
  override def suiteComplete(suiteName: String): Unit = {
    writer.dedent()
  }

  override def testStarting(testName: String): Unit = {
    writer.println("Test: %s".format(writer.bold(testName)))
    writer.indent()
  }

  override def testComplete(testName: String, succeeded: Boolean): Unit = {
    writer.dedent()
  }

  override def requestSending(request: Request): Unit = {
    writer.print("Send: %s %s".format(request.method, request.uri))
    expectationsFailed = false
  }

  override def requestNotSent(request: Request, reason: String, config: IConfig): Unit = {
    writer.println("%s: %s %s".format(writer.red("FAIL"), request.method, request.uri))
    writer.indent {
      writer.println("Reason: Could not send Request: %s".format(reason))
      dumpRequest(request)
      dumpConfig(config)
    }
  }

  override def requestFailed(request: Request, reason: String, elapsedMillis: Int): Unit = {
    rewriteLine(writer.red("FAIL"), "%s %s".format(request.method, request.uri), elapsedMillis)
    writer.indent { 
      writer.println("Reason: %s".format(reason))
      dumpRequest(request)
    }
  }

  override def responseReceived(request: Request, response: Response, config: IConfig, elapsedMillis: Int): Unit = {
    rewriteLine("Complete", "%s %s".format(request.method, request.uri), elapsedMillis)
    lastRequest = request
    lastResponse = response
    writer.indent()
    if (parameters.printConfig) {
      dumpConfig(config)
    }
    if (parameters.printRequests) {
      dumpRequest(request)
    }
    if (parameters.printResponses) {
      dumpResponse(response)
    }
  }

  override def operationStarting(operation: Operation): Unit = {
    if (parameters.printOperations) {
      writer.print("%s: %s".format(writer.yellow("Test"), operation.description))
    }
  }

  override def operationSucceeded(operation: Operation): Unit = {
    if (parameters.printOperations) {
      rewriteLine(writer.green("Pass"), operation.description)
    }
  }

  override def operationFailed(operation: Operation, message: String): Unit = {
    if (parameters.printOperations) {
      rewriteLine(writer.red("FAIL"), message)
    }
    expectationsFailed = true
  }

  def operationSkipped(operation: Operation, message: String): Unit = Unit

  override def operationsComplete(): Unit = {
    if (expectationsFailed) {
      writer.println("Not all expectations were met.  Dumping Request and Response...")
      dump()
    }
    writer.dedent()
  }

  private def dump(): Unit = {
    writer.indent {
      dumpRequest(lastRequest)
      dumpResponse(lastResponse)
    }
  }

  private def dumpResponse(response: Response): Unit = {
    if (response == null) {
      writer.println("No Response")
    } else {
      writer.println("Response:")
      writer.indent {
        writer.println("Status Code: %s".format(response.statusCode))

        if (!response.headers.isEmpty) {
          writer.println("Headers:")
          writer.indent {
            response.headers.foreach {
              h =>
                writer.println("%s: %s".format(h.name, h.value))
            }
          }
        }

        if (!response.cookies.isEmpty) {
          writer.println("Cookies:")
          writer.indent {
            response.cookies.foreach {
              h =>
                writer.println("%s: %s".format(h.name, h.value))
            }
          }
        }

        if (!response.body.isEmpty) {
          writer.println("Body: [%s]".format(response.body))
        }
      }
    }
  }

  private def rewriteLine(status: String, text: String): Unit = writer.stdinRedirected match {
    case false =>
      writer.print("\r")
      writer.println("%s: %s".format(status, text))

    case true =>
      Console.println("... %s".format(status))
  }

  private def rewriteLine(status: String, text: String, elapsed: Int): Unit = writer.stdinRedirected match {
    case false =>
      writer.print("\r")
      writer.println("%s: %s (%d msecs)".format(status, text, elapsed))

    case true =>
      Console.println("... %s (%d msecs)".format(status, elapsed))
  }

  private def dumpConfig(config: IConfig): Unit = {
    writer.println("Configuration:")
    writer.indent {
      config.toMap.foreach {
        pair => writer.println("%s = %s".format(pair._1, pair._2.toString))
      }
    }
  }

  private def dumpRequest(request: Request): Unit = {
    if (request == null) {
      writer.println("No Request")
    } else {
      writer.println("Request: %s %s".format(request.method, request.uri))
      writer.indent {
        if (!request.headers.isEmpty) {
          writer.println("Headers:")
          writer.indent {
            request.headers.foreach {
              h =>
                writer.println("%s: %s".format(h.name, h.value))
            }
          }
        }

        if (!request.cookies.isEmpty) {
          writer.println("Cookies:")
          writer.indent {
            request.cookies.foreach {
              h =>
                writer.println("%s: %s".format(h.name, h.value))
            }
          }
        }

        if (!request.body.isEmpty) {
          writer.println("Body: [%s]".format(request.body))
        }
      }
    }
  }
}
