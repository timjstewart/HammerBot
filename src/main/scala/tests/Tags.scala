package tests

import tjs.hammerbot._

object Tags {

  def getSuite() = suite("Tags", 

   test("Delay Test",
     get("http://${blogHost}/blogs/delay")
       .timeOut(600)),


   test("Get Tags",
     get("http://${blogHost}/blogs")
      .statusCodeEquals(200)),


   test("Skipped Call",
     get("http://${blogHost}/blogs")
       .contentTypeContains("application/json")
       .statusCodeEquals(200))

   )
}


