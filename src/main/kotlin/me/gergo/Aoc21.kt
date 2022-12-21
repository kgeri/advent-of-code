package me.gergo

import java.io.File
import java.util.*

fun main() {
    val monkeys = File("src/main/resources/input21.txt").readLines()
        .map(::parseMonkey)

    val monkeyByName = monkeys.associateBy { it.name }
    val root = (monkeyByName["root"] as ExpressionMonkey21?)!!

    // Part One - this one is simple: we just do a stack-based evaluation of the expressions
    // As a side effect, this builds a cache of evaluation results for all equations, which turns out to be handy for Part Two as well
    val values = evaluate(root, monkeyByName)
    val result1 = values["root"]
    println("Root yells: $result1")

    // Part Two
    val human = monkeyByName["humn"]!!

    // Finding out which side of root's equation we're on
    val neighborFn = { m: Monkey21 ->
        when (m) {
            is ExpressionMonkey21 -> sequenceOf(monkeyByName[m.monkeyA]!!, monkeyByName[m.monkeyB]!!)
            else -> emptySequence()
        }
    }
    val humanPath = dijkstra(root, monkeys, neighborFn) { _, _ -> 1.0 }.shortestPath(human)
    val humanSide = humanPath[0] as ExpressionMonkey21
    
    // One side of the equation will be constant - in fact we've calculated it already!
    val monkeySideValue = (if (root.monkeyA == humanSide.name) values[root.monkeyB]!! else values[root.monkeyA]!!)

    // Invalidating the path of values leading to 'humn'
    // The idea is that on that path, exactly one side of the two-sided equations will be unknown
    val values2 = values.minus(humanPath.map { it.name }.toSet())

    // Finally, we do a similar stack-based traversal as in evaluate(), but this time we have an unknown in each of the equations, so applying the 
    // inverse of the operations for either side A or side B
    val stack = Stack<EqualsMonkey21>()
    stack.add(EqualsMonkey21(humanSide.name, monkeySideValue))
    while (stack.isNotEmpty()) {
        val m = stack.pop()
        val expr = monkeyByName[m.name]!!

        if (expr !is ExpressionMonkey21) {
            println("${expr.name} should be: ${m.expected}")
            break
        }

        val a = values2[expr.monkeyA]
        val b = values2[expr.monkeyB]

        if (a != null && b != null) {
            throw IllegalStateException("Malformed expression tree: at least one side should be unknown")
        } else if (a != null) { // Side A is known, pushing equations for side B
            when (expr.operation) {
                '+' -> stack.push(EqualsMonkey21(expr.monkeyB, m.expected - a))
                '-' -> stack.push(EqualsMonkey21(expr.monkeyB, a - m.expected))
                '*' -> stack.push(EqualsMonkey21(expr.monkeyB, m.expected / a))
                '/' -> stack.push(EqualsMonkey21(expr.monkeyB, a / m.expected))
                else -> throw UnsupportedOperationException("Not supported: ${expr.operation}")
            }
        } else if (b != null) { // Side B is known, pushing equations for side A
            when (expr.operation) {
                '+' -> stack.push(EqualsMonkey21(expr.monkeyA, m.expected - b))
                '-' -> stack.push(EqualsMonkey21(expr.monkeyA, m.expected + b))
                '*' -> stack.push(EqualsMonkey21(expr.monkeyA, m.expected / b))
                '/' -> stack.push(EqualsMonkey21(expr.monkeyA, m.expected * b))
                else -> throw UnsupportedOperationException("Not supported: ${expr.operation}")
            }
        } else {
            throw IllegalStateException("At least one side should have been computed (${expr.monkeyA}, ${expr.monkeyB})")
        }
    }
}

private fun evaluate(root: ExpressionMonkey21, monkeyByName: Map<String, Monkey21>): Map<String, Long> {
    val constants = monkeyByName.values.filterIsInstance<ValueMonkey21>().associateBy({ it.name }, { it.value })
    val values = constants.toMutableMap()
    val stack = Stack<ExpressionMonkey21>()
    stack.add(root)
    while (stack.isNotEmpty()) {
        val m = stack.pop()
        val a = values[m.monkeyA]
        val b = values[m.monkeyB]
        if (a != null && b != null) {
            when (m.operation) {
                '+' -> values[m.name] = a + b
                '-' -> values[m.name] = a - b
                '*' -> values[m.name] = a * b
                '/' -> values[m.name] = a / b
                else -> throw UnsupportedOperationException("Not supported: ${m.operation}")
            }
        } else {
            stack.push(m) // Retry the same calculation once A or B or both are resolved
            if (a == null) stack.push(monkeyByName[m.monkeyA]!! as ExpressionMonkey21?)
            if (b == null) stack.push(monkeyByName[m.monkeyB]!! as ExpressionMonkey21?)
        }
    }
    return values
}

private abstract class Monkey21(val name: String)
private class ValueMonkey21(name: String, val value: Long) : Monkey21(name)
private class ExpressionMonkey21(name: String, val monkeyA: String, val operation: Char, val monkeyB: String) : Monkey21(name)
private class EqualsMonkey21(name: String, val expected: Long) : Monkey21(name) // For Part Two

private fun parseMonkey(line: String): Monkey21 {
    val (name, rest) = line.split(": ")
    val tokens = rest.split(" ")
    return if (tokens.size == 1) ValueMonkey21(name, tokens[0].toLong())
    else ExpressionMonkey21(name, tokens[0], tokens[1][0], tokens[2])
}