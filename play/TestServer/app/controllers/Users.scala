package controllers

import play.api._
import play.api.mvc._

object Users extends Controller {
  
  def get(id: Int) = Action {
    Ok("""{ user: { id: %s, user_id: 20902 } }""".format(id)).as(JSON)
  }
  
}
