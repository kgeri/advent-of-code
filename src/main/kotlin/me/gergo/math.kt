package me.gergo

fun lcm(a: Int, b: Int) = a * (b / gcd(a, b))

fun gcd(a: Int, b: Int): Int {
    var a0 = a
    var b0 = b
    while (b0 > 0) {
        val temp = b0
        b0 = a0 % b0
        a0 = temp
    }
    return a0
}

fun <T> powerset(set: Set<T>): Set<Set<T>> {
    if (set.isEmpty()) return setOf(emptySet())
    val first = set.first()
    val subset = powerset(set.minus(first))
    return subset + subset.map { it + first }
}
