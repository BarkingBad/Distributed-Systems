package ex

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill}

import scala.util.Random

class PriceGenerator extends Actor with ActorLogging {

  override def receive: Receive = {
    case name: String => {
      Thread.sleep(Random.nextInt(400) + 100)
      val price = Random.nextInt(10) + 1
      sender ! GeneratePriceResponse(name, price)
      self ! PoisonPill
    }
  }
}

case class GeneratePriceQuery(name: String, sender: ActorRef)
case class GeneratePriceResponse(name: String, price: Int)
