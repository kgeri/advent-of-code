package me.gergo

import me.gergo.Choice.*
import me.gergo.Result.*
import java.io.File

fun main() {
    val guide = File("src/main/resources/input02.txt").readLines().map(::parseGuide2)

    val score = guide.map(Guide2::score).sum()
    println(score)
}

private val Choices = Choice.values()

enum class Result { Lose, Draw, Win }
data class Guide2(val opponent: Choice, val result: Result) {
    fun score(): Int = scoreFor(pickChoiceFor(opponent, result)) + scoreFor(result)

    private fun scoreFor(r: Result): Int = when (r) {
        Lose -> 0
        Win -> 6
        else -> 3
    }

    private fun scoreFor(c: Choice): Int = c.ordinal + 1
    private fun pickChoiceFor(o: Choice, r: Result): Choice = when (r) {
        Lose -> Choices[Math.floorMod(o.ordinal - 1, 3)]
        Win -> Choices[Math.floorMod(o.ordinal + 1, 3)]
        else -> o
    }
}

fun parseGuide2(line: String): Guide2 {
    val tokens = line.split(" ")
    val opponent = when (tokens[0]) {
        "A" -> Rock
        "B" -> Paper
        else -> Scissors
    }
    val result = when (tokens[1]) {
        "X" -> Lose
        "Y" -> Draw
        else -> Win
    }
    return Guide2(opponent, result)
}