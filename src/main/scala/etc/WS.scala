package etc

import play.api.http.Writeable
import play.api.libs.json.{Json, JsValue}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.libs.ws.ning.NingWSClient

import scala.concurrent.Future

class WS {
  import etc.util._

  private lazy val client = NingWSClient()

  private def request[A](method: String, modify: WSRequest => WSRequest = identity)(extract: WSResponse => A)(url: String, queryString: Seq[(String, String)]): Future[A] = {
    val request = modify(client.url(url).withQueryString(queryString: _*).withMethod(method))
    console.debug(s"$method ${request.uri.toASCIIString}")

    val response = request.execute()
    response.map { r =>
      val a = extract(r)
      r.status match {
        case s if s < 400 => console.debug(
          s"""
             |$method ${request.uri.toASCIIString}
             |$s: $a
           """.stripMargin)
        case s => console.error(
          s"""
             |$method ${request.uri.toASCIIString}
             |$s: $a
           """.stripMargin)
      }
      a
    }
  }

  def get_body(url: String, queryString: (String, String)*): Future[String] = request("GET")(_.body)(url, queryString)
  def get_json(url: String, queryString: (String, String)*): Future[JsValue] = request("GET")(_.json)(url, queryString)
  def post[A : Writeable](url: String, queryString: (String, String)*)(body: A): Future[JsValue] = request("POST", _.withBody(body))(_.json)(url, queryString)
}
