import com.google.gson.Gson
import com.rabbitmq.client.*

object Exchange {
    val name = "serviceExchange"
    val type = BuiltinExchangeType.TOPIC
}

interface Prefixable {
    val prefix: String
}

enum class ServiceExchangeTypes(override val prefix: String) : Prefixable {
    AGENCIES("agencies"),
    CARRIERS("carriers"),
    BOTH("*")
}

enum class MessageExchangeTypes(override val prefix: String) : Prefixable {
    AGENCIES("magencies.*"),
    CARRIERS("*.mcarriers"),
    BOTH("magencies.mcarriers")
}

enum class ServiceTypes {
    PEOPLE,
    CARGO,
    SATELLITE;

    companion object {
        fun items(): List<String> = enumValues<ServiceTypes>().map { it.name }
    }
}

data class Message(val sender: String, val orderId: Int, val message: String) {
    fun serialize() = Gson().toJson(this, Message::class.java)
}
fun String.deserialize() = Gson().fromJson(this, Message::class.java)

fun declareChannel(): Channel = ConnectionFactory().apply {
    host = "localhost"
}.newConnection().createChannel().apply {
    basicQos(1)
}

fun basicConsumer(channel: Channel, block: (Message) -> String) = object : DefaultConsumer(channel) {
    override fun handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: ByteArray) {
        val message = body.toString(charset("UTF-8")).deserialize()
        println(block.invoke(message))
        channel.basicAck(envelope.deliveryTag, false)
    }
}

fun Channel.setUpServiceExchange(keyPrefix: Prefixable, keys: List<String>) = this.apply {
    exchangeDeclare(Exchange.name, Exchange.type)
    keys.forEach {
        queueDeclare(it, false, false, false, null)
        queueBind(it, Exchange.name, "${keyPrefix.prefix}.${it}")
    }
}

fun Channel.setUpMessageExchange(keyRoute: Prefixable, name: String) = this.apply {
    queueDeclare(name, false, false, false, null)
    queueBind(name, Exchange.name, keyRoute.prefix)
}

fun Channel.consume(consumer: Consumer, keys: List<String>) = this.apply {
    keys.forEach{
        basicConsume(it, false, consumer)
    }
}