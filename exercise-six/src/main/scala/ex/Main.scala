package ex

import akka.actor.{ActorRef, ActorSystem, Props}

object Main extends App {
  val system: ActorSystem = ActorSystem("system")

  val serverRef = system.actorOf(Props[Server], "server")
  val clientRefs = (1 to 5).map(id => system.actorOf(Props(new Client(serverRef)), id.toString))

  clientRefs(0) ! "dupa"


}