package thrift.server

import rpc.thrift.Fridge
import rpc.thrift.TV
import rpc.thrift.Thermometer
import org.apache.thrift.TMultiplexedProcessor
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.protocol.TProtocolFactory
import org.apache.thrift.server.TServer.Args
import org.apache.thrift.server.TSimpleServer
import org.apache.thrift.transport.TServerSocket
import org.apache.thrift.transport.TServerTransport

fun main() {
    try {
        val serverTransport: TServerTransport = TServerSocket(9080)
        val protocolFactory: TProtocolFactory = TBinaryProtocol.Factory()
        val multiplex = TMultiplexedProcessor()
        (1..3).forEach {
            multiplex.registerProcessor("Thermometer-$it", Thermometer.Processor(ThermometerHandler()))
        }
        (1..2).forEach {
            multiplex.registerProcessor("TV-$it", TV.Processor(TvHandler()))
        }
        multiplex.registerProcessor("Fridge-1", Fridge.Processor(FridgeHandler()))
        TSimpleServer(Args(serverTransport).protocolFactory(protocolFactory).processor(multiplex)).serve()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}