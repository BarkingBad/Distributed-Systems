package administrator

import Message
import basicConsumer
import com.rabbitmq.client.*
import consume
import declareChannel
import setUpServiceExchange
import java.io.BufferedReader
import java.io.InputStreamReader

fun main() {

    val br = BufferedReader(InputStreamReader(System.`in`))
    val greeting = """
        Declare whom you'd like to message:
        1 -> Agencies
        2 -> Carriers
        3 -> Both
    """.trimIndent()
    println(greeting)
    val topicType = when(br.readLine()) {
        "1" -> MessageExchangeTypes.AGENCIES
        "2" -> MessageExchangeTypes.CARRIERS
        "3" -> MessageExchangeTypes.BOTH
        else -> throw IllegalStateException("Malformed input provided!")
    }.prefix

    val channel = declareChannel()

    channel.setUpServiceExchange(ServiceExchangeTypes.BOTH, listOf("*"))
    val consumer: Consumer = basicConsumer(channel) {
        "Intercepted message $it"
    }
    channel.consume(consumer, listOf("*"))

    while (true) {

        println("Send next message to all $topicType or `exit` to exit.")
        val input = br.readLine()

        if(input == "exit") {
            break
        } else {
            val message = Message("administrator", 0, input).serialize()
            channel.basicPublish(Exchange.name, topicType, null, message.toByteArray(charset("UTF-8")))
            println("Sent: $message")
        }
    }
}
