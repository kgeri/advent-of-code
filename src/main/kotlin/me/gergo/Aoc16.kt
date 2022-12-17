package me.gergo

import java.io.File
import kotlin.math.max

fun main() {
    val valves = File("src/main/resources/input16.txt").readLines()
        .mapIndexed(::parseValves)
    val valvesByName = valves.associateBy(Valve::name)
    val neighbors = valves.associateWith { v -> v.tunnelsTo.map { valvesByName[it]!! } }

    // Part One - dynamic programming, memoization
    data class MemoKey(val valve: Valve, val opened: Set<Valve>, val minute: Int)

    val memo = mutableMapOf<MemoKey, Int>()

    fun findMaxPressure(current: Valve, opened: Set<Valve>, minute: Int): Int {
        val key = MemoKey(current, opened, minute)
        var max = memo[key]
        if (max == null) {
            val pressureReleased = opened.sumOf(Valve::rate)
            max = pressureReleased + if (minute == 30) 0
            else {
                max(
                    if (current.rate > 0 && !opened.contains(current)) // Stay here and open current valve (if the valve is not damaged)
                        findMaxPressure(current, opened.plus(current), minute + 1) else 0,
                    neighbors[current]!!.maxOf { // ...or don't open the valve and move to a neighbor
                        findMaxPressure(it, opened, minute + 1)
                    }
                )
            }
            memo[key] = max
        }
        return max
    }

    // FUK. I spent HOURS wondering why the sample data passes and my puzzle input does not... "You start at valve AA", NOT the first valve :|
    val aa = valves.first { it.name == "AA" }

    val result1 = findMaxPressure(aa, emptySet(), 1)
    println("Max pressure released : $result1")

    // Part Two
    data class MemoKey2(
        val me: Valve,
        val eli: Valve,
        val opened: Long,
        val openedRate: Int,
        val meVisited: Long,
        val eliVisited: Long,
        val minute: Int
    ) {
        fun meCanOpen() = me.rate > 0 && !opened.isBitSet(me.index)
        fun eliCanOpen() = eli.rate > 0 && !opened.isBitSet(eli.index)

        fun meNeighbors() = neighbors[me]!!
        fun eliNeighbors() = neighbors[eli]!!

        fun meMovesEliOpens(neighbor: Valve) =
            MemoKey2(neighbor, eli, opened.setBit(eli.index), openedRate + eli.rate, meVisited.setBit(me.index), eliVisited, minute + 1)

        fun eliMovesMeOpens(neighbor: Valve) =
            MemoKey2(me, neighbor, opened.setBit(me.index), openedRate + me.rate, meVisited, eliVisited.setBit(eli.index), minute + 1)

        fun weBothOpen() =
            MemoKey2(me, eli, opened.setBit(me.index).setBit(eli.index), openedRate + me.rate + eli.rate, meVisited, eliVisited, minute + 1)

        fun weBothMove(n1: Valve, n2: Valve) =
            MemoKey2(n1, n2, opened, openedRate, meVisited.setBit(me.index), eliVisited.setBit(eli.index), minute + 1)
    }

    val memo2 = mutableMapOf<MemoKey2, Int>()

    fun permutations(k: MemoKey2) = sequence {
        if (k.meCanOpen() && k.eliCanOpen() && k.me != k.eli) yield(k.weBothOpen()) // Both valves can be opened
        if (k.meCanOpen())
            for (n in k.eliNeighbors()) { // My valve can be opened, Eli can move
                if (k.meVisited.isBitSet(n.index)) continue // Eli shouldn't go where I went
                yield(k.eliMovesMeOpens(n))
            }
        if (k.eliCanOpen())
            for (n in k.meNeighbors()) { // Eli's valve can be opened, I can move
                if (k.eliVisited.isBitSet(n.index)) continue // I shouldn't go where Eli went
                yield(k.meMovesEliOpens(n))
            }
        for (mn in k.meNeighbors()) { // We both move
            if (k.eliVisited.isBitSet(mn.index)) continue // I shouldn't go where Eli went
            for (en in k.eliNeighbors()) {
                if (k.meVisited.isBitSet(en.index)) continue // Eli shouldn't go where I went
                yield(k.weBothMove(mn, en))
            }
        }
    }

    fun findMaxPressureWithElephant(key: MemoKey2): Int {
        if (key.minute == 26) return key.openedRate // We're done!

        val cachedMax = memo2[key]
        if (cachedMax != null) return cachedMax

        val max = key.openedRate + (permutations(key).map(::findMaxPressureWithElephant).maxOrNull() ?: 0)
        memo2[key] = max

        if (memo2.size % 1000000 == 0) println("Memo entries: ${memo2.size}")
        return max
    }

    val result2 = findMaxPressureWithElephant(MemoKey2(aa, aa, 0L, 0, 0L, 0L, 1))
    println("Max pressure released with Eli: $result2")
}

private fun Long.isBitSet(index: Int): Boolean = this and (1L shl index) != 0L
private fun Long.setBit(index: Int): Long = this or (1L shl index)

private data class Valve(val index: Int, val name: String, val rate: Int, val tunnelsTo: List<String>)

private val ValveTunnelFormat = Regex("Valve (\\w+) has flow rate=(\\d+); tunnels? leads? to valves? (.*)")
private fun parseValves(i: Int, line: String): Valve {
    val (_, name, rate, tunnels) = ValveTunnelFormat.matchEntire(line)!!.groupValues
    return Valve(i, name, rate.toInt(), tunnels.split(", "))
}
