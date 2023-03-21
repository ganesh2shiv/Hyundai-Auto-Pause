package com.custom.app.data

enum class DeviceModel(val model: String, val uuid: String) {

    i20("i20", "a99abcc6-5bc1-11e7-907b-a6006ad3dba0"),

    i10("Grand i10", "a99abcc6-5bc1-11e7-907b-a6006ad3dba0"),

    Venue("Venue", ""),

    Sonet("Kia", "be90b6d9-aeae-4d8d-b92b-615250f08ab9");

    companion object {
        fun from(name: String): DeviceModel {
            for (type in values()) {
                if (type.name == name) {
                    return type
                }
            }
            return i20
        }
    }
}