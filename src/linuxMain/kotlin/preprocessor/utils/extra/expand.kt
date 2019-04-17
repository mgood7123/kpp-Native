package preprocessor.utils.extra

import preprocessor.base.globalVariables
import preprocessor.core.*
import preprocessor.utils.core.abort
import preprocessor.utils.core.algorithms.collapse
import preprocessor.utils.core.algorithms.toByteArray
import kotlin.collections.MutableList

data class occurrence(val name: MutableList<String> = mutableListOf(), val times: MutableList<Int> = mutableListOf())
/**
 * expands a line
 * @return the expanded line
 * @param depth the current depth of expansion
 * @param lex this is used for multi-line processing
 * @param tokenSequence the current [Parser]
 * @param macro the current [Macro]
 * @param ARG the current argument list in an expanding function
 * @param blacklist the current list of macro's which should not be expanded
 * @param s is true if stringization is occuring
 * @param c is true if concation is occuring
 */
fun expand(
    occurrence: occurrence = occurrence(),
    depth: Int = 0,
    lex: Lexer,
    tokenSequence: Parser,
    macro: MutableList<Macro>,
    macroUnexpanded: MutableList<Macro>? = null,
    ARG: MutableList<String>? = null,
    ARGUnexpanded: MutableList<Macro>? = null,
    blacklist: MutableList<String> = mutableListOf(),
    expanding: String? = null,
    expandingType: String? = null,
    originalExpanding: String? = null,
    originalExpandingType: String? = null,
    s: Boolean = false,
    c: Boolean = false
): String? {
    val dm = 15
    if (depth > dm) abort("depth exceeded $dm")
    preprocessor.base.globalVariables.depth = depth
    var stringize = s
    var concat = c
    if (preprocessor.base.globalVariables.debug) {
        println(preprocessor.base.globalVariables.depthAsString() + "PARAMETERS AT FUNCTION CALL START")
        println(
            preprocessor.base.globalVariables.depthAsString() + "expanding '${
            lex.currentLine?.replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())}'"
        )
        println(
            preprocessor.base.globalVariables.depthAsString() + "tokenSequence = ${tokenSequence.toStringAsArray()
                .replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())}"
        )
        println(
            preprocessor.base.globalVariables.depthAsString() + "ARG = ${ARG.toString()
                .replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())}"
        )
        println(preprocessor.base.globalVariables.depthAsString() + "blacklist = $blacklist")
        println(preprocessor.base.globalVariables.depthAsString() + "expanding    : $expanding")
        println(preprocessor.base.globalVariables.depthAsString() + "expandingType: $expandingType")
        println(preprocessor.base.globalVariables.depthAsString() + "originalExpanding: $originalExpanding")
        println(preprocessor.base.globalVariables.depthAsString() + "originalExpandingType: $originalExpandingType")
        println(preprocessor.base.globalVariables.depthAsString() + "macro = ")
        macroList(macro)
            println(preprocessor.base.globalVariables.depthAsString() + "macro unexpanded = ")
        if (macroUnexpanded != null) macroList(macroUnexpanded)
        else println(preprocessor.base.globalVariables.depthAsString() + "null")
    }
