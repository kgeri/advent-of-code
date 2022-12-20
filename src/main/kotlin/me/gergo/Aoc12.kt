package me.gergo

import java.io.File
import kotlin.Double.Companion.POSITIVE_INFINITY
import kotlin.math.abs

fun main() {
    val terrain = File("src/main/resources/input12.txt").readLines()
        .map { it.toCharArray() }.toTypedArray()

    val start = terrain.findAll('S').first()
    val goal = terrain.findAll('E').first()

    terrain[start] = 'a'
    terrain[goal] = 'z'

    fun distance(a: Coordinate, b: Coordinate): Double {
        if (manhattanDistance(a, b) > 1) return POSITIVE_INFINITY // Can only move 1 step up, down, left or right
        val valueA = terrain[a].code
        val valueB = terrain[b].code
        if (valueB - valueA > 1) return POSITIVE_INFINITY // Can only move at most one step up
        return 1.0
    }

    // Part One
    val result1 = aStar(
        start, goal,
        terrain::neighbors,
        { c -> manhattanDistance(c, goal) },
        ::distance
    ).size - 1
    println("Fewest steps required from S: $result1")

    // Part Two

    val result2 = terrain.findAll('a')
        .map {
            val path = aStar(it, goal, terrain::neighbors, { c -> manhattanDistance(c, goal) }, ::distance)
            if (path.isEmpty()) Int.MAX_VALUE else path.size - 1
        }
        .min()
    println("Fewest steps required from any 'a': $result2")
}

private fun Array<CharArray>.findAll(value: Char) = sequence<Coordinate> {
    val matrix = this@findAll
    for (y in matrix.indices) {
        for (x in matrix[y].indices) {
            if (matrix[y][x] == value) yield(Coordinate(x, y))
        }
    }
}

private fun Array<CharArray>.neighbors(c: Coordinate) = neighbors(this[0].size, this.size, c)

private operator fun Array<CharArray>.get(c: Coordinate) = this[c.y][c.x]
private operator fun Array<CharArray>.set(c: Coordinate, value: Char) {
    this[c.y][c.x] = value
}

private fun manhattanDistance(a: Coordinate, b: Coordinate): Double = (abs(b.x - a.x) + abs(b.y - a.y)).toDouble()
