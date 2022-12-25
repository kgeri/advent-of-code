package me.gergo

import java.io.File
import kotlin.math.pow

fun main() {
    val snafuNumbers = File("src/main/resources/input25.txt").readLines()

    val result1 = decimalToSnafu(snafuNumbers.map(::snafuToDecimal).sum())

    println("SNAFU number to supply to the console: $result1")
}

private fun snafuToDecimal(value: String): Long {
    var result = 0L
    for (i in value.indices) {
        val v = when (value[value.length - 1 - i]) {
            '=' -> -2
            '-' -> -1
            '0' -> 0
            '1' -> 1
            '2' -> 2
            else -> throw NumberFormatException(value)
        }
        result += 5.0.pow(i).toLong() * v
    }
    return result
}

private const val Digits = "=-012"
private fun decimalToSnafu(value: Long): String {
    var result = ""
    var rem = value
    while (rem > 0) {
        rem += 2 // Shift because of the negative values
        result = "${Digits[(rem % 5).toInt()]}$result"
        rem /= 5
    }
    return result
}