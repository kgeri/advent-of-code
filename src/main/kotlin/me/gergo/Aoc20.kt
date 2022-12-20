package me.gergo

import java.io.File

fun main() {
    val mixedNumbers = File("src/main/resources/input20.txt").readLines()
        .map(String::toLong)
        .map { it * 811589153 } // Part Two - apply decryption key
        .map { NumberList(it) }

    mixedNumbers.reduce(NumberList::add)
    mixedNumbers[0].loopWith(mixedNumbers[mixedNumbers.size - 1])

    for (i in 1..10) // Part Two - mix 10 times
        for (n in mixedNumbers) {
            // Doing a `mod size-1` to speed things up
            n.moveBy(Math.floorMod(n.value, mixedNumbers.size - 1L))
        }

    val zero = mixedNumbers.first { it.value == 0L }
    val result1 = zero.next(1000).value + zero.next(2000).value + zero.next(3000).value
    println("Sum of coordinates: $result1")
}

private class NumberList(val value: Long) {
    private var prev: NumberList? = null
    private var next: NumberList? = null

    fun add(other: NumberList): NumberList {
        next = other
        other.prev = this
        return other
    }

    fun next(steps: Long): NumberList {
        var result = this
        for (i in 1..steps) result = result.next!!
        return result
    }

    fun prev(steps: Long): NumberList {
        var result = this
        for (i in 1..steps) result = result.prev!!
        return result
    }

    fun loopWith(tail: NumberList) {
        this.prev = tail
        tail.next = this
    }

    fun moveBy(steps: Long) {
        if (steps > 0) {
            moveAfter(next(steps))
        } else {
            moveBefore(prev(-steps))
        }
    }

    private fun moveBefore(other: NumberList) {
        if (other == this) return
        remove()
        val prev = other.prev!!
        prev.next = this
        other.prev = this
        this.prev = prev
        this.next = other
    }

    private fun moveAfter(other: NumberList) {
        if (other == this) return
        remove()
        val next = other.next!!
        other.next = this
        next.prev = this
        this.prev = other
        this.next = next
    }

    private fun remove() {
        val prev = this.prev!!
        val next = this.next!!
        prev.next = next
        next.prev = prev
        this.prev = null
        this.next = null
    }

    override fun toString(): String {
        val buf = StringBuilder()
        var current: NumberList? = this
        while (current != null) {
            if (current != this) buf.append(',')
            buf.append(current.value)
            current = current.next
            if (current == this) break
        }
        return buf.toString()
    }
}
