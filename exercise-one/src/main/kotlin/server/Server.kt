package server

import ServerHost
import SocketUtils
import java.net.DatagramSocket
import java.net.ServerSocket
import java.util.*

fun main() {
    lateinit var tcpServer: ServerHost<ServerSocket>
    lateinit var udpServer: ServerHost<DatagramSocket>
    val input = Scanner(System.`in`)
    var message = ""
    try {
        tcpServer = TcpServer(SocketUtils.PORT_NUMBER)
        udpServer = UdpServer(SocketUtils.PORT_NUMBER)
        tcpServer.accept()
        udpServer.accept()
        while (message != SocketUtils.EXIT) {
            message = input.nextLine()
            tcpServer.broadcast(message)
        }

        tcpServer.disconnect()
    } finally {
        tcpServer.disconnect()
        udpServer.disconnect()
    }
}
