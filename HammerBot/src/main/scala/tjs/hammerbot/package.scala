package tjs

import tjs.hammerbot.model._

package object hammerbot {

  def suite(name: String, tests: Test*): Suite = new TestGroup(name, Config.empty, tests.toSeq, None, None)
  
  def suite(name: String, setUp: TestSetUp, tests: Test*): Suite = new TestGroup(name, Config.empty, tests.toSeq, Some(setUp), None)

  def suite(name: String, tearDown: TestTearDown, tests: Test*): Suite = new TestGroup(name, Config.empty, tests.toSeq, None, Some(tearDown))

  def suite(name: String, setUp: TestSetUp, tearDown: TestTearDown, tests: Test*): Suite = new TestGroup(name, Config.empty, tests.toSeq, Some(setUp), Some(tearDown))

  def suite(name: String, config: Config, tests: Test*): Suite = new TestGroup(name, config, tests.toSeq, None, None)

  def suite(name: String, config: Config, setUp: TestSetUp, tests: Test*): Suite = new TestGroup(name, config, tests.toSeq, Some(setUp), None)

  def suite(name: String, config: Config, tearDown: TestTearDown, tests: Test*): Suite = new TestGroup(name, config, tests.toSeq, None, Some(tearDown))

  def suite(name: String, config: Config, setUp: TestSetUp, tearDown: TestTearDown, tests: Test*): Suite = new TestGroup(name, config, tests.toSeq, Some(setUp), Some(tearDown))

  def config(config: Tuple2[String, Any]*): Config = new Config(config.toMap)

  def suites(name: String, suites: Suite*): Suite = new SuiteGroup(name, suites.toSeq)

  def setUp(calls: Call*) = new TestSetUp(calls.toSeq)

  def tearDown(calls: Call*) = new TestTearDown(calls.toSeq)

  def test(name: String, calls: Call*) = new Test(name, calls.toSeq)

  def get(uri: String) = new Call(new Request(Get(), uri))

  def put(uri: String) = new Call(new Request(Put(), uri))

  def post(uri: String) = new Call(new Request(Post(), uri))

  def delete(uri: String) = new Call(new Request(Delete(), uri))

  def head(uri: String) = new Call(new Request(Head(), uri))

  def options(uri: String) = new Call(new Request(Options(), uri))

  def quote(any: Any): String = "\"%s\"".format(any)

}


