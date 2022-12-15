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

data class Coordinate(val x: Int, val y: Int)

fun aStar(
    start: Coordinate, goal: Coordinate,
    neighborFn: (Coordinate) -> Sequence<Coordinate>,
    heuristicFn: (Coordinate) -> Double,
    distanceFn: (Coordinate, Coordinate) -> Double
): List<Coordinate> {
    val openSet = mutableSetOf(start)
    val cameFrom = mutableMapOf<Coordinate, Coordinate>()

    val gScore = mutableMapOf<Coordinate, Double>()
    gScore[start] = 0.0

    val fScore = mutableMapOf<Coordinate, Double>()
    fScore[start] = heuristicFn(start)

    while (openSet.isNotEmpty()) {
        val current = openSet.minBy { fScore.getOrDefault(it, POSITIVE_INFINITY) }
        if (current == goal) {
            // Path found, reconstructing
            val result = mutableListOf(current)
            var c: Coordinate? = current
            while (true) {
                c = cameFrom[c]
                if (c == null) break
                result.add(0, c)
            }
            return result
        }

        openSet.remove(current)
        for (neighbor in neighborFn(current)) {
            val tentativeGScore = gScore.getOrDefault(current, POSITIVE_INFINITY) + distanceFn(current, neighbor)
            if (tentativeGScore < gScore.getOrDefault(neighbor, POSITIVE_INFINITY)) {
                cameFrom[neighbor] = current
                gScore[neighbor] = tentativeGScore
                fScore[neighbor] = tentativeGScore + heuristicFn(neighbor)
                openSet.add(neighbor)
            }
        }
    }
    return emptyList() // Path not found
}

fun neighbors(width: Int, height: Int, c: Coordinate) = sequence {
    val x = c.x
    val y = c.y
    if (x > 0) yield(Coordinate(x - 1, y))
    if (y + 1 < height) yield(Coordinate(x, y + 1))
    if (x + 1 < width) yield(Coordinate(x + 1, y))
    if (y > 0) yield(Coordinate(x, y - 1))
}

private fun manhattanDistance(a: Coordinate, b: Coordinate): Double = (abs(b.x - a.x) + abs(b.y - a.y)).toDouble()
