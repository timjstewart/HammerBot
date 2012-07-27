# HammerBot #

An HTTP/HTTPS service testing framework.

*Hammer - verb. to work at constantly, to quesiton in a relentless
manner.*

## Examples ##

### Common Imports ####

* This is all you need to define Suites and Tests:

        import tjs.hammerbot._

* To actually run any tests, you'll need:

        import tjs.hammerbot.cli._

### Tests ###

* Make a GET request to a service.  The only reason this would fail is if
the service could not be contacted.

        test("Call my service",
          get("http://myservice.mycompany.com"))

* Make a GET request to a service and ensure that the status code is
  200:

        test("Call my service",
          get("http://myservice.mycompany.com")
            .statusCodeEquals(200))

* Make a GET request to a service and ensure that the status code is
  between 400 and 403:

        test("Call my service",
          get("http://myservice.mycompany.com")
            .statusCodeIsInRange(400, 403))

* Make a GET request to a service and ensure that the content type is
  exactly "application/json":

        test("Call my service",
          get("http://myservice.mycompany.com")
            .contentTypeIs("application/json"))

* Make a GET request to a service and ensure that the content type
  contains "application/json" (useful for services that contain the character
  encoding too):

        test("Call my service",
          get("http://myservice.mycompany.com")
            .contentTypeContains("application/json"))


* Make a GET request to a service and ensure that the response body
  contains a JSON property with a specific value:

        test("Call my service",
          get("http://myservice.mycompany.com")
            .jsonPropertyEquals("user/id", 12345))

* Make a GET request to a service and save a JSON property under a specific
  key:

        test("Call my service",
          get("http://myservice.mycompany.com")
            .saveJsonProperty("user/id", "user_id"))

* Make a GET request to a service using the value of a key.  If the key is not
  found, the test will fail:

        test("Call my service", 
          get("http://myservice.mycompany.com/users/${user_id}")

### Test Suites ###

* Create a Test Suite that contains a single test:

        suite("My Test Suite",
          test("Test One",
            get("http://myservice.mycompany.com/users")))


* Create a Test Suite that contains two tests:

        suite("My Test Suite",

          test("Test One",
            get("http://myservice.mycompany.com/users")),

          test("Test Two",
            get("http://myservice.mycompany.com/blogs")))

* Create a collection of Test Suites:

        suites("My Test Suites",

          suite("My First Test Suite",

            test("Test One",
              get("http://myservice.mycompany.com/users")),

            test("Test Two",
              get("http://myservice.mycompany.com/blogs")))


          suite("My Second Suite",

            test("Test Three",
              get("http://myservice.mycompany.com/comments")),

            test("Test Four",
              get("http://myservice.mycompany.com/tags"))))


### Configuration ###

Configuration can be used to set more dynamic values in one place so that it
can be used by multiple suites:

* Create a configuration object with two key/value pairs in it:

        val conf = config(
          "secret"     -> "abcdcafe",
          "usersHost"  -> "localhost:9000")

* Create a configuration that is only used by one Suite:

        suite("My Test Suite",

          config(
            "secret"     -> "abcdcafe",
            "usersHost"  -> "localhost:9000"),

          test("Test One",
            get("http://myservice.mycompany.com/users")))

* Configuration objects can be merged with the '+' operator;

        val confA = config(
          "a" -> "100",
          "b" -> "200")
               
        val confB = config(
          "c" -> "300",
          "d" -> "400")

        val allConf = confA + confB

### Set Up and Tear Down ###

        suite("My Test Suite",

          setUp(
            get("http://myservice.mycompany.com/createUser")),
          
          tearDown(
            get("http://myservice.mycompany.com/deleteUser")),

          test("Test One",
            get("http://myservice.mycompany.com/users")))

### Running Suites ###

* Here is an example main method belonging to a Main object:

        override def main(args: Array[String]): Unit = {

          val socialSuite = suites( ... )

          val hosts = config(
            "blogHost" -> "localhost:9000",
            "tagsHost" -> "localhost:8000"
          )

          CommandLineProcessor.run(args, socialSuite, hosts)
        }

* Running the sample driver from SBT:

        sbt "project driver" "run run"

* Running the sample driver from SBT in debug mode:

        sbt "project driver" "run run -d"
