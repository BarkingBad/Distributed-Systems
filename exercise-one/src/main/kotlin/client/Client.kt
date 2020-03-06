package client

import ClientHost
import java.net.DatagramSocket
import java.net.MulticastSocket
import java.net.Socket
import java.util.*

fun main() {

    lateinit var tcpClient: ClientHost<Socket>
    lateinit var udpClient: ClientHost<DatagramSocket>
    lateinit var multicast: ClientHost<MulticastSocket>
    var message = ""
    val input = Scanner(System.`in`)
    try {

        tcpClient = TcpClient(SocketUtils.PORT_NUMBER)
        udpClient = UdpClient(SocketUtils.PORT_NUMBER, tcpClient.id)
        multicast = UdpMulticast(SocketUtils.PORT_NUMBER+1, tcpClient.id)
        tcpClient.collect()
        udpClient.collect()
        multicast.collect()
        udpClient.broadcast(SocketUtils.HELLO)
        while (message != SocketUtils.EXIT) {
            message = input.nextLine()
            when (message) {
                "U" -> udpClient.broadcast("${tcpClient.id} ID [${tcpClient.id}]\n${SocketUtils.RESOURCE}")
                "M" -> multicast.broadcast("${tcpClient.id} ID [${tcpClient.id}]\n ${SocketUtils.RESOURCE}")
                else -> tcpClient.broadcast(message)
            }
        }
        tcpClient.disconnect()
    } finally {
        tcpClient.disconnect()
    }
}
