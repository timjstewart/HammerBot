package driver

import tjs.hammerbot._

object Blogs {
 
 def getSuite() = suite("Blog Tests", 

  config(
    "secret"    -> "abcdcafe",
    "usersHost" -> "localhost:9000"),


  test("Retrieve Blog Test",

    get("http://${blogHost}/blogs")
      .withHeader("secret", "${secret}")
      .withCookie("foo", "bar")
      .expect { _ 
        .statusCodeEquals(200)
        .contentTypeContains("application/json")
        .jsonPropertyEquals("blogs/count", 120)
        .jsonPropertyExists("blogs/count")
        .jsonPropertyDoesNotExist("blogs/version")
        .jsonPropertyDoesNotEqual("blogs/count", 121)
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
        .statusCodeDoesNotEqual(200)
      }),


  test("Modify Blog",
    put("http://${blogHost}/blogs/120")
      .withBody("""{blog: { id: 120, title: "Hi" } }""")
      .expect { _
        .bodyDoesNotContain("blorg")
        .bodyDoesNotEqual("""{ blog: { id: 120, user_id: 20902 } }!""")
        .statusCodeEquals(200)
      }),


  test("Delete Blog",
    delete("http://${blogHost}/blogs/120")
      .statusCodeIsNotInRange(201, 300)
      .statusCodeEquals(200)))
 
}
