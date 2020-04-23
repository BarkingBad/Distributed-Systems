package grpc

import io.grpc.ServerBuilder
import kotlinx.coroutines.flow.consumeAsFlow

class GrpcServer(val port: Int) {

    private val server = ServerBuilder.forPort(port).addService(GrpcService()).build()

    fun start() {
        server.start()
        println("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("*** shutting down gRPC server since JVM is shutting down")
                this@GrpcServer.stop()
                println("*** server shut down")
            }
        )
    }

    fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }

    class GrpcService : GrpcGrpcKt.GrpcCoroutineImplBase() {

        private val meetingGenerators = MeetingGenerator()
        override fun subscribeMeetings(request: MeetingRequest) = meetingGenerators[request.department].openSubscription().consumeAsFlow()

        private val weatherGenerators = WeatherGenerator()
        override fun subscribeWeather(request: WeatherRequest) = weatherGenerators[request.city].openSubscription().consumeAsFlow()
    }
}

fun main() {
    val port = 8980
    val server = GrpcServer(port)
    server.start()
    server.blockUntilShutdown()
}