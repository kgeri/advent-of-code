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
    val valley = Valley24(width, height, blizzardStates)

    // Part One
    // We're ending up in (width-1,height-1) instead of , hence the +1
    val result1 = valley.simulate(0, -1, 0, width - 1, height - 1) + 1
    println("Goal reached at: $result1")

    // Part Two
    // Continuing the simulation
    val backToStart = valley.simulate(width - 1, height, result1, 0, 0) + 1
    println("Back to start reached at: $backToStart")
    val result2 = valley.simulate(0, -1, backToStart, width - 1, height - 1) + 1
    println("Goal reached again at: $result2")
}

private class Valley24(private val width: Int, private val height: Int, private val blizzardStates: Array<Array<BooleanArray>>) {

    fun simulate(startX: Int, startY: Int, startTime: Int, goalX: Int, goalY: Int): Int {
        fun State24.moveTo(x: Int, y: Int) = State24(x, y, (time + 1) % blizzardStates.size)
        fun State24.stay() = State24(x, y, (time + 1) % blizzardStates.size)

        var states = setOf(State24(startX, startY, startTime))
        for (t in startTime..Int.MAX_VALUE) {
            println("Minute $t, number of states: ${states.size}")

            val newStates = HashSet<State24>(states.size * 2)
            val bsNext = blizzardStates[(t + 1) % blizzardStates.size]
            for (s in states) {
                if (s.x == 0 && s.y == -1) { // Valley entry
                    if (bsNext.free(0, 0)) newStates.add(s.moveTo(0, 0))
                    newStates.add(s.stay()) // Staying in the start position is always an option
                    continue
                } else if (s.x == width - 1 && s.y == height) { // Valley exit
                    if (bsNext.free(width - 1, height - 1)) newStates.add(s.moveTo(width - 1, height - 1))
                    newStates.add(s.stay()) // Staying in the start position is always an option
                    continue
                }

                // Moving in any direction is an option if there will be no blizzard there in the next round
                if (s.x > 0 && bsNext.free(s.x - 1, s.y)) newStates.add(s.moveTo(s.x - 1, s.y))
                if (s.y > 0 && bsNext.free(s.x, s.y - 1)) newStates.add(s.moveTo(s.x, s.y - 1))
                if (s.x < width - 1 && bsNext.free(s.x + 1, s.y)) newStates.add(s.moveTo(s.x + 1, s.y))
                if (s.y < height - 1 && bsNext.free(s.x, s.y + 1)) newStates.add(s.moveTo(s.x, s.y + 1))
                // Staying in place is only an option if there will be no blizzard in the next round
                if (bsNext.free(s.x, s.y)) newStates.add(s.stay())
            }

            if (newStates.isEmpty()) throw IllegalStateException("Can't move anywhere at $t!")
            if (newStates.any { s -> s.x == goalX && s.y == goalY }) {
                return t + 1
            }
            states = newStates
        }
        throw IllegalStateException("Solution not found :(")
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