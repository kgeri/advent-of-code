package me.gergo

import java.io.File

fun main() {
    val instructions = File("src/main/resources/input10.txt").readLines()
        .map(::parseInstr)

    // Part One
    val result = listOf(20, 60, 100, 140, 180, 220).map {
        val cpu = CPU(instructions)
        for (c in 1 until it) cpu.step()
        it * cpu.x
    }.sum()
    println(result)

    // Part Two
    val cpu = CPU(instructions)
    CRT(40, 6, cpu).draw()
}

private sealed class Instr(val duration: Int)
private object Noop : Instr(1)
private data class Addx(val value: Int) : Instr(2)

private class CPU(private val instructions: List<Instr>) {
    private var instructionPointer = 0
    private var remainingDuration = instructions[instructionPointer].duration
    var x = 1

    fun step() {
        remainingDuration--
        if (remainingDuration > 0) {
            return
        }

        when (val instr = instructions[instructionPointer]) {
            is Noop -> {}
            is Addx -> x += instr.value
        }
        if (instructionPointer >= instructions.size - 1) return
        instructionPointer++
        remainingDuration = instructions[instructionPointer].duration
    }
}

private class CRT(private val width: Int, private val height: Int, private val cpu: CPU) {
    fun draw() {
        for (y in 1..6) {
            for (x in 0..39) {
                print(if (cpu.x - 1 <= x && x <= cpu.x + 1) '#' else '.')
                cpu.step()
            }
            println()
        }
    }
}

private fun parseInstr(line: String): Instr {
    val tokens = line.split(" ")
    return if (tokens[0] == "noop") Noop
    else Addx(tokens[1].toInt())
}