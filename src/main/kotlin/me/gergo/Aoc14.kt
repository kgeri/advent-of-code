package me.gergo

import java.io.File
import kotlin.math.max
import kotlin.math.min

fun main() {
    val rockPaths = File("src/main/resources/input14.txt").readLines()
        .map {
            it.split(" -> ").map { c ->
                val (x, y) = c.split(",")
                Coordinate(x.toInt(), y.toInt())
            }
        }
    val cave = Cave(rockPaths, 2000) // Set floorWidth=0 for Part One... yeah, it's not very elegant, but it worked :)

    while (cave.pourSandAt(Coordinate(500, 0))) {
    }

    println(cave)
    println("Sand at rest: ${cave.sandCount()}")
}

private class Cave(rockPaths: List<List<Coordinate>>, val floorWidth: Int) {
    private val minX: Int
    private val minY: Int
    private val width: Int
    private val height: Int
    private val cells: Array<CharArray>

    init {
        val allCoordinates = rockPaths.flatten()
        minX = min(-floorWidth, allCoordinates.minOf { it.x })
        minY = min(0, allCoordinates.minOf { it.y })
        width = max(allCoordinates.maxOf { it.x } - minX, floorWidth * 2) + 1
        height = allCoordinates.maxOf { it.y } - minY + 1 + 2
        cells = Array(height) { CharArray(width) { '.' } }

        for (rockPath in rockPaths) {
            rockPath.windowed(2) { (a, b) ->
                if (a.x == b.x) {
                    for (y in min(a.y, b.y)..max(a.y, b.y)) this[a.x, y] = '#'
                } else if (a.y == b.y) {
                    for (x in min(a.x, b.x)..max(a.x, b.x)) this[x, a.y] = '#'
                }
            }
        }

        if (floorWidth > 0) {
            for (x in -floorWidth..floorWidth) this[x, height - minY - 1] = '#'
        }
    }

    operator fun get(x: Int, y: Int): Char {
        return cells[y - minY][x - minX]
    }

    operator fun set(x: Int, y: Int, value: Char) {
        cells[y - minY][x - minX] = value
    }

    override fun toString(): String {
        val buf = StringBuilder(height * (width + 1))
        for (y in 0 until height) {
            for (x in 0 until width) {
                buf.append(cells[y][x])
            }
            buf.append('\n')
        }
        return buf.toString()
    }

    fun pourSandAt(start: Coordinate): Boolean {
        if (!isFree(start.x, start.y)) return false // We're full at the starting coordinate

        var x = start.x
        for (y in 0..minY + height) {
            if (isOutside(x, y)) break
            if (isFree(x, y + 1)) continue
            else if (isFree(x - 1, y + 1)) {
                x--
            } else if (isFree(x + 1, y + 1)) {
                x++
            } else {
                this[x, y] = 'o'
                return true // Settled
            }
        }
        return false
    }

    fun sandCount() = cells.sumOf { lines -> lines.count { it == 'o' } }

    private fun isFree(x: Int, y: Int) = isOutside(x, y) || (this[x, y] != '#' && this[x, y] != 'o')
    private fun isOutside(x: Int, y: Int) = x < minX || x >= width + minX || y < minY || y >= height + minY
}