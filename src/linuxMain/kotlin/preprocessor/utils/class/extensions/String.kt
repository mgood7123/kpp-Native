@file:Suppress("unused")

// find         : \{\n    return if \(this \=\= null\) null\n    else this\.(.*)\n\}
// replace with : = this\?\.$1


package preprocessor.utils.`class`.extensions

import preprocessor.core.Parser
import preprocessor.utils.core.algorithms.LinkedList
import preprocessor.utils.core.algorithms.Stack
import kotlin.math.max

/**
 * splits the **string** into **tokens** delimited by a sequence of [delimiters]
 *
 * includes the [delimiters] in the output if [returnDelimiters] is **true**
 * @param delimiters a sequence of **delimiters**, given in the form `"delim1", "delim2", "delim3"` and so on
 * @param returnDelimiters if **true**, the sequence of [delimiters] are included as part of the output, otherwise they are omitted
 * @see split
 * @see tokenize
 */
fun String.tokenizeVararg(vararg delimiters: String, returnDelimiters: Boolean = false): List<String> {
    var res = listOf(this)
    delimiters.forEach { str ->
        res = res.flatMap {
            it.split(str).flatMap {
                listOf(it, str)
            }.dropLast(1).filterNot {
                it.isEmpty()
            }.filterNot {
                !returnDelimiters && it == str
            }
        }
    }
    return res
}

/**
 * splits the **string** into **tokens** delimited by a sequence of [delimiters]
 *
 * includes the [delimiters] in the output if [returnDelimiters] is **true**
 * @param delimiters a sequence of **delimiters**, given in the form `"delim1", "delim2", "delim3"` and so on
 * @param returnDelimiters if **true**, the sequence of [delimiters] are included as part of the output, otherwise they are ommitted
 * @see split
 * @see tokenize
 */
fun String?.tokenizeVararg(vararg delimiters: String, returnDelimiters: Boolean = false): List<String>? =
    this?.tokenizeVararg(delimiters = *delimiters, returnDelimiters = returnDelimiters)

/**
 * splits the **string** into **tokens** delimited by a sequence of [delimiters]
 *
 * includes the [delimiters] in the output if [returnDelimiters] is **true**
 *
 * the [delimiters] are specified on a per-character basis
 *
 * meaning that **tokenize("abc")** is equivalent to **[tokenizeVararg]("a", "b", "c")**
 *
 * @param delimiters a sequence of **delimiters**
 * @param returnDelimiters if **true**, the sequence of [delimiters] are included as part of the output, otherwise they are ommitted
 * @see split
 * @see tokenizeVararg
 */
fun String.tokenize(delimiters: String, returnDelimiters: Boolean = false): List<String> {
    var res = listOf(this)
    val d = delimiters.toStack
    d.forEach { str ->
        res = res.flatMap {
            it.split(str!!).flatMap {
                listOf(it, str)
            }.dropLast(1).filterNot {
                it.isEmpty()
            }.filterNot {
                !returnDelimiters && it == str
            }
        }
    }
    return res
}

/**
 * splits the **string** into **tokens** delimited by a sequence of [delimiters]
 *
 * includes the [delimiters] in the output if [returnDelimiters] is **true**
 *
 * the [delimiters] are specified on a per-character basis
 *
 * meaning that **tokenize("abc")** is equivilant to **[tokenizeVararg]("a", "b", "c")**
 *
 * @param delimiters a sequence of **delimiters**
 * @param returnDelimiters if **true**, the sequence of [delimiters] are included as part of the output, otherwise they are ommitted
 * @see split
 * @see tokenizeVararg
 */
fun String?.tokenize(delimiters: String, returnDelimiters: Boolean = false): List<String>? =
    this?.tokenize(delimiters, returnDelimiters)

/**
 * collapses the string based on the specified **token**
 *
 * if one or more of **token** is subsequently present in the current string, it is replaced with a single **token**
 *
 * for example:
 *
 * **"aaaa".collapse("a")** will be replaced by **"a"**
 *
 * **"aaaabbbbaaaa".collapse("a")** will be replaced by **"abbbba"**
 *
 * **"aaaabbbbaaaa".collapse("b")** will be replaced by **"aaaabaaaa"**
 *
 * **"aaaabbbbaaaa".collapse("a").collapse("b")** will be replaced by **"aba"**
 *
 * **"a    b".collapse(" ", "|rabbit|")** will be replaced by **"a|rabbit|b"**
 * @see expand
 */
