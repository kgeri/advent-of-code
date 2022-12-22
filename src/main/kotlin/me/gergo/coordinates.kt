package me.gergo

enum class Direction {
    L, U, R, D;

    fun clockWise() = when (this) {
        L -> U
        U -> R
        R -> D
        D -> L
    }

    fun counterClockWise() = when (this) {
        L -> D
        U -> L
        R -> U
        D -> R
    }
}

data class Coordinate(val x: Int, val y: Int)

fun neighbors(width: Int, height: Int, c: Coordinate) = sequence {
    val x = c.x
    val y = c.y
    if (x > 0) yield(Coordinate(x - 1, y))
    if (y + 1 < height) yield(Coordinate(x, y + 1))
    if (x + 1 < width) yield(Coordinate(x + 1, y))
    if (y > 0) yield(Coordinate(x, y - 1))
}

