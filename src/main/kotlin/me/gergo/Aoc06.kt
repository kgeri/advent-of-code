package me.gergo

import java.io.File

fun main() {
    val chars = File("src/main/resources/input06.txt").readLines()[0]

    val windowSize = 14 // Make it 4 for Part One
    val result = chars.windowed(windowSize).indexOfFirst { it.toSet().size == windowSize } + windowSize
    println(result)
}
