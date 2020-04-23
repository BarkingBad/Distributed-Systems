package grpc

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.broadcastIn
import kotlinx.coroutines.flow.flow
import java.util.*

class MeetingGenerator {
    private val map: MutableMap<String, BroadcastChannel<MeetingResponse>> = mutableMapOf()

    private val random = Random()

    private val members = listOf(
            User.newBuilder().setName("John").setSurname("Smith").build(),
            User.newBuilder().setName("Albert").setSurname("Makbet").build(),
            User.newBuilder().setName("Marek").setSurname("Goryl").build(),
            User.newBuilder().setName("Kacper").setSurname("Karbon").build(),
            User.newBuilder().setName("Wojtek").setSurname("Dozima").build()
    )

    operator fun get(key: String): BroadcastChannel<MeetingResponse> =
            map.getOrPut(key) {
                flow<MeetingResponse> {
                    while (true) {
                        delay((3000 + random.nextInt(3000)).toLong())
                        emit(MeetingResponse
                                .newBuilder()
                                .setName(key)
                                .setFloor(random.nextInt(5))
                                .setRoom(random.nextInt(100))
                                .setType(MeetingResponse.Type.forNumber(random.nextInt(3)))
                                .addAllUsers(members.shuffled().take(2 + random.nextInt(4)))
                                .build()
                        )
                    }
                }.broadcastIn(GlobalScope)
            }
}

class WeatherGenerator {
    private val map: MutableMap<String, BroadcastChannel<WeatherResponse>> = mutableMapOf()

    private val random = Random()

    operator fun get(key: String): BroadcastChannel<WeatherResponse> =
            map.getOrPut(key) {
                flow<WeatherResponse> {
                    while (true) {
                        delay((3000 + random.nextInt(3000)).toLong())
                        emit(WeatherResponse
                                .newBuilder()
                                .setCity(key)
                                .setTemperature((random.nextInt(40) - 10).toFloat())
                                .setHumidityPercentage(random.nextInt(100))
                                .setWeather(WeatherResponse.Weather.forNumber(random.nextInt(4)))
                                .build()
                        )
                    }
                }.broadcastIn(GlobalScope)
            }
}
