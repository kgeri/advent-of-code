package me.gergo

import java.io.File

fun main() {
    val blizzardLines = File("src/main/resources/input24.txt").readLines()
    val width = blizzardLines[0].length - 2
    val height = blizzardLines.size - 2
    val blizzards = blizzardLines.mapIndexed(::parseBlizzards).flatten()

    // Pre-calculating all possible blizzard states, exploiting the fact that the move in modulo, so their lowest common multiple of the rows/cols
    // will generate all the possible states
    val blizzardStateCount = lcm(width, height)
    val blizzardStates = Array(blizzardStateCount) { t ->
        val map = Array(height) { BooleanArray(width) { false } }
        for (b in blizzards) {
            var x = b.x
            var y = b.y
            when (b.direction) {
                Direction.L -> x -= t
                Direction.U -> y -= t
                Direction.R -> x += t
                Direction.D -> y += t
            }
            map[Math.floorMod(y, height)][Math.floorMod(x, width)] = true
        }
        map
    }
    println("Pre-generated ${blizzardStates.size} states")

    var states = setOf(State24(0, 0, 1))
    for (t in 1..Int.MAX_VALUE) {
        println("Minute $t, number of states: ${states.size}")

        val newStates = HashSet<State24>(states.size)
        val bsNext = blizzardStates[(t + 1) % blizzardStateCount]
        for (s in states) {
            // Moving in any direction is an option if there will be no blizzard there in the next round
            if (s.x > 0 && bsNext.free(s.x - 1, s.y)) newStates.add(State24(s.x - 1, s.y, t + 1))
            if (s.y > 0 && bsNext.free(s.x, s.y - 1)) newStates.add(State24(s.x, s.y - 1, t + 1))
            if (s.x < width - 1 && bsNext.free(s.x + 1, s.y)) newStates.add(State24(s.x + 1, s.y, t + 1))
            if (s.y < height - 1 && bsNext.free(s.x, s.y + 1)) newStates.add(State24(s.x, s.y + 1, t + 1))
            // Staying in place is only an option if there will be no blizzard in the next round
            if (bsNext.free(s.x, s.y)) newStates.add(State24(s.x, s.y, t + 1))
        }

        if (newStates.any { s -> s.x == width - 1 && s.y == height - 1 }) {
            println("Goal reached at: ${t + 2}") // We've started from (0,0) and ended up in (width-1,height-1)
            break
        }
        states = newStates
    }
}

private fun Array<BooleanArray>.free(x: Int, y: Int) = !this[y][x]

private data class State24(val x: Int, val y: Int, val time: Int)

private data class Blizzard(val x: Int, val y: Int, val direction: Direction)

private fun parseBlizzards(y: Int, line: String) = line.mapIndexedNotNull { x, c ->
    when (c) {
        '^' -> Blizzard(x - 1, y - 1, Direction.U)
        'v' -> Blizzard(x - 1, y - 1, Direction.D)
        '<' -> Blizzard(x - 1, y - 1, Direction.L)
        '>' -> Blizzard(x - 1, y - 1, Direction.R)
        else -> null
    }
}