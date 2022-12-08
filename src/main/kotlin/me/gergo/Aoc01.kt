package me.gergo

import java.io.File

fun main() {
    val lines = File("src/main/resources/input01.txt").readLines()

    val elves = mutableListOf<Elf>()
    var lastElf = Elf()
    elves.add(lastElf)
    for (line in lines) {
        if (line.isBlank()) {
            lastElf = Elf()
            elves.add(lastElf)
        } else {
            lastElf.calories += line.toInt()
        }
    }

    println(elves
        .sortedByDescending { it.calories }
        .take(3)
        .sumOf { it.calories })
}

class Elf {
    var calories = 0;
}