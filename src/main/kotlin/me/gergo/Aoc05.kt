package me.gergo

import java.io.File
import java.util.*

fun main() {
    val (stacks, instructions) = parseStacksAndInstructions(File("src/main/resources/input05.txt").readLines())

    println(stacks)
    for (instruction in instructions) {
        // Part One and Two - moving in bulk, the only difference is drop ordering
        val crates = (1..instruction.quantity).map { stacks[instruction.fromStack - 1].pop()!! }
        stacks[instruction.toStack - 1].addAll(crates.reversed()) // Uncomment .reversed() for Part One
    }

    stacks.forEach { print(it.peek()) }
}

typealias CrateStack = Stack<Char>

data class Instruction(val quantity: Int, val fromStack: Int, val toStack: Int) {}

fun parseStacksAndInstructions(lines: List<String>): Pair<List<CrateStack>, List<Instruction>> {
    val stacks = parseStacks(lines.subList(0, lines.indexOf("") - 1))
    val instructions = lines.subList(lines.indexOf("") + 1, lines.size).map(::parseInstruction)
    return Pair(stacks, instructions)
}

fun parseStacks(lines: List<String>): List<CrateStack> {
    val stackCount = (lines[0].length + 1) / 4
    val result = List(stackCount) { Stack<Char>() }
    for (i in lines.size - 1 downTo 0) {
        val line = lines[i]
        for (s in result.indices) {
            val crate = line[s * 4 + 1]
            if (crate != ' ') {
                result[s].add(crate)
            }
        }
    }
    return result
}

fun parseInstruction(line: String): Instruction {
    val m = Regex("move (\\d+) from (\\d+) to (\\d+)").matchEntire(line)!!
    return Instruction(m.groupValues[1].toInt(), m.groupValues[2].toInt(), m.groupValues[3].toInt())
}
