import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

object SocketUtils {
    fun msg(msg: String) = if (msg != EXIT) println(msg) else Unit
    fun msg(sender: Socket, msg: String) = "[${sender.localPort}] $msg"
    fun info(msg: String) = println(msg)
    fun send(socket: Socket) = PrintWriter(socket.getOutputStream(), true)
    fun receive(socket: Socket) = BufferedReader(InputStreamReader(socket.getInputStream()))

    const val HOST_NAME = "localhost"
    const val PORT_NUMBER = 6969
    const val SOCKED_CLOSED = "Socket closed"
    const val SOCKED_IS_CLOSED = "Socket is closed"
    const val EXIT = "end"
    const val HELLO = "HELLO1234567890"
    const val RESOURCE = """

                                   .''.       
       .''.      .        *''*    :_\/_:     . 
      :_\/_:   _\(/_  .:.*_\/_*   : /\ :  .'.:.'.
  .''.: /\ :   ./)\   ':'* /\ * :  '..'.  -=:o:=-
 :_\/_:'.:::.    ' *''*    * '.\'/.' _\(/_'.':'.'
 : /\ : :::::     *_\/_*     -= o =-  /)\    '  *
  '..'  ':::'     * /\ *     .'/.\'.   '
      *            *..*         :
       *
        *
    """
}