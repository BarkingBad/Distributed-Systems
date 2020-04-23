package thrift.client

import org.apache.thrift.TException
import org.apache.thrift.transport.TSocket
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.protocol.TMultiplexedProtocol
import rpc.thrift.{Fridge, Recording, TV, Thermometer}

object ThriftClient extends App {
  val opt = "multiplex"
  val host = "localhost"

  try {

    val transport = new TSocket(host, 9080)
    val protocol = new TBinaryProtocol(transport, true, true)
    val thermometers = (1 to 3).map(x => new Thermometer.Client(new TMultiplexedProtocol(protocol, s"Thermometer-$x")))
    val tvs = (1 to 2).map(x => new TV.Client(new TMultiplexedProtocol(protocol, s"TV-$x")))
    val fridge = new Fridge.Client(new TMultiplexedProtocol(protocol, "Fridge-1"))

    transport.open()

    val in = scala.io.StdIn
    while (true) {
      try {
        System.out.print("==> ")
        System.out.flush()
        val input = in.readLine.split(' ')
        println(input(0) match {
          case "THERMOMETER" => thermometerHandler(thermometers, input)
          case "TV" => tvHandler(tvs, input)
          case "FRIDGE" => fridgeHandler(fridge, input)
          case "EXIT" => exitHandler(transport)
          case _ => defaultHandler()
        })
      } catch {
        case ex: Exception =>
          System.err.println(ex)
      }
    }
  } catch {
    case ex: TException =>
      ex.printStackTrace()
  }

  def tvHandler(tv: IndexedSeq[TV.Client], input: Array[String]): String = {
    if (input.length >= 3 && ((1 to tv.size) contains input(1).toInt)) {
      input(2) match {
        case "scheduleRecording" => tv(input(1).toInt - 1).scheduleRecording(new Recording().setChannel(input(3).toInt).setFromDate(input(4)).setToDate(input(5))).toString
        case "removeRecording" => tv(input(1).toInt - 1).removeRecording(new Recording().setChannel(input(3).toInt).setFromDate(input(4)).setToDate(input(5))).toString
        case "scheduledRecordings" => tv(input(1).toInt - 1).scheduledRecordings().toString
        case "getState" => tv(input(1).toInt - 1).getState.toString
        case "turnOn" => tv(input(1).toInt - 1).turnOn.toString
        case "turnOff" => tv(input(1).toInt - 1).turnOff.toString
        case _ => "Malformed input!"
      }
    } else {
      s"Index out of bounds. Enter value from 1 to ${tv.size}"
    }
  }

  def fridgeHandler(fridge: Fridge.Client, input: Array[String]): String = {
    input(1) match {
      case "currentFreezingTemperature" => fridge.currentFreezingTemperature().toString
      case "changeFreezingTemperature" => fridge.changeFreezingTemperature(input(2).toInt).toString
      case "getState" => fridge.getState.toString
      case "turnOn" => fridge.turnOn.toString
      case "turnOff" => fridge.turnOff.toString
      case _ => "Malformed input!"
    }
  }

  def thermometerHandler(thermometer: IndexedSeq[Thermometer.Client], input: Array[String]): String = {
    if (input.length >= 3 && ((1 to thermometer.size) contains input(1).toInt)) {
      input(2) match {
        case "getTemperature" => BigDecimal(thermometer(input(1).toInt - 1).getTemperature).setScale(2, BigDecimal.RoundingMode.HALF_UP).toString
        case "getState" => thermometer(input(1).toInt - 1).getState.toString
        case "turnOn" => thermometer(input(1).toInt - 1).turnOn.toString
        case "turnOff" => thermometer(input(1).toInt - 1).turnOff.toString
        case _ => "Malformed input!"
      }
    } else {
      s"Index out of bounds. Enter value from 1 to ${thermometer.size}"
    }
  }

  def exitHandler(transport: TSocket) {
    transport.close()
    System.exit(0)
  }

  def defaultHandler(): String =
    """
Unknown command, use one of the following
THERMOMETER $id getTemperature
TV $id scheduleRecording $channel $ISO_DATE $ISO_DATE
TV $id removeRecording $channel $ISO_DATE $ISO_DATE
TV $id scheduledRecordings
FRIDGE currentFreezingTemperature
FRIDGE changeFreezingTemperature $int
$device $id getState
$device $id turnOn
$device $id turnOff
EXIT"""

}

