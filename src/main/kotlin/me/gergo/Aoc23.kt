package me.gergo

import java.io.File

fun main() {
    val elves = File("src/main/resources/input23.txt")
        .readLines()
        .mapIndexed { y, line ->
            line.mapIndexedNotNull { x, c -> if (c == '#') Coordinate(x, y) else null }
        }.flatten()
        .toHashSet()
    val simulateUntil = Int.MAX_VALUE

    val directions = listOf(::canMoveNorth, ::canMoveSouth, ::canMoveWest, ::canMoveEast)
    val moves = listOf(Coordinate::north, Coordinate::south, Coordinate::west, Coordinate::east)

    val proposals = mutableMapOf<Coordinate, MutableList<Coordinate>>()
    fun propose(target: Coordinate, elf: Coordinate) = proposals.computeIfAbsent(target) { mutableListOf() }.add(elf)

    var standing: Int
    for (i in 0 until simulateUntil) {

        // Proposal round
        proposals.clear()
        standing = 0
        for (elf in elves) {
            val canMove = directions.map { it(elf, elves) }
            if (canMove.all { it }) standing++ // Could move anywhere, so we don't
            else {
                for (d in canMove.indices) { // Trying to move in priority order
                    if (canMove[(i + d) % directions.size]) {
                        val move = moves[(i + d) % moves.size]
                        propose(move(elf), elf)
                        break
                    }
                }
            }
        }

        if (standing == elves.size) {
            println("Elves stopped moving at round ${i + 1}")
            break
        }

        // Move round
        proposals
            .filter { it.value.size == 1 }
            .forEach { (target, proposers) ->
                elves.remove(proposers[0])
                elves.add(target)
            }
    }

    // Calculating the rectangle
    val minX = elves.minOf { it.x }
    val minY = elves.minOf { it.y }
    val maxX = elves.maxOf { it.x }
    val maxY = elves.maxOf { it.y }
    val result1 = (maxX - minX + 1) * (maxY - minY + 1) - elves.size
    println("Empty ground tiles after round 10: $result1")
}

private fun Coordinate.north() = Coordinate(x, y - 1)
private fun Coordinate.south() = Coordinate(x, y + 1)
private fun Coordinate.west() = Coordinate(x - 1, y)
private fun Coordinate.east() = Coordinate(x + 1, y)

private fun canMoveNorth(c: Coordinate, elves: Set<Coordinate>) = !elves.contains(Coordinate(c.x - 1, c.y - 1))
        && !elves.contains(Coordinate(c.x, c.y - 1))
        && !elves.contains(Coordinate(c.x + 1, c.y - 1))

private fun canMoveSouth(c: Coordinate, elves: Set<Coordinate>) = !elves.contains(Coordinate(c.x - 1, c.y + 1))
        && !elves.contains(Coordinate(c.x, c.y + 1))
        && !elves.contains(Coordinate(c.x + 1, c.y + 1))

private fun canMoveEast(c: Coordinate, elves: Set<Coordinate>) = !elves.contains(Coordinate(c.x + 1, c.y - 1))
        && !elves.contains(Coordinate(c.x + 1, c.y))
        && !elves.contains(Coordinate(c.x + 1, c.y + 1))

private fun canMoveWest(c: Coordinate, elves: Set<Coordinate>) = !elves.contains(Coordinate(c.x - 1, c.y - 1))
        && !elves.contains(Coordinate(c.x - 1, c.y))
        && !elves.contains(Coordinate(c.x - 1, c.y + 1))