fun String.collapse(token: String): String {
    val str = StringBuilder()
    val parser = Parser(this) { it.toStack }
    val t = parser.IsSequenceOneOrMany(token)
    while (parser.peek() != null) {
        if (t.peek()) {
            t.pop()
            str.append(token)
        } else str.append(parser.pop()!!)
    }
    return str.toString()
}

/**
 * collapses the string based on the specified **token**
 *
 * if one or more of **token** is subsequently present in the current string, it is replaced with a single **token**
 *
 * for example:
 *
 * **"aaaa".collapse("a")** will be replaced by **"a"**
 *
 * **"aaaabbbbaaaa".collapse("a")** will be replaced by **"abbbba"**
 *
 * **"aaaabbbbaaaa".collapse("b")** will be replaced by **"aaaabaaaa"**
 *
 * **"aaaabbbbaaaa".collapse("a").collapse("b")** will be replaced by **"aba"**
 *
 * **"a    b".collapse(" ", "|rabbit|")** will be replaced by **"a|rabbit|b"**
 * @see expand
 */
fun String?.collapse(token: String): String? = this?.collapse(token)

/**
 * collapses the string based on the specified **token**, replacing **token** with **replaceWith**
 *
 * if one or more of **token** is subsequently present in the current string, it is replaced with a single **replaceWith**
 *
 * for example:
 *
 * **"aaaa".collapse("a", "Carrot")** will be replaced by **"Carrot"**
 *
 * **"aaaabbbbaaaa".collapse("a", "Carrot")** will be replaced by **"CarrotbbbbCarrot"**
 *
 * **"aaaabbbbaaaa".collapse("b", "Carrot")** will be replaced by **"aaaaCarrotaaaa"**
 *
 * **"aaaabbbbaaaa".collapse("a", "Carrot").collapse("b", "Carrot")** will be replaced by **"CarrotCarrotCarrot"**
 *
 * **"aaaabbbbaaaa".collapse("b", "Carrot").collapse("a", "Carrot")** will be replaced by **"CarrotCCarrotrrotCarrot"**,
 *
 *
 *
 *
 * note that in the case of **"aaaabbbbaaaa".collapse("b", "Carrot").collapse("a", "Carrot")** you would normally expect **"CarrotCarrotCarrot"**, **however** due to call ordering this is not the
 * case: when **b** is first collapsed, the string results in **"aaaaCarrotaaaa"**, then when **a** is collapsed
 * it is collapsing on the string **"aaaaCarrotaaaa"** and not **"aaaaabbbbaaaa"**
 *
 * @see expand
 */
fun String.collapse(token: String, replaceWith: String): String {
    val str = StringBuilder()
    val parser = Parser(this) { it.toStack }
    val t = parser.IsSequenceOneOrMany(token)
    while (parser.peek() != null) {
        if (t.peek()) {
            t.pop()
            str.append(replaceWith)
        } else str.append(parser.pop()!!)
    }
    return str.toString()
}

/**
 * collapses the string based on the specified **token**, replacing **token** with **replaceWith**
 *
 * if one or more of **token** is subsequently present in the current string, it is replaced with a single **replaceWith**
 *
 * for example:
 *
 * **"aaaa".collapse("a", "Carrot")** will be replaced by **"Carrot"**
 *
 * **"aaaabbbbaaaa".collapse("a", "Carrot")** will be replaced by **"CarrotbbbbCarrot"**
 *
 * **"aaaabbbbaaaa".collapse("b", "Carrot")** will be replaced by **"aaaaCarrotaaaa"**
 *
 * **"aaaabbbbaaaa".collapse("a", "Carrot").collapse("b", "Carrot")** will be replaced by **"CarrotCarrotCarrot"**
 *
 * **"aaaabbbbaaaa".collapse("b", "Carrot").collapse("a", "Carrot")** will be replaced by **"CarrotCCarrotrrotCarrot"**,
 *
 *
 *
 *
 * note that in the case of **"aaaabbbbaaaa".collapse("b", "Carrot").collapse("a", "Carrot")** you would normally expect **"CarrotCarrotCarrot"**, **however** due to call ordering this is not the
 * case: when **b** is first collapsed, the string results in **"aaaaCarrotaaaa"**, then when **a** is collapsed
 * it is collapsing on the string **"aaaaCarrotaaaa"** and not **"aaaaabbbbaaaa"**
 *
 * @see expand
 */
fun String?.collapse(token: String, replaceWith: String): String? = this?.collapse(token, replaceWith)

