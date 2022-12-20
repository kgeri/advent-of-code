package me.gergo

import java.io.File
import java.lang.Integer.max
import kotlin.math.abs
import kotlin.math.sign

fun main() {
    val steps = File("src/main/resources/input09.txt").readLines()
        .map(::parseRopeStep)

    // val ropeField = RopeField(6, 5, 2) // For Part One
    val ropeField = RopeField(6, 5, 10) // For Part Two
    for (step in steps) {
        println(step)
        ropeField.moveBy(step)
    }
    println("Positions visited by tail: ${ropeField.positionsVisitedByTail().count()}")
}

private enum class Direction { L, R, U, D }
private data class RopeStep(val dir: Direction, val steps: Int)
private data class Position(val x: Int, val y: Int) {
    fun distanceFrom(o: Position) = max(abs(x - o.x), abs(y - o.y))
}

private class Rope(internal val knots: MutableList<Position>) {

    fun head() = knots[0]
    fun tail() = knots[knots.size - 1]

    internal fun moveHead(dir: Direction) {
        val head = head()
        knots[0] = when (dir) {
            Direction.L -> Position(head.x - 1, head.y)
            Direction.R -> Position(head.x + 1, head.y)
            Direction.U -> Position(head.x, head.y + 1)
            Direction.D -> Position(head.x, head.y - 1)
        }
        for (i in 1 until knots.size) {
            val prev = knots[i - 1]
            var curr = knots[i]
            while (prev.distanceFrom(curr) > 1) {
                val fx = (prev.x - curr.x).sign
                val fy = (prev.y - curr.y).sign
                curr = if (fx == 0) {
                    Position(curr.x, curr.y + fy)
                } else if (fy == 0) {
                    Position(curr.x + fx, curr.y)
                } else {
                    Position(curr.x + fx, curr.y + fy)
                }
            }
            knots[i] = curr
        }
    }
}

private class RopeField(private val width: Int, private val height: Int, ropeLength: Int) {
    private val rope = Rope(MutableList(ropeLength) { Position(0, 0) })
    private val tailPositions = mutableListOf<Position>()

    fun moveBy(step: RopeStep) {
        for (s in 1..step.steps) {
            rope.moveHead(step.dir)
            tailPositions.add(rope.tail())
            // println(this) // For simple debugging, does not work for negative coordinates!
        }
    }

    override fun toString(): String {
        val buf = Array(height) { CharArray(width) { '.' } }
        for (i in 1 until rope.knots.size) {
            val knot = rope.knots[i]
            buf[height - 1 - knot.y][knot.x] = i.digitToChar()
        }
        buf[height - 1 - rope.head().y][rope.head().x] = 'H'
        return buf.joinToString("\n") { it.joinToString("") } + '\n'
    }

    fun positionsVisitedByTail() = tailPositions.toSet()
}

private fun parseRopeStep(line: String): RopeStep {
    val (dir, steps) = line.split(" ")
    return RopeStep(Direction.valueOf(dir), steps.toInt())
}
