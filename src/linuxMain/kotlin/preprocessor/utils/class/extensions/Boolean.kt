@file:Suppress("unused")

package preprocessor.utils.`class`.extensions

fun <T, R> T.ifTrue(ii:Boolean, code: (ii:T) -> R): R = if (ii) code(this) else this as R

fun <T> T.ifTrueReturn(ii:Boolean, code: (T) -> Unit): Boolean {
    if (ii) code(this)
    return ii
}

fun Boolean.ifTrueReturn(code: () -> Unit): Boolean {
    if (this) code()
    return this
}

fun <T> T.ifFalseReturn(ii:Boolean, code: (T) -> Unit): Boolean {
    if (!ii) code(this)
    return ii
}

fun Boolean.ifFalseReturn(code: () -> Unit): Boolean {
    if (!this) code()
    return this
}

fun <T> T.ifUnconditionalReturn(ii:Boolean, code: (T) -> Unit): Boolean {
    code(this)
    return ii
}

fun <T, R> T.executeIfTrue(ii:Boolean, code: (ii:T) -> R): R = ifTrue(ii) { code(this) }
fun <T> T.executeIfTrueAndReturn(ii:Boolean, code: (T) -> Unit): Boolean = ifTrueReturn(ii, code)
fun Boolean.executeIfTrueAndReturn(code: () -> Unit): Boolean = ifTrueReturn(code)
fun <T> T.executeIfFalseAndReturn(ii:Boolean, code: (T) -> Unit): Boolean = ifFalseReturn(ii, code)
fun <T> T.executeUnconditionallyAndReturn(ii:Boolean, code: (T) -> Unit): Boolean = ifUnconditionalReturn(ii, code)

private fun main() {
    var x = "abc"
    println("x = $x") // x = abc
    val y = x.ifTrue(x.startsWith('a')) {
        it.length
    }
    println("y = $y") // y = 3
    println("x = $x") // x = abc
    val yx = x.ifTrueReturn(x.startsWith('a')) {
        x = it.drop(1) // i want "it" to modify "x" itself
    }
    println("yx = $yx") // yx = true
    println("x = $x") // x = abc // should be "bc"
}
