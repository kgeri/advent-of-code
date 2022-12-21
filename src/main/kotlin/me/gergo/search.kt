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

fun <N> dijkstra(
    source: N,
    nodes: Iterable<N>,
    neighborFn: (N) -> Sequence<N>,
    distanceFn: (N, N) -> Double
): DijkstraResult<N> {
    val dist = mutableMapOf<N, Double>()
    val prev = mutableMapOf<N, N>()
    val q = mutableSetOf<N>()

    for (v in nodes) {
        dist[v] = Double.POSITIVE_INFINITY
        q.add(v)
    }
    dist[source] = 0.0

    while (q.isNotEmpty()) {
        val u = q.minBy { dist[it]!! }
        q.remove(u)

        for (v in neighborFn(u).filter { q.contains(it) }) {
            val alt = dist[u]!! + distanceFn(u, v)
            if (alt < dist[v]!!) {
                dist[v] = alt
                prev[v] = u
            }
        }
    }
    return DijkstraResult(source, dist, prev)
}

data class DijkstraResult<T>(val source: T, val dist: Map<T, Double>, val prev: Map<T, T>) {

    fun shortestPath(target: T): List<T> {
        val s = mutableListOf<T>()
        var u: T? = target
        while (u != source && u != null) {
            s.add(0, u)
            u = prev[u]
        }
        return s
    }
}
