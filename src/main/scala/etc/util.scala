package etc

import java.nio.charset.StandardCharsets
import java.nio.file.{Paths, Files}

import play.api.libs.json.{JsError, JsSuccess, JsResult}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.FiniteDuration
import scala.util.Try

object util {
  implicit class OptionExt[A](option: Option[A]) {
    def toFuture(errorMessage: String): Future[A] = option match {
      case Some(a) => Future.successful(a)
      case None => Future.failed(new RuntimeException(errorMessage))
    }
  }

  implicit class JsResultExt[A](jsResult: JsResult[A]) {
    def toFuture: Future[A] = jsResult match {
      case JsSuccess(a, _) => Future.successful(a)
      case JsError(e) => Future.failed(new RuntimeException(e.toString))
    }
  }

  implicit class ListExt[A](list: List[A]) {
    def traverse[B](f: A => Future[B]) = Future.traverse(list)(f)
    def sequence[B](implicit ev: A <:< Future[B]): Future[List[B]] = Future.sequence(list.map(ev))
  }

  implicit class FutureExt[A](future: Future[A]) {
    def run(implicit timeout: FiniteDuration): Try[A] = Try(Await.result(future, timeout))
  }

  implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global

  object console {
    def info(m: String) = println(Console.BLUE + m + Console.RESET + "\n")
    def debug(m: String) = println(Console.CYAN + m + Console.RESET + "\n")
    def error(m: String) = println(Console.RED + m + Console.RESET + "\n")
  }

  def writeToFile(path: String, contents: String): Future[Unit] = Future(Files.write(Paths.get(path), contents.getBytes(StandardCharsets.UTF_8)))

  def graphURL(path: String) = "https://graph.facebook.com" + path
}