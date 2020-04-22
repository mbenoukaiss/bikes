package io.github.mbenoukaiss.bikes.models

import java.io.Serializable

enum class Status {
    OPEN,
    CLOSED
}

class Position(
    val latitude: Double,
    val longitude: Double
) : Serializable

class Stand(
    val availabilities: Availabilities,
    val capacity: Int
) : Serializable

class Availabilities(
    val bikes: Int,
    val stands: Int
) : Serializable

class Station(
    var number: Int,
    var contractName: String,
    var name: String,
    var address: String,
    var position: Position,
    var banking: Boolean,
    var bonus: Boolean,
    var overflow: Boolean,
    var connected: Boolean,
    var status: Status,
    var lastUpdate: String,
    var totalStands: Stand,
    var mainStands: Stand,
    var overflowStands: Stand?
) : Serializable