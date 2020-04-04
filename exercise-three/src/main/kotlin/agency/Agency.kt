package agency

import Message
import ServiceTypes
import basicConsumer
import com.rabbitmq.client.*
import consume
import declareChannel
import setUpMessageExchange
import setUpServiceExchange
import java.io.BufferedReader
import java.io.InputStreamReader

fun main() {

    println("Enter agency name: ")
    val br = BufferedReader(InputStreamReader(System.`in`))
    val name = br.readLine()
    val orderIdGenerator = generateSequence(0) { it + 1 }.iterator()

    val channel = declareChannel()

    channel.setUpServiceExchange(ServiceExchangeTypes.AGENCIES, listOf(name))
    val serviceConsumer = basicConsumer(channel) {
        "Received acknowledgement from ${it.sender} service provider who took care of our ${it.message} ${it.orderId} request!"
    }
    channel.consume(serviceConsumer, listOf(name))

    val msgName = "$name-message"
    channel.setUpMessageExchange(MessageExchangeTypes.AGENCIES, msgName)
    val messageConsumer: Consumer = basicConsumer(channel) {
        it.message
    }
    channel.consume(messageConsumer, listOf(msgName))

    while (true) {

        println("Enter service type from available ${ServiceTypes.items()} or `exit` to exit.")
        val input = br.readLine()

        if(input in ServiceTypes.items()) {
            val message = Message(name, orderIdGenerator.next(), input).serialize()
            channel.basicPublish(Exchange.name, "${ServiceExchangeTypes.CARRIERS.prefix}.$input", null, message.toByteArray(charset("UTF-8")))
            println("Sent: $message")
        } else if(input == "exit") {
            break
        } else {
            println("Message `$input` is unrecognized!")
        }
    }
}