/**
 * expands the string based on the specified **token**, replacing **token** with **to**
 *
 * if **token** is present in the current string, it is replaced with a **to**
 *
 * for example:
 *
 * **"aaaa".expand("a", "Carrot")** will be replaced by **"CarrotCarrotCarrotCarrot"**
 *
 * **"a    b".expand(" ", "|rabbit")** will be replaced by **"a|rabbit|rabbit|rabbit|rabbitb"**
 *
 * @see collapse
 */
fun String.expand(token: String, to: String): String {
    val str = StringBuilder()
    val parser = Parser(this) { it.toStack }
    val t = parser.IsSequenceOnce(token)
    while (parser.peek() != null) {
        if (t.peek()) {
            t.pop()
            str.append(to)
        } else str.append(parser.pop()!!)
    }
    return str.toString()
}

/**
 * expands the string based on the specified **token**, replacing **token** with **to**
 *
 * if **token** is present in the current string, it is replaced with a **to**
 *
 * for example:
 *
 * **"aaaa".expand("a", "Carrot")** will be replaced by **"CarrotCarrotCarrotCarrot"**
 *
 * **"a    b".expand(" ", "|rabbit")** will be replaced by **"a|rabbit|rabbit|rabbit|rabbitb"**
 *
 * @see collapse
 */
fun String?.expand(token: String, to: String): String? = this?.expand(token, to)

/**
 * expands the string based on the specified **token**,
 *
 * if **token** is present in the current string, it is replaced with **to**
 * except on the last occurrence in which case is replaced with **last**
 *
 * for example:
 *
 * **"aaaa".expand("a", "Carrot", "Stew")** will be replaced by **"CarrotCarrotCarrotStew"**
 *
 * **"a    b".expand(" ", "|rabbit", "|")** will be replaced by **"a|rabbit|rabbit|rabbit|b"**
 *
 * @see collapse
 */
fun String.expand(token: String, to: String, last: String): String {
    val str = StringBuilder()
    val parser = Parser(this) { it.toStack }
    val t = parser.IsSequenceOnce(token)
    while (parser.peek() != null) {
        if (t.peek()) {
            t.pop()
            str.append(if (!t.peek()) last else to)
        } else str.append(parser.pop()!!)
    }
    return str.toString()
}

/**
 * expands the string based on the specified **token**,
 *
 * if **token** is present in the current string, it is replaced with **to**
 * except on the last occurrence in which case is replaced with **last**
 *
 * for example:
 *
 * **"aaaa".expand("a", "Carrot", "Stew")** will be replaced by **"CarrotCarrotCarrotStew"**
 *
 * **"a    b".expand(" ", "|rabbit", "|")** will be replaced by **"a|rabbit|rabbit|rabbit|b"**
 *
 * @see collapse
 */
fun String?.expand(token: String, to: String, last: String): String? = this?.expand(token, to, last)

/**
 * converts a [String] into a [LinkedList]
 * @see LinkedList.toStringConcat
 * @return the resulting conversion
 */
val String.toLinkedList
    get() = LinkedList(
        { t, ACTION -> t.forEach { ACTION(it) } },  // arrayIterator
        { (it as Char).toString() },                 // ACTION
        this
    )

/**
 * converts a [String] into a [Stack]
 * @see Stack.toStringConcat
 * @return the resulting conversion
 */
val String.toStack
    get() = Stack(
        { t, ACTION -> t.forEach { ACTION(it) } },  // arrayIterator
        { (it as Char).toString() },                 // ACTION
        this
    )

/**
 * converts a [String] into a [LinkedList]
 * @see LinkedList.toStringConcat
 * @return the resulting conversion
 */
val String?.toLinkedList get() = this?.toLinkedList

/**
 * converts a [String] into a [Stack]
 * @see Stack.toStringConcat
 * @return the resulting conversion
 */
val String?.toStack get() = this?.toStack

/**
 * converts a [String] into a [ByteArray]
 * @return the resulting conversion
 * @see fileToByteArray
 */
fun String.toByteArray(): ByteArray {
    val b = ByteArray(this.length)
    val fi = this.iterator()
    var i = 0
    while (fi.hasNext()) {
        val B = fi.next().toByte()
        b.set(i, B)
        i++
    }
    return b
}

/**
 * converts a [String] into a [ByteArray]
 * @return the resulting conversion or null
 * @see fileToByteArray
 */
fun String?.toByteArray(): ByteArray? = this?.toByteArray()

