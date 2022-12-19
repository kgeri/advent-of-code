package me.gergo

import me.gergo.Resource.*
import java.io.File
import java.util.*


fun main() {
    val blueprints = File("src/main/resources/input19.txt").readLines()
        .map(::parseBlueprints)

    // Part One
//    val results1 = solve(blueprints, 24)
//    val sumQualityLevels = results1.map { (blueprint, geodes) -> blueprint.id * geodes }.sum()
//    println("Sum quality levels: $sumQualityLevels")

    // Part Two
    val results2 = solve(blueprints.take(3), 32)
    val maxGeodesMultiple = results2.values.reduce(Int::times)
    println("Max geodes multiple: $maxGeodesMultiple")
}

private fun solve(blueprints: List<Blueprint>, minutes: Int): Map<Blueprint, Int> {
    val results = mutableMapOf<Blueprint, Int>()
    for (blueprint in blueprints) {
        var states = setOf(State(mapOf(ORE to 1), emptyMap()))
        for (t in 1..minutes) {
            println("Minute $t, number of states: ${states.size}")
            val newStates = HashSet<State>(states.size * 2)

            val resourceCaps = ResourceTypes.associateWith { type ->  // Cap resource counters
                val maxCost = blueprint.maxCosts[type]!!
                if (maxCost == Int.MAX_VALUE) Int.MAX_VALUE else maxCost * (minutes - t)
            }

            for (state in states) {
                for (type in ResourceTypes) { // Generating possible builder states
                    if (!state.canBuild(type, blueprint)) continue // Not enough minerals :) 
                    if (!state.shouldBuild(type, blueprint)) continue // We already produce enough minerals of this kind to build anything
                    newStates.add(state.buildAndGather(type, blueprint, resourceCaps)) // Building a new robot
                }
                newStates.add(state.gather(resourceCaps)) // Just gathering is always an option
            }
            states = newStates
        }

        val maxGeode = states.maxOf(State::numGeodes)
        results[blueprint] = maxGeode
        println("Maximum number of geodes created: $maxGeode (Blueprint: $blueprint)")
    }
    return results
}

private val ResourceTypes = Resource.values()

private enum class Resource {
    ORE, CLAY, OBSIDIAN, GEODE;
}

private data class State(val robots: Map<Resource, Int>, val resources: Map<Resource, Int>) {

    fun canBuild(type: Resource, blueprint: Blueprint) = blueprint.costs[type]!!.all { (t, cost) -> resources.getOrDefault(t, 0) >= cost }

    fun shouldBuild(type: Resource, blueprint: Blueprint) = // Build robots only until we can produce the most expensive robot of that kind
        robots.getOrDefault(type, 0) < blueprint.maxCosts[type]!!

    fun numGeodes(): Int = resources.getOrDefault(GEODE, 0)

    fun buildAndGather(type: Resource, blueprint: Blueprint, resourceCaps: Map<Resource, Int>): State {
        val newResources = copyOf(resources)
        val newRobots = copyOf(robots) // Building a new robot
        blueprint.costs[type]!!.forEach { (t, cost) -> newResources.increment(t, -cost, resourceCaps[t]!!) } // Reducing minerals by the cost
        newRobots.increment(type, 1, resourceCaps[type]!!) // Adding the robot
        robots.forEach { (t, count) -> newResources.increment(t, count, resourceCaps[t]!!) } // Existing robots are gathering
        return State(newRobots, newResources)
    }

    fun gather(resourceCaps: Map<Resource, Int>): State {
        val newResources = copyOf(resources)
        robots.forEach { (t, count) -> newResources.increment(t, count, resourceCaps[t]!!) } // Existing robots are gathering
        return State(robots, newResources)
    }

    private fun MutableMap<Resource, Int>.increment(type: Resource, delta: Int, cap: Int) {
        val value = this.getOrDefault(type, 0)
        this[type] = minOf(value + delta, cap)
    }

    private fun copyOf(other: Map<Resource, Int>): MutableMap<Resource, Int> {
        val result = EnumMap<Resource, Int>(Resource::class.java)
        result.putAll(other)
        return result
    }
}

private data class Blueprint(
    val id: Int,
    val costs: Map<Resource, List<Pair<Resource, Int>>> // To build <type>, you need these <value, amount> pairs
) {
    val maxCosts =
        Resource.values().associateWith { type -> costs.values.flatten().filter { it.first == type }.maxOfOrNull { it.second } ?: Integer.MAX_VALUE }
}

private val BlueprintFormat =
    Regex("Blueprint (\\d+): Each ore robot costs (\\d+) ore. Each clay robot costs (\\d+) ore. Each obsidian robot costs (\\d+) ore and (\\d+) clay. Each geode robot costs (\\d+) ore and (\\d+) obsidian.")

private fun parseBlueprints(line: String): Blueprint {
    val values = BlueprintFormat.matchEntire(line)!!.groupValues.drop(1).map(String::toInt)
    return Blueprint(
        values[0],
        mapOf(
            ORE to listOf(Pair(ORE, values[1])),
            CLAY to listOf(Pair(ORE, values[2])),
            OBSIDIAN to listOf(Pair(ORE, values[3]), Pair(CLAY, values[4])),
            GEODE to listOf(Pair(ORE, values[5]), Pair(OBSIDIAN, values[6])),
        )
    )
}
