@file:Suppress("unused")

package sample.kotlin

import preprocessor.core.Parenthesis
import preprocessor.core.Parser
import preprocessor.utils.`class`.extensions.toStack
import preprocessor.utils.core.algorithms.Tree

class Kotlin(contents: String) {
    val parseStream = Parser("AABBCCDECDEEBF") { it.toStack() }

    inner class Lexer {
        init {
            // implementation of a parser combinator
            val A = parseStream.IsSequenceZeroOrMany("A")
            val B = parseStream.IsSequenceOneOrMany("B")
            val AB = A and B
            println(AB.AST.prettyPrint
            {
                val x = it!!.list!!.list[0]
                when (val z = x.get()) {
                    is preprocessor.core.Parser.IsSequenceOnce -> "${z.value} as IsSequenceOnce ${x.typeToString()}"
                    is preprocessor.core.Parser.IsSequenceOneOrMany -> "${z.value} as IsSequenceOneOrMany ${x.typeToString()}"
                    else -> "${(z as preprocessor.core.Parser.IsSequenceZeroOrMany).value} as IsSequenceZeroOrMany ${x.typeToString()}"
                }
            })
            val C = parseStream.IsSequenceOnce("C")
            val D = parseStream.IsSequenceOneOrMany("DE")
            val E = parseStream.IsSequenceOnce("E")
            val CD = C and D
            val CDEB = Parenthesis(Parenthesis(Parenthesis(CD) and E) or B)
            val CDEB2 = Parenthesis(Parenthesis(Parenthesis(CD) and E) or B)
            val ABCD = AB and C and CD and CDEB and CDEB and !CDEB and parseStream.IsSequenceOnce("F")
            println(ABCD.AST.prettyPrint {
                val x = it!!.list!!.list[0]
                when (val z = x.get()) {
                    is preprocessor.core.Parser.IsSequenceOnce -> "${z.value} as IsSequenceOnce ${x.typeToString()}"
                    is preprocessor.core.Parser.IsSequenceOneOrMany -> "${z.value} as IsSequenceOneOrMany ${x.typeToString()}"
                    else -> "${(z as preprocessor.core.Parser.IsSequenceZeroOrMany).value} as IsSequenceZeroOrMany ${x.typeToString()}"
                }
            })
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