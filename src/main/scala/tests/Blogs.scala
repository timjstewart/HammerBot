package tests

import tjs.hammerbot._

object Blogs {
 
 def getSuite() = suite("Blog Tests", 

  config(
    "secret"     -> "abcdcafe",
    "usersHost"  -> "localhost:9000"),


  test("Retrieve Blog Test",

    get("http://${blogHost}/blogs")
      .withHeader("secret", "${secret}")
      .withCookie("foo", "bar")
      .expect { _ 
        .statusCodeEquals(200)
        .contentTypeContains("application/json")
        .jsonPropertyEquals("blogs/count", 120)
      },

    get("http://${blogHost}/blogs/32")
      .expect { _
        .contentTypeContains("application/json")
        .saveJsonProperty("blog/user_id", "user_id")
      },

    get("http://${usersHost}/users/${user_id}")
      .withCookie("key", "${system_a}")
      .expect { _
        .saveBody("key-1")
        .saveBodyMatch("""id: (\d+)""".r, "key-2")
        .statusCodeEquals(200)
      }
  ),


  test("Create Blog",
    post("http://${blogHost}/blogs")
      .withBody("""{blog: { title: "Hi" } }""")
      .expect { _
        .statusCodeEquals(201)
      }),


  test("Modify Blog",
    put("http://${blogHost}/blogs/120")
      .withBody("""{blog: { id: 120, title: "Hi" } }""")
      .statusCodeEquals(200)),


  test("Delete Blog",
    delete("http://${blogHost}/blogs/120")
      .statusCodeEquals(200)))
 
}
