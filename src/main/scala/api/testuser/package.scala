package api

import etc.WS
import etc.config.Config
import etc.util._

import scala.concurrent.Future

package object testuser {
  object parseClientAccessToken {
    private val Regex = """access_token=(.+?)(?=\z|&).*""".r
    def apply(s: String): Option[String] = s match {
      case Regex(access_token) => Some(access_token)
      case _ => None
    }
  }

  def getAppAccessToken(config: Config)(implicit ws: WS): Future[String] = for {
    body0 <- ws.get_body(graphURL("/oauth/access_token"), "client_id" -> config.app_id, "client_secret" -> config.app_secret, "grant_type" -> "client_credentials")
    app_access_token <- parseClientAccessToken(body0).toFuture("Failed to parse app access_token")
  } yield app_access_token
}