fun String.toStringBuilder(): StringBuilder {
    return StringBuilder(this)
}

fun String?.toStringBuilder(): StringBuilder? = this?.toStringBuilder()

fun String.toStringBuilder(capacity: Int): StringBuilder {
    return StringBuilder(capacity).append(this)
}

fun String?.toStringBuilder(capacity: Int): StringBuilder? = this?.toStringBuilder(capacity)

fun String.padExtendEnd(to: Int, str: String, rotateString: Boolean, trim: Boolean) = when (trim) {
    true -> when {
        to == 0 || to - this.length isEqualTo 0 -> this
        to - this.length isGreaterThan 0 -> {
            val build = this.toStringBuilder(max(this.length, to));
            val m = str.lastIndex
            var i = 0
            while (build.length isLessThan to) {
                build.append(if (rotateString) str[i] else str)
                if (i isEqualTo m) i = 0 else i++
            }
            build.toString()
        }
        else -> this.padShrinkEnd(to, str, rotateString, trim)
    }
    false -> {
        val build = this.toStringBuilder(max(this.length, to));
        val m = str.lastIndex
        val n = this.length
        var i = 0
        while (build.length isLessThan n + (to * str.length)) {
            build.append(if (rotateString) str[i] else str)
            if (i isEqualTo m) i = 0 else i++
        }
        build.toString()
    }
}

fun String.padExtendEnd(to: Int, str: String, rotateString: Boolean): String =
    this.padExtendEnd(to, str, rotateString, true)

fun String.padExtendEnd(to: Int, str: String): String = this.padExtendEnd(to, str, true)

fun String.padExtendEnd(to: Int, char: Char, rotateString: Boolean, trim: Boolean): String =
    this.padExtendEnd(to, char.toString(), rotateString, trim)

fun String.padExtendEnd(to: Int, char: Char, rotateString: Boolean): String =
    this.padExtendEnd(to, char.toString(), rotateString)

fun String.padExtendEnd(to: Int, char: Char): String = this.padExtendEnd(to, char.toString())

fun String.padExtendEnd(to: Int, rotateString: Boolean, trim: Boolean): String =
    this.padExtendEnd(to, this[0], rotateString, trim)

fun String.padExtendEnd(to: Int, rotateString: Boolean): String = this.padExtendEnd(to, this[0], rotateString)

fun String.padExtendEnd(to: Int): String = this.padExtendEnd(to, this[0])

fun String?.padExtendEnd(to: Int, str: String, rotateString: Boolean): String? =
    this?.padExtendEnd(to, str, rotateString)

fun String?.padExtendEnd(to: Int, str: String): String? = this?.padExtendEnd(to, str)

fun String?.padExtendEnd(to: Int, char: Char, rotateString: Boolean, trim: Boolean): String? =
    this?.padExtendEnd(to, char.toString(), rotateString, trim)

fun String?.padExtendEnd(to: Int, char: Char, rotateString: Boolean): String? =
    this?.padExtendEnd(to, char.toString(), rotateString)

fun String?.padExtendEnd(to: Int, char: Char): String? = this?.padExtendEnd(to, char.toString())

fun String?.padExtendEnd(to: Int, rotateString: Boolean, trim: Boolean): String? =
    this?.padExtendEnd(to, this[0], rotateString, trim)

fun String?.padExtendEnd(to: Int, rotateString: Boolean): String? = this?.padExtendEnd(to, this[0], rotateString)

fun String?.padExtendEnd(to: Int): String? = this?.padExtendEnd(to, this[0])

fun String.padExtendStart(to: Int, str: String, rotateString: Boolean, trim: Boolean): String = when (trim) {
    true -> when {
        to == 0 || to - this.length isEqualTo 0 -> this
        to - this.length isGreaterThan 0 -> {
            val build = this.reversed().toStringBuilder()
            val m = str.lastIndex
            var i = 0
            while (build.length isLessThan to) {
                build.append(if (rotateString) str[i] else str)
                if (i isEqualTo m) i = 0 else i++
            }
            build.reverse().toString()
        }
        else -> this.padShrinkStart(to, str, rotateString, trim)
    }
    false -> {
        val build = this.reversed().toStringBuilder()
        val m = str.lastIndex
        val n = this.length
        var i = 0
        while (build.length isLessThan n + (to * str.length)) {
            build.append(if (rotateString) str[i] else str)
            if (i isEqualTo m) i = 0 else i++
        }
        build.reverse().toString()
    }
}

