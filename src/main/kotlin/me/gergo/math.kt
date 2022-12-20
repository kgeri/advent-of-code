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
