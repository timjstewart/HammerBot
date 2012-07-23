package driver

import tjs.hammerbot._
import tjs.hammerbot.model._

object Tags {
  def headerCountEquals(n: Int)(response: Response, config: IConfig): Result = {
    if (response.headers.length == n)
      Success()
    else 
      Failure("Header Count was wrong")
  }

  def getSuite() = suite("Tags Tests", 

   test("Google Test",
     get("https://mail.google.com/mail")
      .bodyContains("SetGmailCookie")), 

   test("Delay Test",
     get("http://${blogHost}/blogs/delay")
       .timeOut(600)
       .withCustom("Header count should be 3", headerCountEquals(3))
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


