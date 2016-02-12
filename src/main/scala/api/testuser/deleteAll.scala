package api.testuser

import etc.WS
import etc.config.{TestUser, Config}
import play.api.libs.json.Json

import scala.concurrent.Future

case class GetTestUsers(access_token: String, id: String, login_url: String)
object GetTestUsers { implicit val reads = Json.reads[GetTestUsers] }

case class DeleteTestUser(success: Boolean)
object DeleteTestUser { implicit val reads = Json.reads[DeleteTestUser] }

object deleteAll {
  import etc.util._

  def apply(config: Config)(implicit ws: WS): Future[List[Boolean]] = for {
    // Get app access token
    app_access_token <- getAppAccessToken(config)

    // Get all test users in this app
    json <- ws.get_json(graphURL(s"/${config.app_id}/accounts/test-users"), "access_token" -> app_access_token)
    ids <- (json \ "data").validate[List[GetTestUsers]].map(_.map(_.id)).toFuture

    // Delete all test users
    jsons <- ids.traverse(id => ws.get_json(graphURL(s"/$id"), "method" -> "delete", "access_token" -> app_access_token))
    results <- jsons.map(_.validate[DeleteTestUser].toFuture).sequence.map(_.map(_.success))
  } yield results
}