fun String.padExtendStart(to: Int, str: String, rotateString: Boolean): String =
    this.padExtendStart(to, str, rotateString, true)

fun String.padExtendStart(to: Int, str: String): String = this.padExtendStart(to, str, true)

fun String.padExtendStart(to: Int, char: Char, rotateString: Boolean, trim: Boolean): String =
    this.padExtendStart(to, char.toString(), rotateString, trim)

fun String.padExtendStart(to: Int, char: Char, rotateString: Boolean): String =
    this.padExtendStart(to, char.toString(), rotateString)

fun String.padExtendStart(to: Int, char: Char): String = this.padExtendStart(to, char.toString())

fun String.padExtendStart(to: Int, rotateString: Boolean, trim: Boolean): String =
    this.padExtendStart(to, this[0], rotateString, trim)

fun String.padExtendStart(to: Int, rotateString: Boolean): String = this.padExtendStart(to, this[0], rotateString)

fun String.padExtendStart(to: Int): String = this.padExtendStart(to, this[0])

fun String?.padExtendStart(to: Int, str: String, rotateString: Boolean): String? =
    this?.padExtendStart(to, str, rotateString)

fun String?.padExtendStart(to: Int, str: String): String? = this?.padExtendStart(to, str)

fun String?.padExtendStart(to: Int, char: Char, rotateString: Boolean, trim: Boolean): String? =
    this?.padExtendStart(to, char.toString(), rotateString, trim)

fun String?.padExtendStart(to: Int, char: Char, rotateString: Boolean): String? =
    this?.padExtendStart(to, char.toString(), rotateString)

fun String?.padExtendStart(to: Int, char: Char): String? = this?.padExtendStart(to, char.toString())

fun String?.padExtendStart(to: Int, rotateString: Boolean, trim: Boolean): String? =
    this?.padExtendStart(to, this[0], rotateString, trim)

fun String?.padExtendStart(to: Int, rotateString: Boolean): String? = this?.padExtendStart(to, this[0], rotateString)

fun String?.padExtendStart(to: Int): String? = this?.padExtendStart(to, this[0])

fun String.padShrinkEnd(to: Int, str: String, rotateString: Boolean, trim: Boolean): String = when {
    to == 0 || to - this.length isEqualTo 0 -> this
    to - this.length isGreaterThan 0 -> {
        val thisLength = this.length
        val build = this.take(to).toStringBuilder()
        val m = str.lastIndex
        var i = 0
        while (build.length isLessThan thisLength) {
            build.append(if (rotateString) str[i] else str)
            if (i isEqualTo m) i = 0 else i++
        }
        build.toString()
    }
    else -> this.padExtendEnd(to, str, rotateString, trim)
}

fun String.padShrinkEnd(to: Int, str: String, rotateString: Boolean): String =
    this.padShrinkEnd(to, str, rotateString, true)

fun String.padShrinkEnd(to: Int, str: String): String = this.padShrinkEnd(to, str, true)

fun String.padShrinkEnd(to: Int, char: Char, rotateString: Boolean, trim: Boolean): String =
    this.padShrinkEnd(to, char.toString(), rotateString, trim)

fun String.padShrinkEnd(to: Int, char: Char, rotateString: Boolean): String =
    this.padShrinkEnd(to, char.toString(), rotateString)

fun String.padShrinkEnd(to: Int, char: Char): String = this.padShrinkEnd(to, char.toString())

fun String.padShrinkEnd(to: Int, rotateString: Boolean, trim: Boolean): String =
    this.padShrinkEnd(to, this[0], rotateString, trim)

fun String.padShrinkEnd(to: Int, rotateString: Boolean): String = this.padShrinkEnd(to, this[0], rotateString)

fun String.padShrinkEnd(to: Int): String = this.padShrinkEnd(to, this[0])

fun String?.padShrinkEnd(to: Int, str: String, rotateString: Boolean): String? =
    this?.padShrinkEnd(to, str, rotateString)

fun String?.padShrinkEnd(to: Int, str: String): String? = this?.padShrinkEnd(to, str)

fun String?.padShrinkEnd(to: Int, char: Char, rotateString: Boolean, trim: Boolean): String? =
    this?.padShrinkEnd(to, char.toString(), rotateString, trim)

fun String?.padShrinkEnd(to: Int, char: Char, rotateString: Boolean): String? =
    this?.padShrinkEnd(to, char.toString(), rotateString)

