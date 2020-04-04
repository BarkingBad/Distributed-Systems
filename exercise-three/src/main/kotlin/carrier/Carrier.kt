package carrier

import Message
import ServiceTypes
import basicConsumer
import consume
import declareChannel
import setUpMessageExchange
import setUpServiceExchange
import java.io.BufferedReader
import java.io.InputStreamReader

fun main() {

    println("Enter provider name: ")
    val br = BufferedReader(InputStreamReader(System.`in`))
    val name = br.readLine()

    val greeting = """
        Declare provided services:
        1 -> PEOPLE and CARGO
        2 -> PEOPLE and SATELLITE
        3 -> CARGO and SATELLITE
    """.trimIndent()
    println(greeting)
    val services = when(br.readLine()) {
        "1" -> listOf(ServiceTypes.PEOPLE, ServiceTypes.CARGO)
        "2" -> listOf(ServiceTypes.PEOPLE, ServiceTypes.SATELLITE)
        "3" -> listOf(ServiceTypes.CARGO, ServiceTypes.SATELLITE)
        else -> throw IllegalStateException("Malformed input provided!")
    }.map { it.name }

    val channel = declareChannel()

    channel.setUpServiceExchange(ServiceExchangeTypes.CARRIERS, services)
    val serviceConsumer = basicConsumer(channel) {
        val response = Message(name, it.orderId, it.message)
        channel.basicPublish(Exchange.name, "${ServiceExchangeTypes.AGENCIES.prefix}.${it.sender}", null, response.serialize().toByteArray())
        "Received message from ${it.sender} agency tagged with their internal ID ${it.orderId}. They request ${it.message} service!"
    }
    channel.consume(serviceConsumer, services)

    val msgName = "$name-message"
    channel.setUpMessageExchange(MessageExchangeTypes.CARRIERS, msgName)
    val messageConsumer = basicConsumer(channel) {
        it.message
    }
    channel.consume(messageConsumer, listOf(msgName))

    println("Waiting for messages...")
}
