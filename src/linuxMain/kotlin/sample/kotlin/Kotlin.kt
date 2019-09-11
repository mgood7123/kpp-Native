@file:Suppress("unused")

package sample.kotlin

import preprocessor.base.globalVariables
import preprocessor.core.Lexer
import preprocessor.core.Parser
import preprocessor.test.init
import preprocessor.utils.`class`.extensions.toByteArray
import preprocessor.utils.`class`.extensions.toStack
import preprocessor.utils.core.algorithms.Stack
import preprocessor.utils.extra.parse

class Kotlin(contents: String) {
    val parseStream = Parser("AABBCCDEE") { it.toStack() }

    inner class Lexer {
        val A = parseStream.IsSequenceZeroOrMany("A")
        val B = parseStream.IsSequenceOneOrMany("B")
        val C = parseStream.IsSequenceOnce("C")
        val D = parseStream.IsSequenceOneOrMany("DE")
        val AB = A and B
        val CD = C and D
        val ABCD = AB and C and CD

        init {
            ABCD.printList()
            println(parseStream.toStringAsArray())
            println(ABCD.toString())
            println(parseStream.toStringAsArray())
            println(ABCD.peek())
            println(parseStream.toStringAsArray())
            println(ABCD.pop())
            println(parseStream.toStringAsArray())

        }
    }

    inner class Parser {
        fun parse() {
        }
    }
}