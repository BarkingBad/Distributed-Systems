import java.net.Socket

interface Host<I> {
    fun createSocket(): I
    fun broadcast(msg: String): Thread
    fun disconnect(): Unit
}

interface ClientHost<I> : Host<I> {
    val id: Int
    fun collect(): Thread
}

interface ServerHost<I> : Host<I> {
    fun accept(): Thread
}