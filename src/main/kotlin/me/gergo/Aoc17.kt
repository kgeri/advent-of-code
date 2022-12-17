package me.gergo

import java.io.File
import kotlin.math.max

fun main() {
    val jets = File("src/main/resources/input17.txt").readText().toCharArray().map {
        when (it) {
            '>' -> +1
            '<' -> -1
            else -> throw UnsupportedOperationException()
        }
    }
    val rocks = mutableListOf<Rock>()
    val rockTypes = RockType.values()
    // val iterations = 2022L // Part One
    val iterations = 1000000000000L // Part Two

    var rockIndex = 0L
    var jetIndex = 0
    var skippedHeight = 0L
    val watermarkCache = mutableMapOf<WatermarkKey, Pair<Long, Long>>()
    while (rockIndex < iterations) {
        rockIndex++
        val rockType = rockTypes[((rockIndex - 1) % rockTypes.size).toInt()]
        val rock = Rock(2, rocks.height() + rockType.height + 3, rockType)
        rocks.add(rock)

        while (true) {
            val jet = jets[jetIndex]
            jetIndex = (jetIndex + 1) % jets.size
            rock.tryPush(jet, rocks)
            if (!rock.tryFall(rocks)) break
        }

        // Caching watermark
        val height = rocks.height()
        val watermark = rocks.watermark()
        val key = WatermarkKey(rockType, jetIndex, watermark.normalize())

        val last = watermarkCache[key]
        if (last != null) {
            val (lastRockIndex, lastHeight) = last
            val cycleDuration = rockIndex - lastRockIndex
            val cycleHeight = height - lastHeight
            val remainingFullCycles = (iterations - rockIndex) / cycleDuration
            println("Repetition found! rockIndex=#$rockIndex, height=$height, last=$last. Skipping to ${remainingFullCycles * cycleDuration}")
            rockIndex += remainingFullCycles * cycleDuration // Wheeeee...
            skippedHeight = remainingFullCycles * cycleHeight
            watermarkCache.clear()
        } else {
            watermarkCache[key] = Pair(rockIndex, height)
        }

        // Note: the repetition is found so quickly that this optimization was not necessary
        // // Pruning the rocks list to speed collision detection
        // val minWatermark = heights.min()
        // rocks.removeIf { it.y < minWatermark }
    }

    println("Height of tower: ${skippedHeight + rocks.height()}")
}

private fun List<Rock>.height() = this.maxOfOrNull { it.y } ?: 0L
private fun List<Rock>.watermark(): LongArray {
    val watermark = longArrayOf(0, 0, 0, 0, 0, 0, 0)
    for (r in this) {
        val t = r.type
        for (i in 0 until t.height) {
            for (j in 0 until t.width) {
                if (!t.pattern[i][j]) continue
                watermark[r.x + j] = max(watermark[r.x + j], r.y - i)
            }
        }
    }
    return watermark
}

private fun LongArray.normalize(): IntArray {
    val minHeight = this.min()
    return this.map { (it - minHeight).toInt() }.toIntArray()
}

private data class Rock(var x: Int, var y: Long, val type: RockType) {

    fun tryPush(jet: Int, rocks: List<Rock>) {
        val newX = x + jet
        if (newX < 0) return // Would collide with left wall
        else if (newX + type.width > 7) return // Would collide with right wall
        else {
            for (rock in rocks) {
                if (rock != this && rock.overlaps(newX, y, type)) return // Would collide with another rock
            }
            x = newX // Jet pushed the rock successfully
        }
    }

    fun tryFall(rocks: List<Rock>): Boolean {
        val newY = y - 1
        if (newY - type.height < 0) return false // Fell to the floor
        for (rock in rocks) {
            if (rock != this && rock.overlaps(x, newY, type)) return false // Fell on a rock
        }
        y = newY
        return true
    }

    private fun overlaps(x: Int, y: Long, t: RockType): Boolean {
        if (x + t.width < x // To the left
            || x > this.x + type.width // To the right
            || y - t.height > y // Above us
            || y < this.y - type.height // Below us
        ) return false // Bounding boxes don't overlap
        for (i in 0 until t.height) {
            for (j in 0 until t.width) {
                if (!t.pattern[i][j]) continue // The pattern is empty here - can't collide
                val ox = x + j
                val oy = y - i
                for (k in 0 until type.height) {
                    for (l in 0 until type.width) {
                        if (!type.pattern[k][l]) continue // The pattern is empty here - can't collide
                        if (this.x + l == ox && this.y - k == oy) return true
                    }
                }
            }
        }
        return false
    }
}

private enum class RockType(val pattern: Array<BooleanArray>) {
    HORIZONTAL(
        intArrayOf(1, 1, 1, 1)
    ),
    CROSS(
        intArrayOf(0, 1, 0),
        intArrayOf(1, 1, 1),
        intArrayOf(0, 1, 0),
    ),
    CORNER(
        intArrayOf(0, 0, 1),
        intArrayOf(0, 0, 1),
        intArrayOf(1, 1, 1),
    ),
    VERTICAL(
        intArrayOf(1),
        intArrayOf(1),
        intArrayOf(1),
        intArrayOf(1),
    ),
    SPHERICAL(
        intArrayOf(1, 1),
        intArrayOf(1, 1),
    );

    val width = pattern[0].size
    val height = pattern.size

    constructor(vararg intPattern: IntArray) : this(intPattern.map { line -> line.map { it == 1 }.toBooleanArray() }.toTypedArray())
}

private data class WatermarkKey(val rockType: RockType, val nextJet: Int, val watermark: IntArray) {
    override fun equals(other: Any?): Boolean {
        other as WatermarkKey
        return (rockType == other.rockType)
                && (nextJet == other.nextJet)
                && (watermark.contentEquals(other.watermark))
    }

    override fun hashCode(): Int {
        var result = rockType.hashCode()
        result = 31 * result + nextJet
        result = 31 * result + watermark.contentHashCode()
        return result
    }
}