fun String?.padShrinkEnd(to: Int, char: Char): String? = this?.padShrinkEnd(to, char.toString())

fun String?.padShrinkEnd(to: Int, rotateString: Boolean, trim: Boolean): String? =
    this?.padShrinkEnd(to, this[0], rotateString, trim)

fun String?.padShrinkEnd(to: Int, rotateString: Boolean): String? = this?.padShrinkEnd(to, this[0], rotateString)

fun String?.padShrinkEnd(to: Int): String? = this?.padShrinkEnd(to, this[0])

fun String.padShrinkStart(to: Int, str: String, rotateString: Boolean, trim: Boolean): String = when {
    to == 0 || to - this.length isEqualTo 0 -> this
    this.length - to isGreaterThan 0 -> {
        val thisLength = this.length
        val build = this.reversed().take(to).toStringBuilder()
        val m = str.lastIndex
        var i = 0
        while (build.length isLessThan thisLength) {
            build.append(if (rotateString) str[i] else str)
            if (i isEqualTo m) i = 0 else i++
        }
        build.reverse().toString()
    }
    else -> this.padExtendStart(to, str, rotateString, trim)
}

fun String.padShrinkStart(to: Int, str: String, rotateString: Boolean): String =
    this.padShrinkStart(to, str, rotateString, true)

fun String.padShrinkStart(to: Int, str: String): String = this.padShrinkStart(to, str, true)

fun String.padShrinkStart(to: Int, char: Char, rotateString: Boolean, trim: Boolean): String =
    this.padShrinkStart(to, char.toString(), rotateString, trim)

fun String.padShrinkStart(to: Int, char: Char, rotateString: Boolean): String =
    this.padShrinkStart(to, char.toString(), rotateString)

fun String.padShrinkStart(to: Int, char: Char): String = this.padShrinkStart(to, char.toString())

fun String.padShrinkStart(to: Int, rotateString: Boolean, trim: Boolean): String =
    this.padShrinkStart(to, this[0], rotateString, trim)

fun String.padShrinkStart(to: Int, rotateString: Boolean): String = this.padShrinkStart(to, this[0], rotateString)

fun String.padShrinkStart(to: Int): String = this.padShrinkStart(to, this[0])

fun String?.padShrinkStart(to: Int, str: String, rotateString: Boolean): String? =
    this?.padShrinkStart(to, str, rotateString)

fun String?.padShrinkStart(to: Int, str: String): String? = this?.padShrinkStart(to, str)

fun String?.padShrinkStart(to: Int, char: Char, rotateString: Boolean, trim: Boolean): String? =
    this?.padShrinkStart(to, char.toString(), rotateString, trim)

fun String?.padShrinkStart(to: Int, char: Char, rotateString: Boolean): String? =
    this?.padShrinkStart(to, char.toString(), rotateString)

fun String?.padShrinkStart(to: Int, char: Char): String? = this?.padShrinkStart(to, char.toString())

fun String?.padShrinkStart(to: Int, rotateString: Boolean, trim: Boolean): String? =
    this?.padShrinkStart(to, this[0], rotateString, trim)

fun String?.padShrinkStart(to: Int, rotateString: Boolean): String? = this?.padShrinkStart(to, this[0], rotateString)

fun String?.padShrinkStart(to: Int): String? = this?.padShrinkStart(to, this[0])

/**
 * this is overloadable
 *
 * @sample padOverload
 */
fun String.pad(depth: Int): String = this.padExtendStart(depth, "tabulator ", false, false)

/**
 * see [String.padCallback] for how to overload this function
 */
fun String.pad(): String = this.pad(1)

/**
 * see [String.padCallback] for how to overload this function
 */
fun String?.pad(depth: Int): String? = this?.pad(depth)

/**
 * see [String.padCallback] for how to overload this function
 */
fun String?.pad(): String? = this?.pad()

private fun padOverload() {
    var depth = 0
    while (depth != 5) {
        // overload pad callback
        fun String.padCallback(depth: Int): String = this.padExtendStart(
            depth,
            "    ",
            false,
            false
        )

        // redirect pad() to this instance of pad(depth)
        fun String.pad(): String = this.pad(depth)
        println("depth: $depth".pad())
        run {
            // update depth
            fun String.pad(depth: Int): String = this.padExtendStart(
                depth + 1,
                "    ",
                false,
                false
            )

            // redirect pad() to this instance of pad(depth)
            fun String.pad(): String = this.pad(depth)

            println("depth: $depth".pad())
        }
        depth += 1
    }
}