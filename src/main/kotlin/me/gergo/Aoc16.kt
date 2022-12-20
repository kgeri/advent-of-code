package me.gergo

import java.io.File

// TIL:
// - Modeling these branching / optimization problems is MUCH more readable and faster when done with state -> newState Sets (rather than DFS / 
//   recursive functions), and it also allows one to log the state space complexity easily

fun main() {
    val valves = File("src/main/resources/input16.txt").readLines()
        .mapIndexed(::parseValves)
    val valvesByName = valves.associateBy(Valve::name)
    val neighbors = valves.associateWith { v -> v.tunnelsTo.map { valvesByName[it]!! } }

    // FUK. I spent HOURS wondering why the sample data passes and my puzzle input does not... "You start at valve AA", NOT the first valve :|
    val aa = valves.first { it.name == "AA" }

    // Part One
    var states = setOf(ValveState(aa, emptySet(), 0))
    for (i in 1..30) {

        val newStates = HashSet<ValveState>(states.size * 2)
        for (state in states) {
            if (state.shouldOpen()) newStates.add(state.open())
            for (n in neighbors[state.atValve]!!) {
                newStates.add(state.moveTo(n))
            }
        }
        println("Minute: $i, states: ${newStates.size}")
        states = newStates
    }

    val result1 = states.maxOf { it.totalPressureReleased }
    println("Max pressure released: $result1")
}

private data class ValveState(val atValve: Valve, val opened: Set<Valve>, val totalPressureReleased: Int) {

    fun moveTo(v: Valve): ValveState {
        val pressureReleased = opened.sumOf(Valve::rate)
        return ValveState(v, opened, totalPressureReleased + pressureReleased)
    }

    fun shouldOpen(): Boolean {
        return atValve.rate > 0 && !opened.contains(atValve)
    }

    fun open(): ValveState {
        val pressureReleased = opened.sumOf(Valve::rate)
        return ValveState(atValve, opened.plus(atValve), totalPressureReleased + pressureReleased)
    }
}

private data class Valve(val index: Int, val name: String, val rate: Int, val tunnelsTo: List<String>)

private val ValveTunnelFormat = Regex("Valve (\\w+) has flow rate=(\\d+); tunnels? leads? to valves? (.*)")
private fun parseValves(i: Int, line: String): Valve {
    val (_, name, rate, tunnels) = ValveTunnelFormat.matchEntire(line)!!.groupValues
    return Valve(i, name, rate.toInt(), tunnels.split(", "))
}
