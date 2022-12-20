package me.gergo

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.io.File

// TIL:
// - Modeling these branching / optimization problems is MUCH more readable and faster when done with state -> newState Sets (rather than DFS / 
//   recursive functions), and it also allows one to log the state space complexity easily
// - Kotlin coroutines are in a separate library (kotlinx.coroutines)
// - Coroutines are not as mysterious as its name implies - it's yet another async/await / green-threads-on-a-scheduler thing

fun main() {
    val valves = File("src/main/resources/input16.txt").readLines()
        .mapIndexed(::parseValves)
    val valvesByName = valves.associateBy(Valve::name)
    val neighbors = valves.associateWith { v -> v.tunnelsTo.map { valvesByName[it]!! } }

    // FUK. I spent HOURS wondering why the sample data passes and my puzzle input does not... "You start at valve AA", NOT the first valve :|
    val aa = valves.first { it.name == "AA" }

    // Part One
    val result1 = solveFor(aa, neighbors, 30, valves.toSet())
    println("Max pressure released: $result1")

    // Part Two
    val usefulValves = valves.filter { it.rate > 0 }.toSet()
    val maxTotalRate = usefulValves.sumOf(Valve::rate)
    val usefulValveCombinations = powerset(usefulValves) // Calculating all possible subsets, for splitting the work
        .filter {
            // Optimization: preferring more equal-sized workloads
            val totalRate = it.sumOf(Valve::rate)
            totalRate > maxTotalRate * 0.4 && totalRate < maxTotalRate * 0.6
        }

    // Note: the search space could be reduced massively, by removing all the unreachable neighbors based on the subset chosen
    // I'm just too lazy to further optimize it, and the kind-of-sort-of brute force way (16k subsets * few million state space on 16 cores) 
    // completed in a few minutes

    val tasks = mutableListOf<Deferred<Int>>()
    val result2 = runBlocking(Dispatchers.Default) { // I paid for 16 CPUs, so I'm gonna use 16 CPUs :)
        for ((i, myValves) in usefulValveCombinations.withIndex()) {
            val eliValves = usefulValves.minus(myValves) // Making sure to split the work by working on disjoint sets

            tasks.add(async {
                val myBest = solveFor(aa, neighbors, 26, myValves)
                val eliBest = solveFor(aa, neighbors, 26, eliValves)

                val total = myBest + eliBest
                println("Solved $i/${usefulValveCombinations.size}")
                total
            })
        }
        return@runBlocking tasks.maxOf { it.await() }
    }
    println("Max pressure released: $result2")
}

private fun solveFor(start: Valve, neighbors: Map<Valve, List<Valve>>, minutes: Int, valves: Set<Valve>): Int {
    var states = setOf(ValveState(start, 0L, 0, 0))
    for (i in 1..minutes) {
        val newStates = HashSet<ValveState>(states.size * 2)
        for (state in states) {
            if (valves.contains(state.atValve) && state.shouldOpen()) newStates.add(state.open())
            else { // Optimization: if you're standing on a valve, always open it and never move (cuts state space by an order of magnitude)
                for (n in neighbors[state.atValve]!!) {
                    newStates.add(state.moveTo(n))
                }
            }
        }
        // println("Minute: $i, states: ${newStates.size}")
        states = newStates
    }
    return states.maxOf { it.totalPressureReleased }
}

private data class ValveState(val atValve: Valve, val openedSet: Long, val openedRate: Int, val totalPressureReleased: Int) {

    fun moveTo(v: Valve): ValveState {
        return ValveState(v, openedSet, openedRate, totalPressureReleased + openedRate)
    }

    fun shouldOpen(): Boolean {
        return atValve.rate > 0 && !openedSet.isBitSet(atValve.index)
    }

    fun open(): ValveState {
        return ValveState(atValve, openedSet.setBit(atValve.index), openedRate + atValve.rate, totalPressureReleased + openedRate)
    }
}

private fun Long.isBitSet(index: Int): Boolean = this and (1L shl index) != 0L
private fun Long.setBit(index: Int): Long = this or (1L shl index)

private class Valve(val index: Int, val name: String, val rate: Int, val tunnelsTo: List<String>)

private val ValveTunnelFormat = Regex("Valve (\\w+) has flow rate=(\\d+); tunnels? leads? to valves? (.*)")
private fun parseValves(i: Int, line: String): Valve {
    val (_, name, rate, tunnels) = ValveTunnelFormat.matchEntire(line)!!.groupValues
    return Valve(i, name, rate.toInt(), tunnels.split(", "))
}
