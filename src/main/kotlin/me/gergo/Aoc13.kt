package me.gergo

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import java.io.File

fun main() {
    val packetPairs = File("src/main/resources/input13.txt").readText().split("\n\n")
        .map(::parsePacketPair)

    // Part One
    val result1 = packetPairs
        .mapIndexed { i, p -> if (p.first < p.second) i + 1 else 0 }
        .sum()
    println("Sum of packet indices in right order: $result1")

    // Part Two
    val divider1 = Packet(listOf(listOf(2)))
    val divider2 = Packet(listOf(listOf(6)))
    val result2 = File("src/main/resources/input13.txt").readLines()
        .asSequence()
        .filter { it.isNotBlank() }
        .map(::parsePacket)
        .plus(divider1)
        .plus(divider2)
        .sorted()
        .mapIndexed { i, p -> if (p == divider1 || p == divider2) i + 1 else 0 }
        .filter { it != 0 }
        .reduce(Int::times)
    println("Decoder key: $result2")
}

private data class Packet(val value: List<Any>) : Comparable<Packet> {
    override fun compareTo(other: Packet) = compareLists(value, other.value)
}

private fun compareLists(a: List<*>, b: List<*>): Int {
    for (i in a.indices) {
        if (i == b.size) return 1 // Right side ran out of items => left is "larger"
        val cmp = compareValues(a[i], b[i])
        if (cmp != 0) return cmp // One of the items was larger or smaller
    }
    if (a.size < b.size) return -1 // Left side ran out of items => right is "larger"
    return 0 // Equally large and items were equal, too
}

private fun compareValues(a: Any?, b: Any?): Int {
    return if (a is Int && b is Int) {
        a.compareTo(b)
    } else if (a is List<*> && b is List<*>) {
        compareLists(a, b)
    } else if (a is Int) {
        compareLists(listOf(a), b as List<*>)
    } else {
        compareLists(a as List<*>, listOf(b))
    }
}

private fun parsePacketPair(packets: String): Pair<Packet, Packet> {
    val (p1, p2) = packets.split("\n")
    return Pair(parsePacket(p1), parsePacket(p2))
}

private fun parsePacket(line: String) = packetGrammar.parseToEnd(line)

private val packetGrammar = object : Grammar<Packet>() {
    val comma by literalToken(",")
    val openingBracket by literalToken("[")
    val closingBracket by literalToken("]")
    val integer by regexToken("\\d+")
    val whiteSpace by regexToken("\\s+", true)

    val integerParser = integer map { it.text.toInt() }
    val listParser = (-openingBracket and separated(parser(this::value), comma, true) and -closingBracket)
        .map { it.terms }
    val value: Parser<Any> = integerParser or listParser
    override val rootParser = listParser map ::Packet
}
