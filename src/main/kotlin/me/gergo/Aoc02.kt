package me.gergo

import me.gergo.Choice.*
import java.io.File

fun main() {
    val guide = File("src/main/resources/input02.txt").readLines().map(::parseGuide)

    val score = guide.map(Guide::score).sum()
    println(score)
}

enum class Choice { Rock, Paper, Scissors }
private data class Guide(val opponent: Choice, val my: Choice) {
    fun score(): Int = scoreFor(my) + scoreForOutcomeOf(opponent, my)

    private fun scoreFor(c: Choice): Int = c.ordinal + 1
    private fun scoreForOutcomeOf(o: Choice, m: Choice): Int {
        return if (o.ordinal == m.ordinal) 3
        else if (m == Scissors && o == Rock) 0
        else if (m == Rock && o == Scissors) 6
        else if (m.ordinal < o.ordinal) 0
        else 6
    }
}

private fun parseGuide(line: String): Guide {
    val tokens = line.split(" ")
    val opponent = when (tokens[0]) {
        "A" -> Rock
        "B" -> Paper
        else -> Scissors
    }
    val my = when (tokens[1]) {
        "X" -> Rock
        "Y" -> Paper
        else -> Scissors
    }
    return Guide(opponent, my)
}