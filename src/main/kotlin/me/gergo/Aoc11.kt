package me.gergo

import java.io.File
import java.util.*

private const val Rounds = 10000

/**
 * Note: this only has Part Two, as I did Part One with a naiive solution that probably takes all the memory in the world, multiplying horribly large
 * BigDecimals :) The trick in this one is to remember but delay the computation for as long as possible. As we're only really interested in whether
 * the result at the end of a long chain of operations is divisible by N, we can modulo N per every operation - but we must do that for each monkey
 * individually, which means we must remember the computation chain (Items.operations in this implementation)!
 */
fun main() {
    val monkeys = File("src/main/resources/input11.txt").readText().split("\n\n")
        .map { it.split("\n") }
        .map(::parseMonkey)


    for (i in 1..Rounds) {
        for (monkey in monkeys) {
            monkey.inspectAndThrowAll(monkeys)
        }
    }

    for (monkey in monkeys) {
        println("Monkey ${monkey.name} inspected items ${monkey.inspections} times")
    }

    val result = monkeys.map(Monkey::inspections).sortedDescending().take(2).reduce(Long::times)
    println("Level of monkey business: $result")
}

private class Monkey(val name: Int, val items: Queue<Item>, val operation: Operation, val divisor: Int, val throwToTrue: Int, val throwToFalse: Int) {
    var inspections = 0L

    fun inspectAndThrowAll(monkeys: List<Monkey>) {
        while (true) {
            val item = items.poll() ?: return
            item.operations.add(operation)
            inspections++
            val throwTo = if (item.divisibleBy(divisor)) throwToTrue else throwToFalse
            monkeys[throwTo].catch(item)
        }
    }

    fun catch(item: Item) {
        items.add(item)
    }
}

private data class Item(val value: Int, val operations: MutableList<Operation>) {
    fun divisibleBy(divisor: Int): Boolean {
        var current = value
        for (operation in operations) {
            current = when (operation) {
                is Mul -> (current * operation.multiplier) % divisor
                is Add -> (current + operation.add) % divisor
                Square -> (current * current) % divisor
            }
        }
        return current % divisor == 0
    }
}

private sealed interface Operation
private data class Mul(val multiplier: Int) : Operation
private data class Add(val add: Int) : Operation
private object Square : Operation

private fun parseMonkey(lines: List<String>): Monkey {
    val name = lines[0].replace(Regex("\\D+"), "").toInt()
    val items = lines[1].substring("  Starting items: ".length).split(", ").map { Item(it.toInt(), mutableListOf()) }
    val operation = parseOperation(lines[2].substring("  Operation: new = ".length))
    val divisor = lines[3].substring("  Test: divisible by ".length).toInt()
    val throwToTrue = lines[4].substring("    If true: throw to monkey ".length).toInt()
    val throwToFalse = lines[5].substring("    If false: throw to monkey ".length).toInt()
    return Monkey(
        name,
        items.toCollection(LinkedList()),
        operation,
        divisor,
        throwToTrue,
        throwToFalse
    )
}

private fun parseOperation(value: String): Operation {
    val mr = Regex("(\\w+) ([*+]) (\\w+)").matchEntire(value)!!
    assert(mr.groupValues[1] == "old")
    val op = mr.groupValues[2]
    val opB = mr.groupValues[3]
    return if (op == "*") {
        if (opB == "old") {
            Square
        } else {
            Mul(opB.toInt())
        }
    } else {
        Add(opB.toInt())
    }
}
