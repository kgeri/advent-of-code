package me.gergo

import me.gergo.Resource.*
import java.io.File


fun main() {
    val blueprints = File("src/main/resources/input19.txt").readLines()
        .map(::parseBlueprints)

    val blueprint = blueprints[0]
    println("Building $blueprint")

    val memo = mutableMapOf<State, Int>()

    fun simulate(state: State, round: Int): Int {
        if (round == 0) return state.resources[GEODE]
        val cachedResult = memo[state]

        if (cachedResult != null) return cachedResult

        val buildStates = state.resources.keys
            .filter { state.canBuild(it, blueprint) }
            .filter { state.shouldBuild(it, blueprint) }
            .map { state.buildAndGather(it, blueprint) }
        val maxResourceTypes = buildStates.maxOfOrNull { it.resources.resourceTypeCount() } ?: 0

        val possibleStates = buildStates
            .filter { it.resources.resourceTypeCount() >= maxResourceTypes } // Preferring states that maximize the number of resource types
            .plus(state.gather())
        val result = possibleStates.maxOf { simulate(it, round - 1) }

        memo[state] = result
        return result
    }

    val maxGeode = simulate(State(mapOf(ORE to 1), Resources()), 24)
    println(maxGeode)
}

private enum class Resource {
    ORE, CLAY, OBSIDIAN, GEODE;
}

private class Resources() : HashMap<Resource, Int>() {
    init {
        for (type in Resource.values()) put(type, 0)
    }

    constructor(other: Resources) : this() {
        other.forEach { (type, amount) -> this[type] = amount }
    }

    fun resourceTypeCount() = count { it.value > 0 }

    override fun get(key: Resource) = super.get(key)!!

    operator fun plus(deltas: Iterable<Pair<Resource, Int>>): Resources {
        val result = Resources(this)
        deltas.forEach { (type, delta) -> result[type] = result[type] + delta }
        return result
    }

    operator fun minus(deltas: Iterable<Pair<Resource, Int>>): Resources {
        val result = Resources(this)
        deltas.forEach { (type, delta) -> result[type] = result[type] - delta }
        return result
    }
}

private data class State(val robots: Map<Resource, Int>, val resources: Resources) {

    fun canBuild(type: Resource, blueprint: Blueprint) = blueprint.costs[type]!!.all { (t, cost) -> resources[t] >= cost }

    fun shouldBuild(type: Resource, blueprint: Blueprint) = // Do not build a new robot if we can already produce the max needed amount per turn
        robots.getOrDefault(type, 0) < blueprint.maxCosts[type]!!

    fun buildAndGather(type: Resource, blueprint: Blueprint) = // Building a robot, decreasing with costs and incrementing with what was gathered
        State(robots.plus(Pair(type, robots.getOrDefault(type, 0) + 1)), resources - blueprint.costs[type]!! + gatheredResources())

    fun gather() = State(robots, resources + gatheredResources()) // Incrementing with whatever the robots gathered
    private fun gatheredResources() = robots.map { (type, count) -> Pair(type, count) }
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
