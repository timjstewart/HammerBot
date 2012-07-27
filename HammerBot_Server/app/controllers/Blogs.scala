package controllers

import play.api._
import play.api.mvc._

object Blogs extends Controller {
  
  def put(id: Int) = Action {
    Ok("""{ blog: { id: %s, user_id: 20902 } }""".format(id)).as(JSON)
  }
  
  def get(id: Int) = Action {
    Ok("""{ blog: { id: %s, user_id: 20902 } }""".format(id)).as(JSON)
  }
  
  def post = Action {
    Status(201)
  }
  
  def getAll = Action {
    Ok("""{ blogs: { count: 120 } }""").as(JSON)
  }
  
  def delete(id: Int) = Action {
    Ok("""{ blogs: { count: 120 } }""").as(JSON)
  }
  
  def delay() = Action {
    Thread.sleep(500)
    Ok("{ }").as(JSON)
  }

  def setUp = Action {
    Status(201)
  }
  
  def tearDown = Action {
    Status(201)
  }
  
}
