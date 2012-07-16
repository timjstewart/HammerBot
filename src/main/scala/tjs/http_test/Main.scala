package tjs.http_test

object Main extends App {

  import model._
  import reporters._
  import runner._

  val all = suites("All",
      
    suites("Social", 
      suite("Blog Tests", 

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
            .statusCodeEquals(200)),

        test("Delay Test",
          get("http://${blogHost}/blogs/delay")
            .timeOut(600)),

        test("Get Tags",
          get("http://${blogHost}/blogs")),

        test("Skipped Call",
          get("http://${blogHost}/blogs")
            .contentTypeContains("application/json")
            .statusCodeEquals(200))

        )
      )
    )

  val hosts = config(
    "blogHost" -> "localhost:9000",
    "tagsHost" -> "localhost:8000"
  )

  val keys = config(
    "system_a" -> "FEEDBEEF",
    "system_b" -> "3141569"
  )

  val conf = hosts + keys

  val metrics = new MetricsReporter()
  val console = new ConsoleReporter(ConsoleReporter.debug)

  val allReporters = new CompositeReporter(metrics, console)

  new Runner(conf, allReporters).run(all)

  println(metrics.toString)
}
