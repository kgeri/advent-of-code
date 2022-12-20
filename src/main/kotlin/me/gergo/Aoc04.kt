package me.gergo

import java.io.File

fun main() {
    val assignments = File("src/main/resources/input04.txt").readLines()
        .map(::parseAssignment)

    // val results = assignments.count(Assignment::fullyContains) // Part One
    val results = assignments.count(Assignment::overlaps) // Part Two
    println(results)
}

private data class Assignment(val first: IntRange, val second: IntRange) {
    fun fullyContains() = first.minus(second).isEmpty() || second.minus(first).isEmpty()
    fun overlaps() = first.minus(second).size < first.count() || second.minus(first).size < second.count()
}

private fun parseAssignment(line: String): Assignment {
    val ranges = line.split(",").map {
        val range = it.split("-").map(String::toInt)
        range[0]..range[1]
    }
    return Assignment(ranges[0], ranges[1])
}
