package ex

import akka.actor.{ActorRef, ActorSystem, Props}


object Main extends App {
  val system: ActorSystem = ActorSystem("system")

  val serverRef = system.actorOf(Props[Server], "server")
  val clientRefs = (1 to 5).map(id => system.actorOf(Props(new Client(serverRef)), id.toString))

  val in = scala.io.StdIn
  while (true) {
    try {
      System.out.print("==> ")
      System.out.flush()
      val input = in.readLine.split(' ')
      clientRefs(input(0).toInt) ! input(1)
    } catch {
      case ex: Exception =>
        System.err.println(ex)
    }
  }
}