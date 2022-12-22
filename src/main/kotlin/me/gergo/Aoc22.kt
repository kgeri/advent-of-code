package me.gergo

import java.io.File

// Now this was mental.
// I quickly gave up on solving this nicely for Part Two (ie. generally for any kind of input), and rather just added another section to the input, 
// that consists of from-to ranges, mapping the edges of the cube with seams, eg. this means:
//   0,100-49,100;50,50-50,99;U;R
//   "map the 0,100-49,100 edge to the 50,50-50,99 edge tile by tile, in this order, and whenever we'd exit the first edge Up, change that to Right"
// This way, I ended up with a 50x50x50 cube where Tile22s on the edges are connected by Seam22s, possibly flipping the direction of the traversal.
// From this point, most of the time I spent on debugging the edge declarations and directions, and finding an annoying bug with hitting a wall 
// immediately after traversing a seam.

fun main() {
    val (board, instructions) = parseMapAndInstructions(File("src/main/resources/input22-part2.txt").readText())

    fun resolve(tile: RealTile22, neighbor: Tile22, dir: Direction): Pair<RealTile22, Direction> = when (neighbor) {
        is RealTile22 -> Pair(neighbor, dir)
        is Seam22 -> resolve(tile, neighbor.to, neighbor.newDir)
        Wall22 -> Pair(tile, dir)
        VoidTile22 -> throw IllegalArgumentException("Hit the Void!")
    }

    var current = board.topLeft()
    var currentDir = Direction.R
    val debugPath = mutableListOf<Pair<Coordinate, Direction>>()
    for (ins in instructions) {
        when (ins) {
            Turn22.L -> currentDir = currentDir.counterClockWise()
            Turn22.R -> currentDir = currentDir.clockWise()
            is Step22 -> {
                for (i in 1..ins.amount) {
                    val neighbor = when (currentDir) {
                        Direction.L -> current.left
                        Direction.U -> current.up
                        Direction.R -> current.right
                        Direction.D -> current.down
                    }
                    val (next, nextDir) = resolve(current, neighbor, currentDir)
                    if (next == current) break // Can't move, we hit a wall
                    current = next
                    currentDir = nextDir
                    debugPath.add(Pair(Coordinate(current.x, current.y), currentDir))
                }
            }
        }
    }

    println(board.debug(debugPath))

    val row = current.y + 1
    val col = current.x + 1
    val facingValue = when (currentDir) {
        Direction.L -> 2
        Direction.U -> 3
        Direction.R -> 0
        Direction.D -> 1
    }
    val password = 1000 * row + 4 * col + facingValue
    println("Password: $password")
}

private class Board22(private val tiles: List<List<Tile22>>) {
    fun topLeft() = tiles[0].filterIsInstance<RealTile22>().first()

    fun debug(path: MutableList<Pair<Coordinate, Direction>>): String {
        val debug = Array(tiles.size) { CharArray(tiles[0].size) { ' ' } }
        for (y in tiles.indices) {
            for (x in tiles[y].indices) {
                val t = tiles[y][x]
                if (t is RealTile22) debug[y][x] = '.'
                else if (t is Wall22) debug[y][x] = '#'
            }
        }
        for ((c, dir) in path) {
            debug[c.y][c.x] = when (dir) {
                Direction.L -> '<'
                Direction.U -> '^'
                Direction.R -> '>'
                Direction.D -> 'v'
            }
        }
        return debug.joinToString("\n") { it.joinToString("") }
    }

    operator fun get(x: Int, y: Int) = tiles[y][x] as RealTile22
}

private sealed interface Tile22
private object VoidTile22 : Tile22
private class RealTile22(val x: Int, val y: Int, var right: Tile22, var up: Tile22, var left: Tile22, var down: Tile22) : Tile22
private object Wall22 : Tile22
private class Seam22(val to: Tile22, val newDir: Direction) : Tile22

