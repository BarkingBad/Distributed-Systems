package grpc

import java.io.Closeable
import java.util.concurrent.TimeUnit

import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.stub.StreamObserver
import io.grpc.{ConnectivityState, ManagedChannel, ManagedChannelBuilder}

import scala.collection.mutable

class GrpcClient(private val channel: ManagedChannel) extends Closeable {

  private val stub = GrpcGrpc.newStub(channel).withWaitForReady()

  def close(): Unit = channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)

  class GrpcStreamObserver[T] extends StreamObserver[T] {
    override def onNext(result: T): Unit = println(result)

    override def onError(t: Throwable): Unit = println("RPC ERROR")

    override def onCompleted(): Unit = println("COMPLETED STREAM ERROR")
  }

  val callbackMeetings = new GrpcStreamObserver[MeetingResponse]

  def meetings(param: String): Unit = stub.subscribeMeetings(MeetingRequest.newBuilder().setDepartment(param).build(), callbackMeetings)


  val callbackWeather = new GrpcStreamObserver[WeatherResponse]

  def weather(param: String): Unit = stub.subscribeWeather(WeatherRequest.newBuilder().setCity(param).build(), callbackWeather)
}

object Main extends App {
  private val channel = ManagedChannelBuilder
    .forAddress("localhost", 8980)
    .usePlaintext
    .asInstanceOf[ManagedChannelBuilder[InProcessChannelBuilder]]
    .build()

  private val subscribedTopics = new mutable.MutableList[(String, String)]()
  val thread = new Thread {
    override def run(): Unit = {
      var state = 1
      while (true) {
        Thread.sleep(1000)
        while (channel.getState(true) != ConnectivityState.READY) {
          Thread.sleep(1000)
          println("reconnecting")
          state = 1
        }
        if (state == 1) {
          println("connected")
          state = 0
          subscribedTopics.foreach { case (t, p) => subscribe(t, p) }
        }
      }
    }
  }
  thread.start()

  val client = new GrpcClient(channel)

  while (true) {
    val input = scala.io.StdIn.readLine()
    val splitInput = input.split(' ')

    splitInput(0) match {
      case "SUBSCRIBE" =>
        if (splitInput.length == 3) {
          subscribe(splitInput(1), splitInput(2))
          subscribedTopics += ((splitInput(1), splitInput(2)))
        }
      case "EXIT" => exitHandler()
      case _ => defaultHandler()
    }
  }

  def subscribe(topic: String, param: String) {
    topic match {
      case "WEATHER" => client.weather(param)
      case "MEETING" => client.meetings(param)
      case _ => defaultTopic()
    }
  }

  def exitHandler(): Unit = System.exit(0)

  def defaultTopic(): Unit = println(
    """
      |Unsupported topic, use one of the following
      |MEETING
      |WEATHER""".trim
  )

  def defaultHandler(): Unit = println(
    """
      |Unknown command, use one of the following
      |SUBSCRIBE $topic $param
      |EXIT""".trim
  )
}