//    if (originalExpanding != null || originalExpandingType != null) abort()
    val expansion = StringBuilder()
    var iterations = 0
    val maxIterations = 100
    while (iterations <= maxIterations && tokenSequence.peek() != null) {
        val directive = tokenSequence.IsSequenceOnce(Macro().Directives().value)
        val defineDef = tokenSequence.IsSequenceOnce(Macro().Directives().Define().value)
        val abortDef = tokenSequence.IsSequenceOnce("abort")
        val ignoreDef = tokenSequence.IsSequenceOnce("ignore")


        val space: Parser.IsSequenceOneOrMany = tokenSequence.IsSequenceOneOrMany(" ")
        val newline: Parser.IsSequenceOnce = tokenSequence.IsSequenceOnce("\n")
        val comment = tokenSequence.IsSequenceOnce("//")
        val blockCommentStart = tokenSequence.IsSequenceOnce("/*")
        val blockCommentEnd = tokenSequence.IsSequenceOnce("*/")
        val comma = tokenSequence.IsSequenceOnce(",")
        val emptyParenthesis = tokenSequence.IsSequenceOnce("()")
        val leftParenthesis: Parser.IsSequenceOnce = tokenSequence.IsSequenceOnce("(")
        val rightParenthesis = tokenSequence.IsSequenceOnce(")")
        val leftBrace = tokenSequence.IsSequenceOnce("[")
        val rightBrace = tokenSequence.IsSequenceOnce("]")
        val leftBracket = tokenSequence.IsSequenceOnce("{")
        val rightBracket = tokenSequence.IsSequenceOnce("}")
        val doubleString = tokenSequence.IsSequenceOnce("\"")
        val singleString = tokenSequence.IsSequenceOnce("'")
        val backslash = tokenSequence.IsSequenceOnce("\\")
        if (comment.peek()) {
            if (preprocessor.base.globalVariables.debug) println(
                preprocessor.base.globalVariables.depthAsString() + "clearing comment token '${tokenSequence
                    .toString()
                    .replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())}'"
            )
            tokenSequence.clear()
        } else if (blockCommentStart.peek()) {
            var depthBlockComment = 0
            blockCommentStart.pop() // pop the first /*
            depthBlockComment++
            var iterations = 0
            val maxIterations = 1000
            while (iterations <= maxIterations) {
                if (tokenSequence.peek() == null) {
                    lex.lex()
                    // Line is longer than allowed by code style (> 120 columns)
                    if (lex.currentLine == null) abort(
                        preprocessor.base.globalVariables.depthAsString() +
                                "no more lines when expecting more lines, unterminated block comment"
                    )
                    tokenSequence.tokenList = parserPrep(lex.currentLine as String)
                }
                if (newline.peek()) newline.pop()
                else if (blockCommentStart.peek()) {
                    depthBlockComment++
                    blockCommentStart.pop()
                } else if (blockCommentEnd.peek()) {
                    depthBlockComment--
                    blockCommentEnd.pop()
                    if (depthBlockComment == 0) {
                        break
                    }
                } else tokenSequence.pop()
                iterations++
            }
            if (iterations > maxIterations) abort(
                preprocessor.base.globalVariables.depthAsString() + "iterations expired"
            )
        } else if (doubleString.peek()) {
            if (preprocessor.base.globalVariables.debug) println(
                preprocessor.base.globalVariables.depthAsString() +
                        "popping double string token '$doubleString'"
            )
            expansion.append(doubleString.toString())
            doubleString.pop() // pop the first "
            var iterations = 0
            val maxIterations = 1000
            while (iterations <= maxIterations) {
                if (tokenSequence.peek() == null) {
                    lex.lex()
                    // Line is longer than allowed by code style (> 120 columns)
                    if (lex.currentLine == null) abort(
                        preprocessor.base.globalVariables.depthAsString() +
                                "no more lines when expecting more lines, unterminated block comment"
                    )
                    tokenSequence.tokenList = parserPrep(lex.currentLine as String)
                }
                if (newline.peek()) {
                    if (preprocessor.base.globalVariables.debug) println(
                        preprocessor.base.globalVariables.depthAsString() + "popping newline token '${
                        newline
                            .toString()
                            .replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())
                        }'"
                    )
                    newline.pop()
                } else if (doubleString.peek()) {
                    if (preprocessor.base.globalVariables.debug) println(
                        preprocessor.base.globalVariables.depthAsString() +
                                "popping double string token '$doubleString'"
                    )
                    expansion.append(doubleString.toString())
                    doubleString.pop()
                    break
                } else if (backslash.peek()) {
                    if (preprocessor.base.globalVariables.debug) println(
                        preprocessor.base.globalVariables.depthAsString() +
                                "popping backslash token '$backslash'"
                    )
                    expansion.append(backslash.toString())
                    backslash.pop()
                    if (doubleString.peek()) {
                        if (preprocessor.base.globalVariables.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "popping double string token '$doubleString'"
                        )
                        expansion.append(doubleString.toString())
                        doubleString.pop()
                    } else {
                        if (preprocessor.base.globalVariables.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "unknown backslash sequence '${tokenSequence.peek()}'"
                        )
                        expansion.append(tokenSequence.pop()!!)
                    }
                } else {
                    val popped = tokenSequence.pop()
                    if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() + "popping normal token '$popped'")
                    expansion.append(popped)
                }
                iterations++
            }
            if (iterations > maxIterations) abort(
                preprocessor.base.globalVariables.depthAsString() + "iterations expired"
            )
        } else if (singleString.peek()) {
            if (preprocessor.base.globalVariables.debug) println(
                preprocessor.base.globalVariables.depthAsString() +
                        "popping single string token '$singleString'"
            )
            expansion.append(singleString.toString())
            singleString.pop() // pop the first "
            var iterations = 0
            val maxIterations = 1000
            while (iterations <= maxIterations) {
                if (tokenSequence.peek() == null) {
                    lex.lex()
                    // Line is longer than allowed by code style (> 120 columns)
                    if (lex.currentLine == null) abort(
                        preprocessor.base.globalVariables.depthAsString() +
                                "no more lines when expecting more lines, unterminated block comment"
                    )
                    tokenSequence.tokenList = parserPrep(lex.currentLine as String)
                }
                if (newline.peek()) {
                    if (preprocessor.base.globalVariables.debug) println(
                        preprocessor.base.globalVariables.depthAsString() + "popping newline token '${
                        newline
                            .toString()
                            .replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())
                        }'"
                    )
                    newline.pop()
                } else if (singleString.peek()) {
                    if (preprocessor.base.globalVariables.debug) println(
                        preprocessor.base.globalVariables.depthAsString() +
                                "popping single string token '$singleString'"
                    )
                    expansion.append(singleString.toString())
                    singleString.pop()
                    break
                } else if (backslash.peek()) {
                    if (preprocessor.base.globalVariables.debug) println(
                        preprocessor.base.globalVariables.depthAsString() +
                                "popping backslash token '$backslash'"
                    )
                    expansion.append(backslash.toString())
                    backslash.pop()
                    if (singleString.peek()) {
                        if (preprocessor.base.globalVariables.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "popping single string token '$singleString'"
                        )
                        expansion.append(singleString.toString())
                        singleString.pop()
                    } else {
                        if (preprocessor.base.globalVariables.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "unknown backslash sequence '${tokenSequence.peek()}'"
                        )
                        expansion.append(tokenSequence.pop()!!)
                    }
                } else {
                    val popped = tokenSequence.pop()
                    if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() + "popping normal token '$popped'")
                    expansion.append(popped)
                }
                iterations++
            }
            if (iterations > maxIterations) abort(
                preprocessor.base.globalVariables.depthAsString() + "iterations expired"
            )
        } else if (emptyParenthesis.peek()) {
            if (preprocessor.base.globalVariables.debug) println(
                preprocessor.base.globalVariables.depthAsString() +
                        "popping empty parenthesis token '$emptyParenthesis'"
            )
            expansion.append(emptyParenthesis.toString())
            emptyParenthesis.pop()
        } else if (newline.peek()) {
            if (preprocessor.base.globalVariables.debug) println(
                preprocessor.base.globalVariables.depthAsString() + "popping newline token '${
                newline
                    .toString()
                    .replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())
                }'"
            )
            newline.pop()
        } else if (
            (space.peek() && tokenSequence.lineInfo.column == 1)
            ||
            (tokenSequence.lineInfo.column == 1 && directive.peek())
        ) {
            /*
            5
Constraints
The only white-space characters that shall appear between preprocessing tokens within a prepro-
cessing directive (from just after the introducing # preprocessing token through just before the
terminating new-line character) are space and horizontal-tab (including spaces that have replaced
comments or possibly other white-space characters in translation phase 3).

             */
            if (space.peek()) {
                // case 1, space at start of file followed by define
                if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() + "popping space token '$space'")
                space.pop()
                expansion.append(" ")
            }
            if (directive.peek()) {
                if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() + "popping directive token '$directive'")
                directive.pop()

                if (stringize) {
                    stringize = false
                    concat = true
                } else stringize = true

                if (space.peek()) {
                    stringize = false
                    concat = false
                    // case 1, space at start of file followed by define
                    if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() + "popping space token '$space'")
                    space.pop()
                }
                if (abortDef.peek()) abort("#abort found")
                if (ignoreDef.peek()) return null
                if (defineDef.peek()) {
                    stringize = false
                    concat = false
                    // case 2, define at start of line
                    if (preprocessor.base.globalVariables.debug) println(
                        preprocessor.base.globalVariables.depthAsString() +
                                "popping ${Macro().Directives().Define().value} statement '${tokenSequence
                                    .toString().replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())
                                }'"
                    )
                    processDefine("${Macro().Directives().value}$tokenSequence", macro)
                    tokenSequence.clear()
                }
            }
        } else if (space.peek()) {
            if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() + "popping space token '$space'")
            space.pop()
            expansion.append(" ")
        } else {
            val index = macro.size - 1
            val ss = tokenSequence.peek()
            val name: String
            if (ss == null) abort(preprocessor.base.globalVariables.depthAsString() + "something is wrong")
            name = ss
            if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() + "popping normal token '$name'")
            /*
            kotlin supports new line statements but functions MUST not contain
            a new line between the identifier and the left parenthesis
             */
            val isAlphaNumarical: Boolean = name.matches("[A-Za-z0-9]*".toRegex())
            var macroFunctionExists = false
            var macroFunctionIndex = 0
            var macroObjectExists = false
            var macroObjectIndex = 0
            if (isAlphaNumarical) {
                macroFunctionIndex = macroExists(
                    name,
                    Macro().Directives().Define().Types().Function,
                    index,
                    macro
                )
                if (globalVariables.currentMacroExists) {
                    macroFunctionExists = true
                }
                macroObjectIndex = macroExists(
                    name,
                    Macro().Directives().Define().Types().Object,
                    index,
                    macro
                )
                if (globalVariables.currentMacroExists) {
                    macroObjectExists = true
                }
            }
            if (macroObjectExists || macroFunctionExists) {
                val occurrenceExists: Boolean
                if (!occurrence.name.contains(name)) occurrence
                    .apply { this.name.add(name) }
                    .apply { this.times.add(1) }
                    .also { occurrenceExists = false }
                else occurrence.times[occurrence.name.indexOf(name)] += 1.also { occurrenceExists = true }

                var isFunction = false

                if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() + "looking ahead")
                val tsa = tokenSequence.clone()
                val tsaSpace = tsa.IsSequenceOneOrMany(" ")
                val tsaLeftParenthesis = tsa.IsSequenceOnce("(")
                tsa.pop() // pop the function name
                if (tsaSpace.peek()) tsaSpace.pop() // pop any spaces in between
                if (tsaLeftParenthesis.peek()) isFunction = true

                var skip = false
                if (blacklist.contains(name)) skip = true
                tokenSequence.pop() // pop name
                if (isFunction) {
                    if (originalExpanding.equals(name) && originalExpandingType.equals(Macro().Directives().Define().Types().Function)) skip =
                        true
                    if (occurrenceExists) skip = false
                    if (ARGUnexpanded != null) {
                        macroExists(
                            name,
                            Macro().Directives().Define().Types().Function,
                            0,
                            ARGUnexpanded
                        )
                        if (globalVariables.currentMacroExists) skip = true
                    }
                    if (macroFunctionExists && !skip) {
                        if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() + "'$name' is a function")
                        // we know that this is a function, proceed to attempt to extract all arguments
                        /* 1) is identifying the bound variables
                              this is done automatically in processDefine
                         */
                        /* 1.5) For each argument passed to the function macro, expand that argument
                              replacing the original argument with its expansion
                        */
                        val eFS1R = expandFunctionStep1Point5(
                            depth = depth,
                            lex = lex,
                            tokenSequence = tokenSequence,
                            macro = macro,
                            space = space,
                            newline = newline,
                            leftParenthesis = leftParenthesis,
                            rightParenthesis = rightParenthesis,
                            leftBrace = leftBrace,
                            rightBrace = rightBrace,
                            leftBracket = leftBracket,
                            rightBracket = rightBracket,
                            comma = comma,
                            macroFunctionIndex = macroFunctionIndex,
                            index = index
                        )
                        if (preprocessor.base.globalVariables.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "eFS1R.argv = ${eFS1R.argv}"
                        )
                        // 2) Substitute all the bound variables from step 1 with the corresponding parameters.
                        val e1 = expandFunctionStep2(
                            depth = depth,
                            macro = macro,
                            macroUnexpanded = eFS1R.macroUnexpanded,
                            index = index,
                            name = name,
                            macroTypeDependantIndex = eFS1R.macroTypeDependantIndex,
                            arguments = macro[index].macros[eFS1R.macroTypeDependantIndex].arguments,
                            argv = eFS1R.argv,
                            replacementList = macro[index].macros[eFS1R.macroTypeDependantIndex].replacementList
                        )
                        // 3) rescan, if the original macro appears again in the output, it isn't expanded any further
                        if (preprocessor.base.globalVariables.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "rescanning, ARG = $ARG"
                        )
                        if (preprocessor.base.globalVariables.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "eFS1R.argv = ${eFS1R.argv}"
                        )
                        val eX = if (originalExpanding != null) originalExpanding
                        else if (eFS1R.argv.isNotEmpty()) name
                        else null
                        val eXT = if (originalExpandingType != null) originalExpandingType
                        else if (eFS1R.argv.isNotEmpty()) Macro().Directives().Define().Types().Function
                        else null
                        if (preprocessor.base.globalVariables.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "eX = $eX"
                        )
                        if (preprocessor.base.globalVariables.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "eXT = $eXT"
                        )
                        if (preprocessor.base.globalVariables.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "blacklisting $name"
                        )
                        if (macroUnexpanded != null) {
                            if (preprocessor.base.globalVariables.debug) println(
                                preprocessor.base.globalVariables.depthAsString() +
                                        "macroUnexpanded " +
                                        "${macroUnexpanded[index].macros[eFS1R.macroTypeDependantIndex].identifier} of type " +
                                        "${macroUnexpanded[index].macros[eFS1R.macroTypeDependantIndex].type} has value " +
                                        "${macroUnexpanded[index].macros[eFS1R.macroTypeDependantIndex].replacementList
                                            ?.replace(
                                                "\n",
                                                "\n" + preprocessor.base.globalVariables.depthAsString()
                                            )}"
                            )
                            abort("1")
                        }
                        if (preprocessor.base.globalVariables.debug) {
                            if (eFS1R.macroUnexpanded != null && eFS1R.macroUnexpanded.all { it.isNotBlank() }) {
                                println(
                                    preprocessor.base.globalVariables.depthAsString() +
                                            "ARG = " + ARG)
                                println(
                                    preprocessor.base.globalVariables.depthAsString() +
                                            "macroUnexpanded = " + eFS1R.macroUnexpanded)
                            }
                        }
                        blacklist.add(name)
                        val e2 = expandFunctionStep3(
                            depth = depth,
                            lex = lex,
                            macro = macro,
                            macroUnexpanded = toMacro(
                                definition = ARG,
                                replacementList = eFS1R.macroUnexpanded
                            ),
                            ARG = ARG,
                            ARGUnexpanded = toMacro(eFS1R.macroUnexpanded),
                            blacklist = blacklist,
                            expanding = expanding,
                            expandingType = expanding,
                            originalExpanding = eX,
                            originalExpandingType = eXT,
                            s = s,
                            c = c,
                            string = e1!!
                        )

                        if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() +
                                "expansion = '${expansion.toString()}'")
                        if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() +
                                "e2 = '$e2'")

                        // 4) put the result of substitution back into the source code
                        if (e2 != null) expansion.append(e2)
                        if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() +
                                "expansion = '${expansion.toString()}'")
                    } else {
                        if (preprocessor.base.globalVariables.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "'$name' is a function but no associated function macro exists"
                        )
                        expansion.append(name)
                    }
                } else {
                    if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() + "'$name' is an object")
                    if (macroObjectExists) {
                        /*
                        macros expand multiple times in the same context : #define a b > a a > b b not b a, here
                        `a` is blacklisted then expanded, if `a` occurs a second time in the same depth it should
                        be expanded regardless
                         */
                        if (originalExpanding.equals(name) && originalExpandingType.equals(Macro().Directives().Define().Types().Object)) skip =
                            true
                        if (occurrenceExists) skip = false
                        if (skip) {
                            if (preprocessor.base.globalVariables.debug) println(
                                preprocessor.base.globalVariables.depthAsString() +
                                        "but it is currently being expanded"
                            )
                            expansion.append(name)
                        } else {
                            if (preprocessor.base.globalVariables.debug) println(
                                preprocessor.base.globalVariables.depthAsString() +
                                        "and is not currently being expanded"
                            )

                            val macroTypeDependantIndex = macroObjectIndex
                            // Line is longer than allowed by code style (> 120 columns)
                            if (preprocessor.base.globalVariables.debug) println(
                                preprocessor.base.globalVariables.depthAsString() +
                                        "${macro[index].macros[macroTypeDependantIndex].identifier} of type " +
                                        "${macro[index].macros[macroTypeDependantIndex].type} has value " +
                                        "${macro[index].macros[macroTypeDependantIndex].replacementList
                                            ?.replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())}"
                            )
                            if (stringize) {
                                if (macroUnexpanded != null) {
                                    if (preprocessor.base.globalVariables.debug) println(
                                        preprocessor.base.globalVariables.depthAsString() +
                                                "macroUnexpanded " +
                                                "${macroUnexpanded[index].macros[macroTypeDependantIndex].identifier} of type " +
                                                "${macroUnexpanded[index].macros[macroTypeDependantIndex].type} has value " +
                                                "${macroUnexpanded[index].macros[macroTypeDependantIndex].replacementList
                                                    ?.replace(
                                                        "\n",
                                                        "\n" + preprocessor.base.globalVariables.depthAsString()
                                                    )}"
                                    )
                                    expansion.append(
                                        "\"${macroUnexpanded[index].macros[macroTypeDependantIndex].replacementList
                                            ?.collapse(" ")
                                            ?.replace("\"", "\\" + "\"")}\""
                                    )
                                } else abort("macroUnexpanded is null")
                                stringize = false
                            } else {
                                // Line is longer than allowed by code style (> 120 columns)
                                val replacementList =
                                    macro[index].macros[macroTypeDependantIndex].replacementList
                                            as String
                                val lex = Lexer(
                                    replacementList.toByteArray(),
                                    globalVariables.tokensNewLine
                                )
                                lex.lex()
                                if (lex.currentLine != null) {
                                    if (ARG != null) {
                                        if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() + "ARG = $ARG")
                                        if (!ARG.contains(name)) {
                                            if (preprocessor.base.globalVariables.debug) println(
                                                preprocessor.base.globalVariables.depthAsString() +
                                                        "blacklisting $name"
                                            )
                                            blacklist.add(name)
                                        } else {
                                            if (preprocessor.base.globalVariables.debug) println(
                                                preprocessor.base.globalVariables.depthAsString() +
                                                        "$name is an argument"
                                            )
                                        }
                                    } else {
                                        if (preprocessor.base.globalVariables.debug) println(
                                            preprocessor.base.globalVariables.depthAsString() +
                                                    "warning: ARG is null"
                                        )
                                        if (preprocessor.base.globalVariables.debug) println(
                                            preprocessor.base.globalVariables.depthAsString() +
                                                    "blacklisting $name"
                                        )
                                        blacklist.add(name)
                                    }
                                    val parser =
                                        Parser(parserPrep(lex.currentLine as String))
                                    val eX = if (originalExpanding != null) originalExpanding
                                    else if (depth == 0) name
                                    else null
                                    val eXT = if (originalExpandingType != null) originalExpandingType
                                    else if (depth == 0) Macro().Directives().Define().Types().Object
                                    else null
                                    val e = expand(
                                        depth = depth + 1,
                                        lex = lex,
                                        tokenSequence = parser,
                                        macro = macro,
                                        blacklist = blacklist,
                                        expanding = name,
                                        expandingType = Macro().Directives().Define().Types().Object,
                                        originalExpanding = eX,
                                        originalExpandingType = eXT,
                                        s = stringize,
                                        c = concat
                                    )!!
                                    preprocessor.base.globalVariables.depth = depth
                                    if (preprocessor.base.globalVariables.debug) println(
                                        preprocessor.base.globalVariables.depthAsString() +
                                                "macro Object expansion $name returned $e"
                                    )
                                    if (stringize) stringize = false
                                    if (concat) concat = false
                                    expansion.append(e)
                                }
                            }
                        }
                    } else {
                        if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() + "but does not exist as a macro")
                        expansion.append(name)
                    }
                }
            } else expansion.append(tokenSequence.pop()!!)
        }
        iterations++
    }
    if (iterations > maxIterations) if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() + "iterations expired")
    if (preprocessor.base.globalVariables.debug) {
        println(preprocessor.base.globalVariables.depthAsString() + "expansion = $expansion")
        println(preprocessor.base.globalVariables.depthAsString() + "PARAMETERS AT FUNCTION CALL END")
        println(preprocessor.base.globalVariables.depthAsString() + "expanding '${lex.currentLine?.replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())}'")
        println(preprocessor.base.globalVariables.depthAsString() + "tokenSequence = ${tokenSequence.toStringAsArray()}")
        println(preprocessor.base.globalVariables.depthAsString() + "ARG = $ARG")
        println(preprocessor.base.globalVariables.depthAsString() + "blacklist = $blacklist")
        println(preprocessor.base.globalVariables.depthAsString() + "expanding    : $expanding")
        println(preprocessor.base.globalVariables.depthAsString() + "expandingType: $expandingType")
    }
    return expansion.trimEnd().toString()
}

