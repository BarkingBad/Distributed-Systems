package client

import ClientHost
import java.net.Socket
import kotlin.concurrent.thread

class TcpClient(private val portNumber: Int) : ClientHost<Socket> {

    val socket: Socket = createSocket().also {
        if (it.isBound) {
            SocketUtils.info("Client has connected to the server. Received name is ${it.localPort}")
        }
    }

    override val id = socket.localPort

    override fun createSocket() = Socket(SocketUtils.HOST_NAME, portNumber)

    override fun collect() = thread {
        try {
            while (socket.isConnected) {
                SocketUtils.receive(socket).readLine().also {
                    if(it == null) disconnect()
                    SocketUtils.msg(it)
                }
            }
        } catch (e: Exception) {
            if (e.message == SocketUtils.SOCKED_IS_CLOSED || e.message == SocketUtils.SOCKED_CLOSED)
                disconnect()
        }
    }

    override fun broadcast(msg: String) = thread {
        if (socket.isConnected) {
            SocketUtils.send(socket).println(SocketUtils.msg(socket, msg))
        }
    }

    override fun disconnect() {
        if (!socket.isClosed) {
            socket.close()
            SocketUtils.info("Closed socket")
        }
    }
}