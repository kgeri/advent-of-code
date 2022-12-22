package me.gergo

import java.io.File

fun main() {
    val (board, instructions) = parseMapAndInstructions(File("src/main/resources/input22.txt").readText())

    var position = board.topLeft()
    var facing = Direction.R
    for (ins in instructions) {
        when (ins) {
            Turn22.L -> facing = facing.counterClockWise()
            Turn22.R -> facing = facing.clockWise()
            is Step22 -> position = board.move(position, ins.amount, facing)
        }

        // Debug
//        println("Instruction: $ins")
//        println(board.print(position, facing))
//        println()
    }

    val row = position.y + 1
    val col = position.x + 1
    val facingValue = when (facing) {
        Direction.L -> 2
        Direction.U -> 3
        Direction.R -> 0
        Direction.D -> 1
    }
    val password = 1000 * row + 4 * col + facingValue
    println("Password: $password")
}

private class Board22(private val tiles: List<CharArray>) {

    fun topLeft() = Coordinate(row(0).first, 0)

    fun move(at: Coordinate, amount: Int, direction: Direction): Coordinate {
        val row = row(at.y)
        val col = col(at.x)
        val rowWidth = row.last - row.first + 1
        val colWidth = col.last - col.first + 1
        var x = at.x
        var y = at.y
        for (i in 1..amount) {
            when (direction) {
                Direction.L -> {
                    val nx = row.first + Math.floorMod(x - row.first - 1, rowWidth)
                    if (!isWall(nx, y)) x = nx
                }

                Direction.R -> {
                    val nx = row.first + Math.floorMod(x - row.first + 1, rowWidth)
                    if (!isWall(nx, y)) x = nx
                }

                Direction.U -> {
                    val ny = col.first + Math.floorMod(y - col.first - 1, colWidth)
                    if (!isWall(x, ny)) y = ny
                }

                Direction.D -> {
                    val ny = col.first + Math.floorMod(y - col.first + 1, colWidth)
                    if (!isWall(x, ny)) y = ny
                }
            }
        }
        return Coordinate(x, y)
    }

    private fun isWall(x: Int, y: Int) = tiles[y][x] == '#'
    private fun row(y: Int) = tiles[y].indexOfFirst { it != ' ' }..tiles[y].indexOfLast { it != ' ' }

    private fun col(x: Int): IntRange {
        var minY = Int.MAX_VALUE
        var maxY = 0
        for (y in tiles.indices) {
            if (tiles[y][x] != ' ') {
                minY = minOf(minY, y)
                maxY = maxOf(maxY, y)
            }
        }
        return minY..maxY
    }

    fun print(position: Coordinate, facing: Direction): String {
        val buf = StringBuilder()
        for (y in tiles.indices) {
            val tile = tiles[y]
            if (y == position.y) {
                val tileWithAgent = tile.copyOf()
                tileWithAgent[position.x] = when (facing) {
                    Direction.L -> '<'
                    Direction.U -> '^'
                    Direction.R -> '>'
                    Direction.D -> 'v'
                }
                buf.append(tileWithAgent)
            } else {
                buf.append(tile)
            }
            buf.append('\n')
        }
        return buf.toString()
    }
}

private sealed interface Instruction22
private enum class Turn22 : Instruction22 { L, R }
private data class Step22(val amount: Int) : Instruction22

private fun parseMapAndInstructions(text: String): Pair<Board22, List<Instruction22>> {
    val (boardText, instructionText) = text.split("\n\n")
    val tiles = boardText.split('\n').map(String::toCharArray)
    val instructions = Regex("(L|R|\\d+)+?").findAll(instructionText)
        .map {
            when (it.value) {
                "L" -> Turn22.L
                "R" -> Turn22.R
                else -> Step22(it.value.toInt())
            }
        }.toList()
    return Pair(Board22(tiles), instructions)
}
