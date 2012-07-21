# HammerBot #

''Hammer - verb. to work at constantly, to quesiton in a relentless manner.''

## Examples ##

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