/**
 *
 */
@Suppress("ClassName")
data class expandFunctionStep1Point5Return(
    val argv: MutableList<String>,
    val macroUnexpanded: MutableList<String>?,
    val macroTypeDependantIndex: Int
)

/**
 *
 */
fun expandFunctionStep1Point5(
    depth: Int = 0,
    lex: Lexer,
    tokenSequence: Parser,
    macro: MutableList<Macro>,
    space: Parser.IsSequenceOneOrMany,
    newline: Parser.IsSequenceOnce,
    leftParenthesis: Parser.IsSequenceOnce,
    rightParenthesis: Parser.IsSequenceOnce,
    leftBrace: Parser.IsSequenceOnce,
    rightBrace: Parser.IsSequenceOnce,
    leftBracket: Parser.IsSequenceOnce,
    rightBracket: Parser.IsSequenceOnce,
    comma: Parser.IsSequenceOnce,
    macroFunctionIndex: Int,
    index: Int
): expandFunctionStep1Point5Return {
    var depthParenthesis = 0
    var depthBrace = 0
    var depthBracket = 0
    if (space.peek()) space.pop() // pop any spaces in between
    tokenSequence.pop() // pop the first (
    depthParenthesis++
    var iterations = 0
    val maxIterations = 100
    var argc = 0
    val argv: MutableList<String> = mutableListOf()
    argv.add("")
    while (iterations <= maxIterations) {
        if (newline.peek()) {
            newline.pop()
            if (tokenSequence.peek() == null) {
                if (preprocessor.base.globalVariables.debug) println(
                    preprocessor.base.globalVariables.depthAsString() +
                            "ran out of tokens, grabbing more tokens from the next line"
                )
                lex.lex()
                if (lex.currentLine == null) abort(
                    preprocessor.base.globalVariables.depthAsString() +
                            "no more lines when expecting more lines"
                )
                tokenSequence.tokenList = parserPrep(lex.currentLine as String)
            }
        }
        if (preprocessor.base.globalVariables.debug) println(
            preprocessor.base.globalVariables.depthAsString() +
                    "popping '${tokenSequence.peek()}'"
        )
        if (leftParenthesis.peek()) {
            depthParenthesis++
            argv[argc] = argv[argc].plus(tokenSequence.pop())
        } else if (leftBrace.peek()) {
            depthBrace++
            argv[argc] = argv[argc].plus(tokenSequence.pop())
        } else if (leftBracket.peek()) {
            depthBracket++
            argv[argc] = argv[argc].plus(tokenSequence.pop())
        } else if (rightParenthesis.peek()) {
            depthParenthesis--
            if (depthParenthesis == 0) {
                tokenSequence.pop()
                break
            }
            argv[argc] = argv[argc].plus(tokenSequence.pop())
        } else if (rightBrace.peek()) {
            depthBrace--
            argv[argc] = argv[argc].plus(tokenSequence.pop())
        } else if (rightBracket.peek()) {
            depthBracket--
            argv[argc] = argv[argc].plus(tokenSequence.pop())
        } else if (comma.peek()) {
            if (depthParenthesis == 1) {
                argc++
                argv.add("")
                comma.pop()
            } else argv[argc] = argv[argc].plus(tokenSequence.pop())
        } else argv[argc] = argv[argc].plus(tokenSequence.pop())
        iterations++
    }
    if (iterations > maxIterations) if (preprocessor.base.globalVariables.debug) println(
        preprocessor.base.globalVariables.depthAsString() + "iterations expired"
    )
    argc++
    if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() + "argc = $argc")
    if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() + "argv = $argv")
    val macroTypeDependantIndex = macroFunctionIndex
    // Line is longer than allowed by code style (> 120 columns)
    if (preprocessor.base.globalVariables.debug) println(
        preprocessor.base.globalVariables.depthAsString() +
                "${macro[index].macros[macroTypeDependantIndex].identifier} of type " +
                "${macro[index].macros[macroTypeDependantIndex].type} has value " +
                "${macro[index].macros[macroTypeDependantIndex].replacementList
                    ?.replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())}"
    )
    if (preprocessor.base.globalVariables.debug) println(
        preprocessor.base.globalVariables.depthAsString() +
                "macro  args = ${macro[index].macros[macroTypeDependantIndex].arguments}"
    )
    val macroUnexpanded: MutableList<String> = mutableListOf()
    var i = 0
    if (preprocessor.base.globalVariables.debug) println(
        preprocessor.base.globalVariables.depthAsString() +
                "expanding arguments: $argc arguments to expand"
    )
    while (i < argc) {
        macroUnexpanded.add(argv[i])
        // expand each argument
        val lex = Lexer(
            argv[i].toByteArray(),
            globalVariables.tokensNewLine
        )
        lex.lex()
        if (lex.currentLine != null) {
            val parser =
                Parser(parserPrep(lex.currentLine as String))
            val e = expand(
                depth = depth + 1,
                lex = lex,
                tokenSequence = parser,
                macro = macro,
                expanding = argv[i].substringBefore('('),
                expandingType = if (argv[i].contains('('))
                    Macro().Directives().Define().Types().Function
                else
                    Macro().Directives().Define().Types().Object
            )
            preprocessor.base.globalVariables.depth = depth
            if (preprocessor.base.globalVariables.debug) println(
                preprocessor.base.globalVariables.depthAsString() +
                        "macro expansion '${argv[i]}' returned $e"
            )
            argv[i] = e!!.trimStart().trimEnd()
        }
        i++
    }
    if (preprocessor.base.globalVariables.debug) println(
        preprocessor.base.globalVariables.depthAsString() +
                "expanded arguments: $argc arguments expanded"
    )
    if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() + "target args = $argv")
    if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() + "unexpanded target args = $macroUnexpanded")
    return expandFunctionStep1Point5Return(
        argv = argv,
        macroUnexpanded = if (macroUnexpanded.isNotEmpty()) macroUnexpanded else null,
        macroTypeDependantIndex = macroTypeDependantIndex
    )
}

