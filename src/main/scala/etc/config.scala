package etc

import play.api.libs.json.Json

object config {
  case class Config(app_id: String, app_secret: String)
  object Config { implicit val reads = Json.reads[Config] }

  case class TestUser(name: String, locale: String, profilePictureURL: String, coverPhotoURL: String)
  object TestUser { implicit val reads = Json.reads[TestUser] }
}
