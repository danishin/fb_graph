import etc.config.{TestUser, Config}
import play.api.libs.json._
import scala.io.Source
import scala.util.{Failure, Success}

object Application extends App {
  import scala.concurrent.duration._
  import etc.util._

  implicit lazy val ws = new etc.WS
  implicit lazy val timeout = 10.seconds

  lazy val (config, test_users) = {
    val json = Json.parse(Source.fromFile("config.json").mkString)
    (json.validate[Config].get, json.validate[List[TestUser]]((__ \ "test_users").read[List[TestUser]]).get)
  }

  args match {
    case Array("test-user", "create") =>
      console.info(s"Create ${test_users.length} Test User")

      test_users.traverse(api.testuser.create(config)).run match {
        case Success(l) =>
          l.zipWithIndex.foreach { case (u, i) =>
            console.info(
              s"""
                 |Test User $i
                 |Access Token: ${u.access_token}
                 |Email: ${u.email}
                 |Password: ${u.password}
                 |CoverPhotoPreviewURL: ${u.previewCoverPhotoURL}
              """.stripMargin)
          }

          sys.exit()
        case Failure(e) =>
          console.error(s"Creating test user failed with: $e")
          sys.exit(1)
      }

    case Array("test-user", "deleteAll") =>
      console.info("Delete All Test Users")

      api.testuser.deleteAll(config).run match {
        case Success(l) =>
          console.info(s"Attempted to delete ${l.length} test users with ${l.count(_ == false)} failed")
          sys.exit()

        case Failure(e) =>
          console.error(s"Deleting all test users failed with: $e")
          sys.exit(1)
      }

    case a =>
      console.error(s"Invalid Argument: ${a.mkString(" ")}")
      sys.exit(1)
  }
}
