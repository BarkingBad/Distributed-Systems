package ex

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import scala.util.Success

class Server extends Actor with ActorLogging {

  override def receive: Receive = {
    case PriceQuery(name) => context.actorOf(Props[ServerDelegate]) ! GeneratePriceQuery(name, sender)
  }
}

sealed class ServerResponse

case class PriceResponse(name: String, price: Int, quantity: Int) extends ServerResponse

case class TimeoutResponse(name: String, quantity: Int) extends ServerResponse

class ServerDelegate extends Actor with ActorLogging {

  implicit val timeout: Timeout = 300.millisecond
  implicit val ec: ExecutionContextExecutor = context.dispatcher

  override def receive: Receive = {
    case GeneratePriceQuery(name, sender) => {
      val s = sender
      val queries = (1 to 2).map(_ => (context.actorOf(Props[PriceGenerator]) ? name).mapTo[GeneratePriceResponse])
      queries(0)
        .zipWith(queries(1))((x, y) => if (x.price < y.price) x else y)
        .fallbackTo(queries(0))
        .fallbackTo(queries(1))
        .onComplete {
          case Success(GeneratePriceResponse(name, price)) => s ! PriceResponse(name, price, 0)
          case _ => s ! TimeoutResponse(name, 0)
        }
      self ! PoisonPill
    }
  }
}