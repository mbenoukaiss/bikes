package io.github.mbenoukaiss.bikes.models

enum class Status {
    OPEN,
    CLOSED
}

class Position(
    val latitude: Float,
    val longitude: Float
)

class Stand(
    val availabilities: Availabilities,
    val capacity: Int
)

class Availabilities(
    val bikes: Int,
    val stands: Int
)

class Station(
    val number: Int,
    val contractName: String,
    val name: String,
    val address: String,
    val position: Position,
    val banking: Boolean,
    val bonus: Boolean,
    val overflow: Boolean,
    val connected: Boolean,
    val status: Status,
    val lastUpdate: String,
    val totalStands: Stand,
    val mainStands: Stand,
    val overflowStands: Stand
)