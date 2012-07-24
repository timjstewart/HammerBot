package driver

import tjs.hammerbot._
import tjs.hammerbot.model._

object Tags {

  case class HeaderCountEquals(n: Int) extends CustomOperation {
    def description = "Header Count should equal: %d".format(n)
    def apply(response: Response, config: IConfig): Result = {
      if (response.headers.length == n)
        Success()
      else 
        Failure("%s but it was %d.".format(description, response.headers.length))
    }
  }

  def getSuite() = suite("Tags Tests", 

   test("Google Test",
     get("https://mail.google.com/mail")
      .bodyContains("SetGmailCookie")), 

   test("Delay Test",
     get("http://${blogHost}/blogs/delay")
       .timeOut(600)
       .withCustom(HeaderCountEquals(2))
       .saveBody("foo")),


   test("Get Tags",
     get("http://${blogHost}/blogs")
      .statusCodeEquals(200)),


   test("Skipped Call",
     get("http://${blogHost}/blogs")
       .contentTypeContains("application/json")
       .statusCodeEquals(200))

   )
}


