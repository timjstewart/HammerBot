package tests

import tjs.hammerbot._

object Tags {

  def getSuite() = suite("Tags Tests", 

   test("Google Test",
     get("https://mail.google.com/mail")
      .bodyContains("SetGmailCookie")),


   test("Delay Test",
     get("http://${blogHost}/blogs/delay")
       .timeOut(600)
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

