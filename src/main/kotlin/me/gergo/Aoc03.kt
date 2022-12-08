package me.gergo

import java.io.File

fun main() {
    val guide = File("src/main/resources/input03.txt").readLines()
        .map(::parseRucksack)
        .chunked(3) // For Part Two

    // val results = guide.map(Rucksack::priorityOfSharedType).sum() // For Part One
    val results = guide.map(::priorityOfSharedType).sum() // For Part Two
    println(results)
}

data class Rucksack(val c1: String, val c2: String) {
    fun priorityOfSharedType() = priorityOf(sharedType()) // For Part One

    private fun sharedType(): Char = c1.toCharArray().intersect(c2.toCharArray().toSet()).first()
}

fun parseRucksack(line: String): Rucksack {
    return Rucksack(line.substring(0, line.length / 2), line.substring(line.length / 2))
}

fun priorityOfSharedType(group: Iterable<Rucksack>): Int {
    val sharedType = group.map { (it.c1 + it.c2).toCharArray().toSet() }.reduce { acc, r -> acc.intersect(r) }.first()
    return priorityOf(sharedType)
}

fun priorityOf(type: Char): Int {
    val d = type.code
    return if (d >= 'a'.code && d <= 'z'.code) d - 'a'.code + 1
    else d - 'A'.code + 27
}
