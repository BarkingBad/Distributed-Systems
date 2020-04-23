package thrift.server

import rpc.thrift.*
import kotlin.random.Random

class DeviceHandler : Device.Iface {

    private var upTimeGenerator = newGenerator()

    private fun newGenerator() = generateSequence(0) { it + Random.nextInt(5) + 2 }.iterator()

    private var deviceState = StateOfDevice().apply {
        state = StateTypes.UP
        upTime = 0
        errorMsg = ""
    }

    override fun getState() = deviceState.apply {
        if(state == StateTypes.UP)
            upTime = upTimeGenerator.next()
    }

    override fun turnOff() = deviceState.apply {
        upTime = 0
        state = StateTypes.DOWN
    }.let { Status().apply { status = StatusTypes.OK } }.also {
        upTimeGenerator = newGenerator()
    }

    override fun turnOn() = deviceState.apply {
        upTime = 0
        state = StateTypes.UP
    }.let { Status().apply { status = StatusTypes.OK } }
}