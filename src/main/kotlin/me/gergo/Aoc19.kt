package me.gergo

import me.gergo.Resource.*
import java.io.File

// TIL:
// - Kotlin mutableMap / mutableSet are backed by LinkedHashMap, which is bloody slow
// - You'd expect that a java.util.EnumMap's hashCode / equals is fast, but the darned thing is _allocating Iterators_ <facepalm>
// - Copying objects of any kind becomes awful when you do it millions of times (duh)
// - Modeling with primitive fields in mutable objects this lead to at least an order of magnitude improvement
// Still, for problems like this one, picking the right branch pruning methods are what really matters.

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
        var states = setOf(State())
        for (t in 1..minutes) {
            println("Minute $t, number of states: ${states.size}")
            val newStates = HashSet<State>(states.size * 2)

            // Cap resource counters with maxCost * minutesLeft (as we won't ever need more of that resource)
            val minutesLeft = minutes - t
            val resourceCaps = ResourceTypes.associateWith { type ->
                val maxCost = blueprint.maxCosts[type]!!
                if (maxCost == Int.MAX_VALUE) Int.MAX_VALUE else maxCost * minutesLeft
            }

            for (state in states) {
                for (type in ResourceTypes) { // Generating possible builder states
                    if (!state.canBuild(type, blueprint)) continue // Not enough minerals :) 
                    if (!state.shouldBuild(type, blueprint, minutesLeft)) continue // We already produce enough minerals of this kind
                    newStates.add(state.buildAndGather(type, blueprint, resourceCaps, minutesLeft)) // Building a new robot
                }
                newStates.add(state.gather(resourceCaps)) // Just gathering is always an option
            }
            states = newStates
        }

        val maxGeode = states.maxOf { it.stock(GEODE) }
        results[blueprint] = maxGeode
        println("Maximum number of geodes created: $maxGeode (Blueprint: $blueprint)")
    }
    return results
}

private val ResourceTypes = Resource.values()

private enum class Resource {
    ORE, CLAY, OBSIDIAN, GEODE;
}

private data class State(
    var oreRobots: Int,
    var clayRobots: Int,
    var obsidianRobots: Int,
    var geodeRobots: Int,
    var ore: Int,
    var clay: Int,
    var obsidian: Int,
    var geode: Int
) {
    constructor() : this(1, 0, 0, 0, 0, 0, 0, 0)

    fun robotCount(type: Resource) = when (type) {
        ORE -> oreRobots
        CLAY -> clayRobots
        OBSIDIAN -> obsidianRobots
        GEODE -> 0 // Optimization: Geode robots are not tracked
    }

    fun stock(type: Resource) = when (type) {
        ORE -> ore
        CLAY -> clay
        OBSIDIAN -> obsidian
        GEODE -> geode
    }

    fun canBuild(type: Resource, blueprint: Blueprint) = blueprint.costs[type]!!.all { (t, cost) -> stock(t) >= cost }

    fun shouldBuild(type: Resource, blueprint: Blueprint, minutesLeft: Int): Boolean {
        // Build robots only until we can produce the most expensive robot of that kind
        return robotCount(type) < blueprint.maxCosts[type]!!
    }

    fun buildAndGather(type: Resource, blueprint: Blueprint, resourceCaps: Map<Resource, Int>, minutesLeft: Int): State {
        val result = gather(resourceCaps) // Existing robots are gathering

        // Optimization for Geode bots = we don't have to keep track of them, just immediately add a geode count of minutesLeft to the resources
        if (type == GEODE) {
            for ((t, cost) in blueprint.costs[type]!!) {
                result.updateStock(t, -cost, resourceCaps) // Reducing minerals by the cost
            }
            result.updateStock(GEODE, minutesLeft, resourceCaps) // Fast-forwarding geode count
            return result
        }

        // Building a new robot
        for ((t, cost) in blueprint.costs[type]!!) {
            result.updateStock(t, -cost, resourceCaps) // Reducing minerals by the cost
        }
        result.addRobot(type)
        return result
    }

    fun gather(resourceCaps: Map<Resource, Int>): State {
        val result = State(oreRobots, clayRobots, obsidianRobots, geodeRobots, ore, clay, obsidian, geode)
        result.updateStock(ORE, oreRobots, resourceCaps)
        result.updateStock(CLAY, clayRobots, resourceCaps)
        result.updateStock(OBSIDIAN, obsidianRobots, resourceCaps)
        // Optimization: Geode robots are not tracked
        return result
    }

    private fun addRobot(type: Resource) {
        when (type) { // Adding the robot
            ORE -> oreRobots++
            CLAY -> clayRobots++
            OBSIDIAN -> obsidianRobots++
            else -> {} // Optimization: Geode robots are not tracked
        }
    }

    private fun updateStock(type: Resource, delta: Int, resourceCaps: Map<Resource, Int>) {
        when (type) {
            ORE -> ore = minOf(ore + delta, resourceCaps[ORE]!!)
            CLAY -> clay = minOf(clay + delta, resourceCaps[CLAY]!!)
            OBSIDIAN -> obsidian = minOf(obsidian + delta, resourceCaps[OBSIDIAN]!!)
            GEODE -> geode = minOf(geode + delta, resourceCaps[GEODE]!!)
        }
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
