namespace java rpc.thrift

enum StateTypes {
    UP,
    DOWN,
    ERROR
}

struct StateOfDevice {
    1: StateTypes state,
    2: i32 upTime
    3: optional string errorMsg
}

enum StatusTypes {
    OK,
    ERROR
}

struct Status {
    1: StatusTypes status,
    2: optional string errorMsg
}

service Device {
   StateOfDevice getState()
   Status turnOn()
   Status turnOff()
}

service Fridge extends Device {
    Status changeFreezingTemperature(1: i32 temperature),
    i32 currentFreezingTemperature()
}

typedef string Timestamp

struct Recording {
    1: i32 channel,
    2: Timestamp fromDate,
    3: Timestamp toDate
}

service TV extends Device {
    Status scheduleRecording(1: Recording recording)
    Status removeRecording(1: Recording recording)
    list<Recording> scheduledRecordings()
}

service Thermometer extends Device {
    double getTemperature()
}