package ex

import java.net.URL

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives.{complete, concat, get, pathPrefix}
import org.jsoup.Jsoup
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

import scala.concurrent.{ExecutionContext, Future}

class HttpServer(server: ActorRef) {

  implicit val timeout: Timeout = 3000.milliseconds

  def route(implicit ec: ExecutionContext, system: ActorSystem): Route = {
    concat(
      pathPrefix("price" / Remaining) { name =>
        get {
          complete((server ? PriceQuery(name)).fallbackTo(Future {
            TimeoutWithoutQuantityResponse(name)
          }).mapTo[ServerResponse].map(_.toString)
          )
        }
      },
      pathPrefix("review" / Remaining) { product =>
        get {
          complete(
            try {
              Jsoup
              .parse(new URL(s"https://www.opineo.pl/?szukaj=$product&s=2"), 10000)
              .body()
              .getElementById("page")
              .getElementById("content")
              .getElementById("screen")
              .getElementsByClass("pls")
              .get(0)
              .getElementsByClass("shl_i pl_i")
              .get(0)
              .getElementsByClass("pl_attr")
              .get(0)
              .getElementsByTag("li")
              .eachText()
              .toArray
              .map(_.toString)
              .mkString("\n")
            } catch {
              case _ => "Nie znaleziono produktu"
            }
          )
        }
      }
    )
  }
}
