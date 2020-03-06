package server

import ServerHost
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.concurrent.thread

class UdpServer(private val portNumber: Int) : ServerHost<DatagramSocket> {
    private val socket: DatagramSocket = createSocket()
    private val buffer: ByteArray = ByteArray(1024)
    private val clients: ConcurrentLinkedQueue<DatagramPacket> = ConcurrentLinkedQueue()

    override fun createSocket() = DatagramSocket(portNumber).apply {
        reuseAddress = true
    }

    override fun accept() = thread {
        while (true) {
            Arrays.fill(buffer, 0.toByte())
            val packet = DatagramPacket(buffer, buffer.size)
            socket.receive(packet)
            val respond = String(packet.data).trim { it <= ' ' }
            clients += packet
            if(respond != SocketUtils.HELLO) {
                SocketUtils.msg(respond)
                broadcast(respond, packet.port).join()
            }
        }
    }

    private fun broadcast(msg: String, sender: Int) = thread {
        val sendBuffer = msg.toByteArray()
        clients.forEach {
            DatagramPacket(sendBuffer, sendBuffer.size, it.address, it.port).run {
                if (it.port != sender) {
                    socket.send(this)
                }
            }
        }
    }

    override fun broadcast(msg: String) = thread {
        clients.forEach {
            DatagramPacket(msg.toByteArray(), msg.toByteArray().size, it.address, it.port).run {
                socket.send(this)
            }
        }
    }

    override fun disconnect() {
        if (!socket.isClosed) {
            socket.close()
        }
    }
}