/**
 *
 */
fun expandFunctionStep2(
    depth: Int = 0,
    macro: MutableList<Macro>,
    macroUnexpanded: MutableList<String>?,
    index: Int,
    name: String,
    macroTypeDependantIndex: Int,
    arguments: MutableList<String>?,
    argv: MutableList<String>,
    replacementList: String?
): String? = if (macro[index].macros[macroTypeDependantIndex].replacementList == null) null
else {
    val lex = Lexer(
        replacementList!!.toByteArray(),
        globalVariables.tokensNewLine
    )
    lex.lex()
    if (lex.currentLine != null) {
        val parser = Parser(parserPrep(lex.currentLine as String))
        val associatedArguments: MutableList<Macro>? = toMacro(arguments, argv)
        val associatedArgumentsUnexpanded: MutableList<Macro>? = toMacro(
            definition = arguments,
            replacementList = macroUnexpanded
        )
        if (associatedArguments == null || associatedArgumentsUnexpanded == null) abort("arguments are null")
        // Line is longer than allowed by code style (> 120 columns)
        if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() + "arguments" + arguments)
        val e = expand(
            depth = depth + 1,
            lex = lex,
            tokenSequence = parser,
            macro = associatedArguments,
            macroUnexpanded = associatedArgumentsUnexpanded,
            ARG = arguments,
            expanding = name,
            expandingType = Macro().Directives().Define().Types().Function
        )
        preprocessor.base.globalVariables.depth = depth
        e
    } else null
}

