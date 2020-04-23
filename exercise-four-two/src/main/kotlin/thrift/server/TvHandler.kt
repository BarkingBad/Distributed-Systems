package thrift.server

import rpc.thrift.*

class TvHandler : TV.Iface, Device.Iface by DeviceHandler() {

    private val recordingList: MutableList<Recording> = mutableListOf()

    override fun scheduleRecording(recording: Recording) =
        Status().apply {
            status = StatusTypes.OK
        }.also {
            recordingList += recording
        }

    override fun removeRecording(recording: Recording) =
        Status().apply {
            status = StatusTypes.OK
        }.also {
            recordingList -= recording
        }

    override fun scheduledRecordings() = recordingList
}