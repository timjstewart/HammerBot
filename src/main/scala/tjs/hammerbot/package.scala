package tjs

import tjs.hammerbot.model._

package object hammerbot {

  def suite(name: String, tests: Test*): Tree = new Leaf(name, Config.empty, tests.toSeq)

  def suite(name: String, config: Config, tests: Test*): Tree = new Leaf(name, config, tests.toSeq)

  def config(config: Tuple2[String, Any]*): Config = new Config(config.toMap)

  def suites(name: String, suites: Tree*): Tree = new Branch(name, suites.toSeq)

  def test(name: String, calls: Call*) = new Test(name, calls.toSeq)

  def get(uri: String) = new Call(new Request(Get(), uri))

  def put(uri: String) = new Call(new Request(Put(), uri))

  def post(uri: String) = new Call(new Request(Post(), uri))

  def delete(uri: String) = new Call(new Request(Delete(), uri))

  def head(uri: String) = new Call(new Request(Head(), uri))

  def options(uri: String) = new Call(new Request(Options(), uri))

  def quote(any: Any): String = "\"%s\"".format(any)

}


