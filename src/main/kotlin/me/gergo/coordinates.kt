package me.gergo

import kotlin.math.abs

data class Coordinate(val x: Int, val y: Int)

fun neighbors(width: Int, height: Int, c: Coordinate) = sequence {
    val x = c.x
    val y = c.y
    if (x > 0) yield(Coordinate(x - 1, y))
    if (y + 1 < height) yield(Coordinate(x, y + 1))
    if (x + 1 < width) yield(Coordinate(x + 1, y))
    if (y > 0) yield(Coordinate(x, y - 1))
}

