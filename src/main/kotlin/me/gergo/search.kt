package me.gergo

fun <N> aStar(
    start: N, goal: N,
    neighborFn: (N) -> Sequence<N>,
    heuristicFn: (N) -> Double,
    distanceFn: (N, N) -> Double
): List<N> {
    val openSet = mutableSetOf(start)
    val cameFrom = mutableMapOf<N, N>()

    val gScore = mutableMapOf<N, Double>()
    gScore[start] = 0.0

    val fScore = mutableMapOf<N, Double>()
    fScore[start] = heuristicFn(start)

    while (openSet.isNotEmpty()) {
        val current = openSet.minBy { fScore.getOrDefault(it, Double.POSITIVE_INFINITY) }
        if (current == goal) {
            // Path found, reconstructing
            val result = mutableListOf(current)
            var c: N? = current
            while (true) {
                c = cameFrom[c]
                if (c == null) break
                result.add(0, c)
            }
            return result
        }

        openSet.remove(current)
        for (neighbor in neighborFn(current)) {
            val tentativeGScore = gScore.getOrDefault(current, Double.POSITIVE_INFINITY) + distanceFn(current, neighbor)
            if (tentativeGScore < gScore.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) {
                cameFrom[neighbor] = current
                gScore[neighbor] = tentativeGScore
                fScore[neighbor] = tentativeGScore + heuristicFn(neighbor)
                openSet.add(neighbor)
            }
        }
    }
    return emptyList() // Path not found
}