/**
 * rescan
 */
fun expandFunctionStep3(
    depth: Int,
    lex: Lexer,
    macro: MutableList<Macro>,
    macroUnexpanded: MutableList<Macro>?,
    ARG: MutableList<String>?,
    ARGUnexpanded: MutableList<Macro>?,
    blacklist: MutableList<String>,
    expanding: String?,
    expandingType: String?,
    originalExpanding: String?,
    originalExpandingType: String?,
    s: Boolean,
    c: Boolean,
    string: String
): String? {
    if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() +
            "string = '$string'")
    val lex2 = Lexer(string.toByteArray(), globalVariables.tokensNewLine)
    lex2.lex()
    if (lex2.currentLine != null) {
        val parser = Parser(parserPrep(lex2.currentLine as String))
        if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() +
                "lex.currentLine = '${lex.currentLine}'")
        if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() +
                "lex2.currentLine = '${lex2.currentLine}'")
        val e = expand(
            depth = depth + 1,
            lex = lex2,
            tokenSequence = parser,
            macro = macro,
            ARG = ARG,
            ARGUnexpanded = ARGUnexpanded,
            macroUnexpanded = macroUnexpanded,
            blacklist = blacklist,
            expanding = expanding,
            expandingType = expandingType,
            originalExpanding = originalExpanding,
            originalExpandingType = originalExpandingType,
            s = s,
            c = c
        )
        preprocessor.base.globalVariables.depth = depth
        return e
    } else return null
}