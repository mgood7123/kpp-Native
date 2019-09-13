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
    val X = Parser("1211") { it.toStack() }

    inner class Lexer {
        val A1 = X.IsSequenceOnce("1")
        val B1 = X.IsSequenceOnce("2")
        val lists = X.Group(A1 and B1) and X.Group(X.Group(A1 and A1) or B1)
        // ((A) and (B)) and (((A) and (A)) or (B))
        // (1 && 2) && ((1 && 1) || 2)
        // will match 1211
        // left 1211
        // list 1: 12 > match 12
        // left: 11
        // list2: 11||2 > match 11
        // left: null

        val A = parseStream.IsSequenceZeroOrMany("A")
        val B = parseStream.IsSequenceOneOrMany("B")
        val C = parseStream.IsSequenceOnce("C")
        val D = parseStream.IsSequenceOneOrMany("DE")
        val AB = A and B
        val CD = C and D
        val ABCD = AB and C and CD

        init {
            lists.printList()
            println(parseStream.toStringAsArray())
//            println(parseStream.toStringAsArray())
//            println(list1.peek())
//            println(parseStream.toStringAsArray())
//            println(list1.pop())
//            println(parseStream.toStringAsArray())

        }
    }

    inner class Parser {
        fun parse() {
        }
    }
}