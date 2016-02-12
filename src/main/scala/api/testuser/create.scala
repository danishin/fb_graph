package api.testuser

import etc.WS
import etc.config.{TestUser, Config}
import play.api.libs.json.Json

import scala.concurrent.Future

private case class CreateTestUser(access_token: String, email: String, id: String, login_url: String, password: String)
private object CreateTestUser {
  implicit val reads = Json.reads[CreateTestUser]
}

private case class UploadPhoto(id: String, post_id: String)
private object UploadPhoto {
  implicit val reads = Json.reads[UploadPhoto]
}

case class CreatedTestUser(access_token: String, email: String, password: String, previewCoverPhotoURL: String)

object create {
  import etc.util._

  private def uploadPhoto(url: String, message: String)(access_token: String)(implicit ws: WS): Future[UploadPhoto] = for {
    json1 <- ws.post(graphURL("/me/photos"), "access_token" -> access_token)(Json.obj("url" -> url, "message" -> message))
    up <- json1.validate[UploadPhoto].toFuture
  } yield up

  def apply(config: Config)(user: TestUser)(implicit ws: WS) = for {
    // Get app's access_token
    app_access_token <- getAppAccessToken(config)

    // Create Test User
    json0 <- ws.get_json(graphURL(s"/${config.app_id}/accounts/test-users"), "access_token" -> app_access_token, "installed" -> "true", "name" -> user.name, "locale" -> user.locale, "permissions" -> "email,publish_actions", "method" -> "post")
    CreateTestUser(tmp_access_token, email, id, login_url, password) <- json0.validate[CreateTestUser].toFuture

    // Extend Access Token to 60 days.
    body1 <- ws.get_body(graphURL(s"/oauth/access_token"), "client_id" -> config.app_id, "client_secret" -> config.app_secret, "grant_type" -> "fb_exchange_token", "fb_exchange_token" -> tmp_access_token)
    access_token <- parseClientAccessToken(body1).toFuture("Failed to parse test user's access_token")

    // NB: There is no api for setting profile picture or cover photo unfortunately. http://stackoverflow.com/questions/7286847/can-i-add-profile-data-to-a-facebook-test-user
    // Upload Profile Picture
    UploadPhoto(profile_picture_id, profile_picture_post_id) <- uploadPhoto(user.profilePictureURL, "profile picture")(access_token)

    // Upload Cover Photo
    UploadPhoto(cover_photo_id, cover_photo_post_id) <- uploadPhoto(user.coverPhotoURL, "cover photo")(access_token)
  } yield CreatedTestUser(access_token, email, password, s"https://www.facebook.com/profile.php?preview_cover=$cover_photo_id")
}
