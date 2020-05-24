package ex

import akka.actor.{Actor, ActorLogging, ActorRef}

class Client(val server: ActorRef) extends Actor with ActorLogging {

  override def receive: Receive = {
    case productName: String => server ! PriceQuery(productName)
    case PriceResponse(name, price, quantity) => println(s"Server claims that $price is the best for $name. It was queried $quantity times so far.")
    case TimeoutResponse(name, quantity) => println(s"Unfortunately request has been timed out for $name. It was queried $quantity times so far.")
  }
}

case class PriceQuery(name: String)