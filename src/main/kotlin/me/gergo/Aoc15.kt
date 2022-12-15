package me.gergo

import java.io.File
import kotlin.math.abs

fun main() {
    val sensors = File("src/main/resources/input15.txt").readLines()
        .map(::parseSensors)

    val maxRange = sensors.maxOf(Sensor::range)
    val minX = sensors.minOf { it.closestBeacon.x } - maxRange
    val maxX = sensors.maxOf { it.closestBeacon.x } + maxRange
    val beacons = sensors.map(Sensor::closestBeacon).toSet()

    // Part One
    val atY = 2000000
    val result1 = (minX..maxX).count { x ->
        sensors.any { it.inRange(Coordinate(x, atY)) } // Number of coordinates covered by the sensor in our row,
    } - beacons.count { it.y == atY } // ...minus the number of known beacons in that row

    println("Positions that cannot contain a beacon: $result1")

    // Part Two
    val max = 4000000
    val tuningFrequency =
        sensors.flatMap(Sensor::outerBorder) // The search space can be greatly limited by considering the outer border of the sensor range
            .filter { it.x in 0..max && it.y in 0..max } // ...except for all the coordinates that fall outside the 0..4000000 range
            .distinct() // ...removing duplicates
            .filter { c -> sensors.none { s -> s.inRange(c) } } // ...finally selecting the undetected coordinate
            .map { p -> p.x * 4000000L + p.y }
    println("Distress beacon tuning frequency: $tuningFrequency")
}

private data class Sensor(val position: Coordinate, val closestBeacon: Coordinate) {
    val range = manhattanDistance(position, closestBeacon.x, closestBeacon.y)
    fun inRange(c: Coordinate) = manhattanDistance(position, c.x, c.y) <= range

    fun outerBorder() = sequence<Coordinate> {
        val radius = range

        for (i in 0..radius) { // Left to top
            yield(Coordinate(position.x - radius - 1 + i, position.y - i))
        }
        for (i in 0..radius) { // Top to right
            yield(Coordinate(position.x + i, position.y - radius - 1 + i))
        }
        for (i in 0..radius) { // Right to bottom
            yield(Coordinate(position.x + radius + 1 - i, position.y + i))
        }
        for (i in 0..radius) { // Bottom to left
            yield(Coordinate(position.x - i, position.y + radius + 1 - i))
        }
    }
}

private fun manhattanDistance(a: Coordinate, x: Int, y: Int) = abs(a.x - x) + abs(a.y - y)

private val SensorBeaconFormat = Regex("Sensor at x=(-?\\d+), y=(-?\\d+): closest beacon is at x=(-?\\d+), y=(-?\\d+)")
private fun parseSensors(line: String): Sensor {
    val (_, sx, sy, bx, by) = SensorBeaconFormat.matchEntire(line)!!.groupValues
    return Sensor(Coordinate(sx.toInt(), sy.toInt()), Coordinate(bx.toInt(), by.toInt()))
}
