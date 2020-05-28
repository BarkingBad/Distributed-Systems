package ex

import akka.actor.{Actor, PoisonPill, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import scala.util.Success

class Server extends Actor {

  override def receive: Receive = {
    case PriceQuery(name) => context.actorOf(Props[ServerDelegate]) ! GeneratePriceQuery(name, sender)
  }
}

sealed class ServerResponse
case class PriceResponse(name: String, price: Int, quantity: Int) extends ServerResponse
case class PriceWithoutQuantityResponse(name: String, price: Int) extends ServerResponse
case class TimeoutResponse(name: String, quantity: Int) extends ServerResponse
case class TimeoutWithoutQuantityResponse(name: String) extends ServerResponse

class ServerDelegate extends Actor {

  implicit val timeout: Timeout = 300.millisecond
  implicit val ec: ExecutionContextExecutor = context.dispatcher

  override def receive: Receive = {
    case GeneratePriceQuery(name, sender) => {
      val s = sender
      val queries = (1 to 2).map(_ => (context.actorOf(Props[PriceGenerator]) ? name).mapTo[GeneratePriceResponse])
      val quantity = (context.actorOf(Props[DbDelegate]) ? DbRequest(name)).mapTo[DbResponse]

      val price = queries(0)
        .zipWith(queries(1))((x, y) => if (x.price < y.price) x else y)
        .fallbackTo(queries(0))
        .fallbackTo(queries(1))

      price.zipWith(quantity)((query, quantity) => PriceResponse(query.name, query.price, quantity.quantity))
        .fallbackTo(price)
        .fallbackTo(quantity)
        .onComplete {
          case Success(priceResponse: PriceResponse) => s ! priceResponse
          case Success(GeneratePriceResponse(name, price)) => s ! PriceWithoutQuantityResponse(name, price)
          case Success(DbResponse(name, quantity)) => s ! TimeoutResponse(name, quantity)
          case _ => s ! TimeoutWithoutQuantityResponse(name)
        }
      self ! PoisonPill
    }
  }
}
