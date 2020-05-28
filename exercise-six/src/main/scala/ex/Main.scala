package ex

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http

import scala.concurrent.ExecutionContext


object Main extends App {
  implicit val system: ActorSystem = ActorSystem("system")

  implicit val ec: ExecutionContext = system.dispatcher
  val serverRef = system.actorOf(Props[Server], "server")
  val httpServer = new HttpServer(serverRef)
  val clientRefs = (1 to 5).map(id => system.actorOf(Props(new Client(serverRef)), id.toString))

  val bindingFuture = Http().bindAndHandle(httpServer.route, "localhost", 8080)

  val in = scala.io.StdIn
  while (true) {
    try {
      System.out.flush()
      val input = in.readLine.split(' ')
      clientRefs(input(0).toInt) ! input(1)
    } catch {
      case ex: Exception =>
        System.err.println(ex)
    }
  }
}