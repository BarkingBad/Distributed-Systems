package server

import ServerHost
import java.lang.Exception
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread


class TcpServer(private val portNumber: Int) : ServerHost<ServerSocket> {
    private val socket: ServerSocket = createSocket().also {
        if (it.isBound) {
            SocketUtils.info("Created socket")
        }
    }
    private val clients: ConcurrentHashMap<Socket, Thread?> = ConcurrentHashMap()

    override fun createSocket() = ServerSocket(portNumber)

    private fun clientThread(socket: Socket) = thread(start = true) {
        try {
            while (socket.isConnected) {
                val response: String? = SocketUtils.receive(socket).readLine()
                if (response == null || response == SocketUtils.EXIT) {
                    clients.remove(socket)
                    socket.close()
                    SocketUtils.info("Closed connection with: " + socket.port)
                    break
                } else {
                    SocketUtils.msg(response)
                    val broadcasting = broadcast(response, socket)
                    broadcasting.join()
                }
            }
        } catch (e: Exception) {
            if (e.message != SocketUtils.SOCKED_CLOSED) {
                e.printStackTrace()
            }
        }
    }

    override fun accept() = thread {
        try {
            while (true) {
                val client = socket.accept()
                if (!clients.containsKey(client)) {
                    clients[client] = clientThread(client)
                    SocketUtils.info("Connected client: " +
                            client.remoteSocketAddress +
                            " with id: " + client.port)
                }
            }
        } catch (e: Exception) {
            if (e.message != SocketUtils.SOCKED_CLOSED) {
                e.printStackTrace()
            }
        } finally {
            clients.values.forEach {
                try {
                    it!!.join()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }.also { SocketUtils.info("Waiting for connections ...") }

    override fun disconnect() {
        if (!socket.isClosed) {
            clients.keys.forEach {
                if (!it.isClosed) {
                    it.close()
                    SocketUtils.info("Closed connection with: " + it.port)
                }
            }
            socket.close()
            SocketUtils.info("Closed server socket")
        }
    }

    override fun broadcast(msg: String) = thread {
        clients.keys.forEach {
            if (it.isConnected) {
                SocketUtils.send(it).println(msg)
            }
        }
    }

    private fun broadcast(msg: String, sender: Socket) = thread {
        clients.keys.forEach {
            if (it.isConnected && it !== sender) {
                SocketUtils.send(it).println(msg)
            }
        }
    }
}