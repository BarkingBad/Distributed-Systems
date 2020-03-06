package client

import ClientHost
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.util.*
import kotlin.concurrent.thread

class UdpMulticast(portNumber: Int, override val id: Int) : ClientHost<MulticastSocket> {
    private val portNumber: Int = portNumber
    private val address: InetAddress = InetAddress.getByName("239.0.0.1")
    private val buffer: ByteArray = ByteArray(1024)
    private val socket: MulticastSocket = createSocket()

    override fun createSocket() = MulticastSocket(portNumber).apply {
        joinGroup(address)
    }

    override fun collect() = thread {
        while (true) {
            Arrays.fill(buffer, 0.toByte())
            val packet = DatagramPacket(buffer, buffer.size)
            socket.receive(packet)
            val respond = String(packet.data)
            respond.trim {it <= ' '}.split(" ID ", limit = 2).takeIf { it[0].toInt() != id }.run {
                if(this != null) {
                    SocketUtils.msg(this[1].trim {it <= ' '})
                }
            }
        }
    }

    override fun broadcast(msg: String) = thread {
        DatagramPacket(msg.toByteArray(), msg.toByteArray().size, address, portNumber).also {
            socket.send(it)
        }
    }

    override fun disconnect() {
        if (!socket.isClosed) {
            socket.leaveGroup(address)
            socket.close()
        }
    }
}