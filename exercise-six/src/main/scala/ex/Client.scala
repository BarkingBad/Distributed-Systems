package ex

import akka.actor.{Actor, ActorRef}

class Client(val server: ActorRef) extends Actor {

  override def receive: Receive = {
    case productName: String => server ! PriceQuery(productName)
    case PriceResponse(name, price, quantity) => println(s"Server claims that $price is the best for $name. It was queried $quantity times so far.")
    case PriceWithoutQuantityResponse(name, price) => println(s"Server claims that $price is the best for $name. Database also timed out.")
    case TimeoutResponse(name, quantity) => println(s"Unfortunately request has been timed out for $name. It was queried $quantity times so far.")
    case TimeoutWithoutQuantityResponse(name) => println(s"Unfortunately request has been timed out for $name. Database also timed out.")
  }
}

case class PriceQuery(name: String)