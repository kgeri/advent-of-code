package me.gergo

import java.io.File
import java.util.*

private const val Rounds = 10000

/**
 * Note: this only has Part Two, as I did Part One with a naiive solution that probably takes all the memory in the world, multiplying horribly large
 * BigDecimals :) The trick in this one is to remember to delay the computation for as long as possible. As we're only really interested in whether
 * the result at the end of a long chain of operations is divisible by N, we can modulo N per every operation - but we must do that for each monkey
 * individually, which means we must remember the computation chain (Items.operations in this implementation)!
 * 
 * Note2: I'm an idiot, and one of my previous ideas of using the LCM of the divisors actually works (and obviously doesn't require one to store the
 * history of the operations) - I just didn't realize it worked because my Items were storing the value as an Int... it's interesting to think about, 
 * though - if the task were to be a bit differently parameterized (eg. with larger starting values), then Long wouldn't be enough either!
 */
fun main() {
    val monkeys = File("src/main/resources/input11.txt").readText().split("\n\n")
        .map { it.split("\n") }
        .map(::parseMonkey)


    val lcm = monkeys.map(Monkey::divisor).reduce(::lcm)
    println("LCM = $lcm")

    for (i in 1..Rounds) {
        for (monkey in monkeys) {
            monkey.inspectAndThrowAll(monkeys, lcm)
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

    fun inspectAndThrowAll(monkeys: List<Monkey>, lcm: Int) {
        while (true) {
            val item = items.poll() ?: return
            item.apply(operation, lcm)
            inspections++
            val throwTo = if (item.divisibleBy(divisor)) throwToTrue else throwToFalse
            monkeys[throwTo].catch(item)
        }
    }

    fun catch(item: Item) {
        items.add(item)
    }
}

private data class Item(var value: Long) {
    fun apply(operation: Operation, lcm: Int) {
        value = when (operation) {
            is Mul -> (value * operation.multiplier) % lcm
            is Add -> (value + operation.add) % lcm
            Square -> (value * value) % lcm
        }
    }

    fun divisibleBy(divisor: Int) = value % divisor == 0L
}

private sealed interface Operation
private data class Mul(val multiplier: Int) : Operation
private data class Add(val add: Int) : Operation
private object Square : Operation

private fun parseMonkey(lines: List<String>): Monkey {
    val name = lines[0].replace(Regex("\\D+"), "").toInt()
    val items = lines[1].substring("  Starting items: ".length).split(", ").map { Item(it.toLong()) }
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
