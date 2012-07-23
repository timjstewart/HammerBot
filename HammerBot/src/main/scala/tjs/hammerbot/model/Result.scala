package tjs.hammerbot.model

sealed abstract class Result()

case class Failure(val message: String) extends Result()

case class Success() extends Result()

