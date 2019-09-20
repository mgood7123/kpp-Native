@file:Suppress("unused")

package sample.kotlin

import preprocessor.core.Parser
import preprocessor.utils.`class`.extensions.lastIndex
import preprocessor.utils.`class`.extensions.toStack
//import preprocessor.utils.core.algorithms.BinaryTree
import preprocessor.utils.core.algorithms.Tree

class Kotlin(contents: String) {
    val parseStream = Parser("AABBCCDEE") { it.toStack() }
    val X = Parser("1211") { it.toStack() }

    inner class Lexer {
        val A1 = X.IsSequenceOnce("1")
        val B1 = X.IsSequenceOnce("2")
//        val lists = X.Group(A1 and B1) and X.Group(X.Group(A1 and A1) or B1) // depth 2, 2, 1
//        val lists = X.Group(X.Group(X.Group(X.Group(A1 and X.Group(B1 and B1)) or A1) or B1) and B1) and A1 // depth 5 4 3 2 1
//        val lists = A1 and X.Group(B1) // depth 0, 1
//        val lists = X.Group(A1 and X.Group(B1)) // depth 1, 2
//        val lists = A1 and X.Group(A1 and X.Group(B1)) // depth 0, 1, 2
//        val lists = A1 and X.Group(B1) // depth 0, 1
//        val lists = X.Group(A1 and B1) // depth 1
//        val lists = X.Group(A1 and B1) and X.Group(A1 and B1) // depth 1, 1
//        val lists = X.Group(A1 and A1) or B1
        /*
groupCombination 0: And
group 0 (depth 1): And
it.once.value = 1 as IsSequenceOnce
it.once.value = 1 as IsSequenceOnce
groupCombination 1: Or
group 0 (depth 0): Or
it.once.value = 2 as IsSequenceOnce
         */
//        val lists = X.Group(X.Group(A1 and A1) or B1)
        /*
groupCombination 0: And
group 0 (depth 2): And
it.once.value = 1 as IsSequenceOnce
it.once.value = 1 as IsSequenceOnce
groupCombination 1: Or
group 0 (depth 1): Or
it.once.value = 2 as IsSequenceOnce
         */

//        val lists =
//            X.Group2(                        // group container
//                X.Group2(                    // group container
//                    X.Group2(                // group container
//                        A1 and      // A1 is Parser.IsSequenceOnce
//                                            // and between two Parser.IsSequenceOnce returns Parser.Combination
//                                            // normally this would be optimized to instead return Parser.IsSequenceOnce
//                                            // however it is easier to just promote it to a Parser.Combination
//                                B1          // B1 is Parser.IsSequenceOnce
//                    )                       // returns Parser.Group
//                )                           // returns Parser.GroupCombination, this holds multiple Parser.Group's
//            ) or                            // returns Parser.GroupCombination, this holds multiple Parser.Group's
//                    X.Group2(                // group container
//                        X.Group2(    // group container
//                            A1 and  // A1 is Parser.IsSequenceOnce
//                                            // and between two Parser.IsSequenceOnce returns Parser.Combination
//                                            // normally this would be optimized to instead return Parser.IsSequenceOnce
//                                            // however it is easier to just promote it to a Parser.Combination
//                                    A1      // A1 is Parser.IsSequenceOnce
//                        ) or                // or between a Parser.Group and anything else returns Parser.GroupCombination
//                                B1          // B1 is Parser.IsSequenceOnce
//                    )                       // returns Parser.GroupCombination, this holds multiple Parser.Group's

        /*
(((1 and 2))) or ((1 and 1) or 2)
(((1 && 2))) || ((1 && 1) || 2)
(((true && false))) || ((true && true) || false)
true || (true || false)
groupCombination 0: Or
group 0 (depth 4): And
it.once.value = 1 as IsSequenceOnce And
it.once.value = 2 as IsSequenceOnce And
groupCombination 1: Or
group 0 (depth 2): And
it.once.value = 1 as IsSequenceOnce And
it.once.value = 1 as IsSequenceOnce And
groupCombination 2: Or
group 0 (depth 1): Or
it.once.value = 2 as IsSequenceOnce Or
         */
//        val lists = X.Group(X.Group(X.Group(A1 and B1)) or X.Group(A1 and A1) or B1)
        /*
(((1 and 2)) or (1 and 1) or 2)
(((1 && 2)) || (1 && 1) || 2)
(((true && false)) || (true && true) || false)
true || true || false
groupCombination 0: Or
group 0 (depth 4): And
it.once.value = 1 as IsSequenceOnce And
it.once.value = 2 as IsSequenceOnce And
groupCombination 1: Or
group 0 (depth 3): And
it.once.value = 1 as IsSequenceOnce And
it.once.value = 1 as IsSequenceOnce And
groupCombination 2: Or
group 0 (depth 1): Or
it.once.value = 2 as IsSequenceOnce Or
         */
//        val lists = A1 or X.Group(A1 or B1)
        /*
         root
        /  |
      A1   A1
           |
           B1
        */
        /*
1 or (1 or 2)
1 || (1 || 2)
true || (true || false)
true || true
groupCombination 0: Or
group 0 (depth 0): Or
it.once.value = 1 as IsSequenceOnce Or
groupCombination 1: Or
group 0 (depth 1): Or
it.once.value = 1 as IsSequenceOnce Or
it.once.value = 2 as IsSequenceOnce Or
         */
//        val lists = A1 or A1 and B1
        /*
1 or 1 or 2
1 || 1 || 2
true || true || false
it.once.value = 1 as IsSequenceOnce Or
it.once.value = 1 as IsSequenceOnce Or
it.once.value = 2 as IsSequenceOnce Or
         */
        // (1 and 2) and ((1 and 1) or 2)
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
            val lists = X.Group(X.Group(X.Group(A1 and X.Group(B1)))) or X.Group(X.Group(A1 and A1) or B1)
            println(lists.AST.prettyPrint { it?.list?.value })
//            println(parseStream.toStringAsArray())
//            println(ABCD.peek())
//            println(parseStream.toStringAsArray())
//            println(ABCD.pop())
//            println(parseStream.toStringAsArray())

        }
    }

    inner class Parser {
        fun parse() {
        }
    }
}