private sealed interface Instruction22
private enum class Turn22 : Instruction22 { L, R }
private data class Step22(val amount: Int) : Instruction22

private fun parseMapAndInstructions(text: String): Pair<Board22, List<Instruction22>> {
    val (boardText, instructionText, seamsText) = text.split("\n\n")

    val tiles = boardText.split('\n').mapIndexed { y, line ->
        line.mapIndexed { x, t ->
            when (t) {
                '.' -> RealTile22(x, y, VoidTile22, VoidTile22, VoidTile22, VoidTile22)
                '#' -> Wall22
                else -> VoidTile22
            }
        }
    }

    val debug = Array(tiles.size) { CharArray(tiles[0].size) { ' ' } }

    for (y in tiles.indices) {
        for (x in tiles[y].indices) {
            val tile = tiles[y][x]

            if (tile is RealTile22) debug[y][x] = '.'
            else if (tile is Wall22) debug[y][x] = '#'

            if (tile !is RealTile22) continue
            if (x > 0) tile.left = tiles[y][x - 1]
            if (y > 0) tile.up = tiles[y - 1][x]
            if (x < tiles[y].size - 1) tile.right = tiles[y][x + 1]
            if (y < tiles.size - 1) tile.down = tiles[y + 1][x]
        }
    }

    val instructions = Regex("(L|R|\\d+)+?").findAll(instructionText)
        .map {
            when (it.value) {
                "L" -> Turn22.L
                "R" -> Turn22.R
                else -> Step22(it.value.toInt())
            }
        }.toList()

    for ((si, seam) in seamsText.split("\n").withIndex()) {
        val (fromRange, toRange, fromDir, toDir) = seam.split(";")
        val from = parseCoordinateRange(fromRange)
        val to = parseCoordinateRange(toRange)
        val fd = Direction.valueOf(fromDir)
        val td = Direction.valueOf(toDir)
        for (i in from.indices) {
            val f = from[i]
            val t = to[i]
            val fromTile = tiles[f.y][f.x]
            val toTile = tiles[t.y][t.x]

            if (fromTile is RealTile22) {
                when (fd) {
                    Direction.L -> fromTile.left = Seam22(toTile, td)
                    Direction.U -> fromTile.up = Seam22(toTile, td)
                    Direction.R -> fromTile.right = Seam22(toTile, td)
                    Direction.D -> fromTile.down = Seam22(toTile, td)
                }
            }
            if (toTile is RealTile22) {
                when (td.opposite()) {
                    Direction.L -> toTile.left = Seam22(fromTile, fd.opposite())
                    Direction.U -> toTile.up = Seam22(fromTile, fd.opposite())
                    Direction.R -> toTile.right = Seam22(fromTile, fd.opposite())
                    Direction.D -> toTile.down = Seam22(fromTile, fd.opposite())
                }
            }

            debug[f.y][f.x] = si.digitToChar()
            debug[t.y][t.x] = si.digitToChar()
        }
    }

    // Debug output
    println(debug.joinToString("\n") { it.joinToString("") })
    for (t in tiles.flatten().filterIsInstance<RealTile22>()) {
        if (t.up is VoidTile22) println("Error: (${t.x},${t.y}).up is void!")
        if (t.right is VoidTile22) println("Error: (${t.x},${t.y}).right is void!")
        if (t.down is VoidTile22) println("Error: (${t.x},${t.y}).down is void!")
        if (t.left is VoidTile22) println("Error: (${t.x},${t.y}).left is void!")
    }

    return Pair(Board22(tiles), instructions)
}

private fun parseCoordinateRange(rangeText: String): List<Coordinate> {
    val (minX, minY, maxX, maxY) = rangeText.split(',', '-').map(String::toInt)
    val results = mutableListOf<Coordinate>()
    for (y in between(minY, maxY)) {
        for (x in between(minX, maxX)) {
            results.add(Coordinate(x, y))
        }
    }
    return results
}

private fun between(from: Int, to: Int) = if (from < to) from..to else from downTo to
