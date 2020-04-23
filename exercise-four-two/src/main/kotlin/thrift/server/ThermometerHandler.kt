package thrift.server

import rpc.thrift.Device
import rpc.thrift.Thermometer
import kotlin.random.Random

class ThermometerHandler : Thermometer.Iface, Device.Iface by DeviceHandler() {

    private val temperature = generateSequence(20.0) { Random.nextDouble(20.0) + 10.0 }.iterator()
    override fun getTemperature() = temperature.next()
}