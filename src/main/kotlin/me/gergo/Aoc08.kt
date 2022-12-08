package me.gergo

import java.io.File
import java.lang.Integer.min

fun main() {
    val trees = Trees(File("src/main/resources/input08.txt").readLines())

//    println(trees)
    println("Visible trees: ${trees.visibleTrees()}") // Part One
    println("Max scenic score: ${trees.maxScenicScore()}") // Part Two
}

class Trees(lines: List<String>) {
    private val forest: Array<IntArray>
    private val width: Int
    private val height: Int

    init {
        forest = lines.map { it.toCharArray().map(Char::digitToInt).toIntArray() }.toTypedArray()
        width = forest[0].size
        height = forest.size
    }

    fun visibleTrees(): Int {
        return forest.mapIndexed { r, row ->
            row.filterIndexed { c, _ ->
                isVisible(r, c)
            }.count()
        }.sum()
    }

    fun maxScenicScore(): Int {
        return forest.flatMapIndexed() { r, row ->
            row.mapIndexed { c, _ ->
                scenicScore(r, c)
            }
        }.max()
    }

    private fun scenicScore(r: Int, c: Int): Int {
        val tree = forest[r][c]
        val leftVD = viewDistance(row(r, 0, c).reversed(), tree)
        val rightVD = viewDistance(row(r, c + 1, width).asIterable(), tree)
        val upVD = viewDistance(col(c, 0, r).reversed(), tree)
        val downVD = viewDistance(col(c, r + 1, height).asIterable(), tree)
        return upVD * leftVD * rightVD * downVD
    }

    private fun viewDistance(trees: Iterable<Int>, fromTree: Int): Int {
        var count = 0
        for (tree in trees) {
            count++
            if (tree >= fromTree) break
        }
        return count
    }

    private fun isVisible(r: Int, c: Int): Boolean {
        // Trees in the before/after the specified tree have a maximum height less than the tree's
        val tree = forest[r][c]
        return maxInRow(r, 0, c) < tree
                || maxInRow(r, c + 1, width) < tree
                || maxInCol(c, 0, r) < tree
                || maxInCol(c, r + 1, height) < tree
    }

    override fun toString(): String {
        return forest.joinToString("\n") { it.joinToString(transform = Int::toString) }
    }

    private fun maxInRow(r: Int, fromColumn: Int, toColumn: Int) = row(r, fromColumn, toColumn).maxOrNull() ?: -1

    private fun maxInCol(c: Int, fromRow: Int, toRow: Int) = col(c, fromRow, toRow).maxOrNull() ?: -1
    private fun row(r: Int, fromColumn: Int, toColumn: Int) = forest[r].sliceArray(fromColumn until min(toColumn, width))
    private fun col(c: Int, fromRow: Int, toRow: Int) = (fromRow until min(toRow, height)).map(forest::get).map { it[c] }.toIntArray()
}