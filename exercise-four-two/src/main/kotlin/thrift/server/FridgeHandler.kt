package thrift.server

import rpc.thrift.*

class FridgeHandler : Fridge.Iface, Device.Iface by DeviceHandler() {

    private var temperature: Int = 6

    override fun currentFreezingTemperature() = temperature

    override fun changeFreezingTemperature(temperature: Int) = when(temperature) {
        in 4..12 -> {
            this.temperature = temperature
            Status().apply { status = StatusTypes.OK }
        }
        else -> {
            Status().apply {
                status = StatusTypes.ERROR
                errorMsg = "Given temperature is out uf bounds! We do not's support such freezing."
            }
        }
    }
}