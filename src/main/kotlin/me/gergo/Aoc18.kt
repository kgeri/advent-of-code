package me.gergo

import java.io.File
import java.util.*

fun main() {
    val points = File("src/main/resources/input18.txt").readLines()
        .map(::parseCoordinate)
        .toSet()

    // Part One: of all the 6 neighbors of this coordinate, how many are not in coordinates
    fun surfaceArea(points: Set<Coordinate3D>): Int {
        return points.sumOf { c ->
            c.neighbors().count { !points.contains(it) }
        }
    }
    println("Surface area: ${surfaceArea(points)}")

    // Part Two: how many surfaces are on the inside?
    // Dumb solution: fill the outside with "water" in 20x20x20 and count the surface area using that
    // Note: this naive fill algo works clumsily unless the bounds are well around the object... so adding +2 to the max
    val bounds = points.maxOf { c -> maxOf(c.x, c.y, c.z) } + 2
    fun fill3D(start: Coordinate3D, points: Set<Coordinate3D>): Set<Coordinate3D> {
        val result = mutableSetOf<Coordinate3D>()
        val boundary = Stack<Coordinate3D>()
        boundary.push(start)

        while (boundary.isNotEmpty()) {
            val c = boundary.pop()
            result.add(c)
            for (n in c.neighbors()) {
                if (result.contains(n)) continue
                else if (n.x <= -bounds || n.x >= bounds || n.y <= -bounds || n.y >= bounds || n.z <= -bounds || n.z >= bounds) continue
                else if (points.contains(n)) continue
                else boundary.push(n)
            }
        }

        return result
    }

    val water = fill3D(Coordinate3D(0, 0, 0), points)
    val width = water.maxOf { it.x } - water.minOf { it.x } + 1
    println("Water volume: ${water.size} in ${width}x${width}x${width}")
    println("Surface area excluding air bubbles: ${surfaceArea(water) - (width * width * 6)}")
}

private data class Coordinate3D(val x: Int, val y: Int, val z: Int) {
    fun neighbors() = sequence {
        yield(Coordinate3D(x - 1, y, z))
        yield(Coordinate3D(x + 1, y, z))
        yield(Coordinate3D(x, y - 1, z))
        yield(Coordinate3D(x, y + 1, z))
        yield(Coordinate3D(x, y, z - 1))
        yield(Coordinate3D(x, y, z + 1))
    }
}

private fun parseCoordinate(line: String): Coordinate3D {
    val (x, y, z) = line.split(",")
    return Coordinate3D(x.toInt(), y.toInt(), z.toInt())
}
