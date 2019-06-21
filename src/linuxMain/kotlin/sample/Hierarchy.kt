
class Hierarchy {
    private val indentation = 4

    private fun indent(depth: Int) = " ".repeat(indentation).repeat(depth)
    fun printHierarchy() = printHierarchy(0, rootFileSystem)
    private fun printHierarchy(depth: Int = 0, rootFileSystem: List<Any>) {
        var indexI = 0
        for (any in rootFileSystem) {
            when(any) {
                is List<*> -> printHierarchy(depth+1, any as List<Any>)
                else ->  when(indexI) {
                    0 -> println("${indent(if (depth == 0) 0 else depth)}directory = $any").also {indexI++}
                    1 -> println("${indent(depth + 1)}file: $any").also {indexI++}
                    2 -> println("${indent(depth + 1)}full file path: $any").also {indexI++}
                    3 -> {
                        println("${indent(depth + 1)}content: $any")
                        indexI = 1
                    }
                }
            }
        }
    }
    fun listFiles() = listFiles(rootFileSystem)
    private fun listFiles(rootFileSystem: List<Any>) {
        var indexI = 0
        for (any in rootFileSystem) {
            when(any) {
                is List<*> -> listFiles(any as List<Any>)
                else ->  when(indexI) {
                    0 -> println("directory = $any").also {indexI++}
                    1 -> println("file: $any").also {indexI++}
                    2 -> println("full file path: $any").also {indexI++}
                    3 -> {
                       indexI = 1 // skip contents
                    }
                }
            }
        }
    }
    fun find(file: String) = find(file, rootFileSystem)
    private fun find(file: String, rootFileSystem: List<Any>) {
        var indexI = 0
        for (any in rootFileSystem) {
            when(any) {
                is List<*> -> find(file, any as List<Any>)
                else ->  when(indexI) {
                    0 -> println("directory = $any").also {indexI++}
                    1 -> println("file: $any").also {indexI++}
                    2 -> println("full file path: $any").also {indexI++}
                    3 -> {
                       indexI = 1 // skip contents
                    }
                }
            }
        }
    }

    private val rootFileSystem = listOf(
        "Root",
        listOf(
            "linuxMain",
            listOf(
                "kotlin",
                listOf(
                    "preprocessor",
                    listOf(
                        "core",
                        "Parser.kt", "linuxMain/kotlin/preprocessor/core/Parser.kt", """package preprocessor.core

import preprocessor.base.globalVariables
import preprocessor.utils.core.abort
import preprocessor.utils.core.algorithms.Stack
import preprocessor.utils.`class`.extensions.tokenize

/**
 * a minimal Parser implementation
 * @param tokens a list of tokens to split by
 * @see preprocessor.globals.Globals.tokens
 * @see Lexer
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class Parser(tokens: String, stackMethod: (String) -> Stack<String>) {
    val stackMethodFunction = stackMethod

    /**
     * a line represented as an [Stack] as returned by [parserPrep]
     * @see preprocessor.globals.Globals.tokens
     */
    var tokenList: Stack<String> = stackMethodFunction(tokens)

    /**
     *
     */
    inner class InternalLineInfo {
        /**
         *
         */
        inner class Default {
            /**
             *
             */
            var column: Int = 1
            /**
             *
             */
            var line: Int = 1
        }

        /**
         *
         */
        var column: Int = Default().column
        /**
         *
         */
        var line: Int = Default().line
    }

    /**
     *
     */
    var lineInfo: InternalLineInfo = InternalLineInfo()

    /**
     * @return a new independent instance of the current [Parser]
     */
    fun clone(): Parser {
        return Parser(tokenList.clone().toStringConcat(), stackMethodFunction)
    }

    /**
     * wrapper for [Stack.peek]
     * @return [tokenList].[peek()][Stack.peek]
     */
    fun peek(): String? {
        return tokenList.peek()
    }

    /**
     * wrapper for [Stack.pop]
     * @return [tokenList].[pop()][Stack.pop] on success
     *
     * **null** on failure
     *
     * [aborts][abort] if the [tokenList] is corrupted
     */
    fun pop(): String? {
        if (tokenList.peek() == null) {
            try {
                tokenList.pop()
            } catch (e: NoSuchElementException) {
                return null
            }
            abort(preprocessor.base.globalVariables.depthAsString() + "token list is corrupted, or the desired exception 'java.util.NoSuchElementException' was not caught")
        }
        val returnValue = tokenList.pop() ?: abort(preprocessor.base.globalVariables.depthAsString() + "token list is corrupted")
        var i = 0
        while (i < returnValue.length) {
            if (returnValue[i] == '\n') {
                lineInfo.column = lineInfo.Default().column
                lineInfo.line++
            } else lineInfo.column++
            i++
        }
        return returnValue
    }

    /**
     * wrapper for [Stack.clear]
     * @return [tokenList].[clear()][Stack.clear]
     */
    fun clear() {
        tokenList.clear()
    }

    /**
     * @return the current [tokens][preprocessor.globals.Globals.tokens] left, converted into a [String]
     * @see stackToString
     */
    override fun toString(): String {
        return tokenList.toStringConcat()
    }

    /**
     * @return the current [tokens][preprocessor.globals.Globals.tokens] left, directly returns
     * [tokenList].[toString()][Stack.toString]
     */
    fun toStringAsArray(): String {
        return tokenList.toString()
    }

    /**
     * matches a sequence of **str** zero or more times
     * @see IsSequenceOneOrMany
     * @see IsSequenceOnce
     */
    inner class IsSequenceZeroOrMany(str: String) {
        val value: String = str
        private val seq: IsSequenceOneOrMany = IsSequenceOneOrMany(value)

        /**
         * returns the value of [IsSequenceOneOrMany.toString]
         *
         * the original [tokenList] is [cloned][Parser.clone] to prevent modifications to the original [list][tokenList]
         *
         * @return the result of [IsSequenceOneOrMany.toString], which can be an empty string
         * @see IsSequenceOneOrMany
         * @see peek
         * @see pop
         */
        override fun toString(): String {
            return seq.toString()
        }

        /**
         * this function always returns true
         * @see toString
         * @see pop
         */
        fun peek(): Boolean {
            return true
        }

        /**
         *
         * **WARNING**: this **CAN** modify [tokenList]
         *
         * **this relies on the safety of [IsSequenceOneOrMany.pop]**
         *
         * this function always returns true
         * @see IsSequenceOneOrMany
         * @see toString
         * @see peek
         */
        fun pop(): Boolean {
            while (seq.peek()) seq.pop()
            return true
        }
    }

    /**
     * matches a sequence of **str** one or more times
     * @see IsSequenceZeroOrMany
     * @see IsSequenceOnce
     */
    inner class IsSequenceOneOrMany(str: String) {
        val value: String = str

        /**
         * returns the accumulative value of [IsSequenceOnce.toString]
         *
         * the original [tokenList] is [cloned][Parser.clone] to prevent modifications to the original [list][tokenList]
         *
         * @return the accumalative result of [IsSequenceOnce.toString], which can be an empty string
         * @see IsSequenceOnce
         * @see peek
         * @see pop
         */
        override fun toString(): String {
            val o = clone().IsSequenceOnce(value)
            val result = StringBuilder()
            while (o.peek()) {
                result.append(o.toString())
                o.pop()
            }
            return result.toString()
        }

        /**
         *
         * the original [tokenList] is [cloned][Parser.clone] to prevent modifications to the original [list][tokenList]
         *
         * @return true if [value] matches at least once
         * @see IsSequenceOnce
         * @see toString
         * @see pop
         */
        fun peek(): Boolean {
            val o = clone().IsSequenceOnce(value)
            var matches = 0
            while (o.peek()) {
                matches++
                o.pop()
            }
            if (matches != 0) return true
            return false
        }

        /**
         *
         * **WARNING**: this **CAN** modify [tokenList]
         *
         * it is recommended to use ***if (Parser.peek()) Parser.pop()*** to avoid accidental modifications, where
         * **Parser** is an instance of [IsSequenceOneOrMany]
         *
         * @return true if [value] matches at least once
         * @see IsSequenceOnce
         * @see IsSequenceOnce.pop
         * @see toString
         * @see peek
         */
        fun pop(): Boolean {
            val o = IsSequenceOnce(value)
            var matches = 0
            while (o.peek()) {
                matches++
                o.pop()
            }
            if (matches != 0) return true
            return false
        }
    }

    /**
     * matches a sequence of **str**
     *
     * this is the base class of which all Parser sequence classes use
     *
     * based on [Stack] functions [toString][Stack.toString], [peek][Stack.peek], and
     * [pop][Stack.pop]
     *
     * @param str a string to match
     * @see IsSequenceZeroOrMany
     * @see IsSequenceOneOrMany
     */
    inner class IsSequenceOnce(str: String) {
        var left: IsSequenceOnce = this
        var right: IsSequenceOnce? = null
        var side = left

        val parent = this@Parser
        /**
         * the string to match
         */
        val value = str

        /**
         * returns the accumulative value of [Parser.peek] and [Parser.pop]
         *
         * the original [tokenList] is [cloned][Parser.clone] to prevent modifications to the original [list][tokenList]
         *
         * @return "" if either [tokenList] or [str][value] is empty
         *
         * otherwise the result of [Parser.peek], which can never be an empty string
         * @see peek
         * @see pop
         */
        override fun toString(): String {
            val tmp = tokenList.clone()
            val s = stackMethodFunction(value)
            if (s.peek() == null || tmp.peek() == null) return ""
            val result = StringBuilder()
            while (tmp.peek() != null && s.peek() != null) {
                val x = tmp.peek()
                if (tmp.pop() == s.pop()) result.append(x)
            }
            return result.toString()
        }

        /**
         * the original [tokenList] is [cloned][Parser.clone] to prevent modifications to the original [list][tokenList]
         *
         * @return false if [str][value] does not match
         *
         * otherwise returns true
         * @see toString
         * @see pop
         */
        fun peek(): Boolean {
            val tmp = tokenList.clone()
            val s = stackMethodFunction(value)
            if (s.peek() == null || tmp.peek() == null) return false
            val expected = s.size
            var matches = 0
            while (tmp.peek() != null && s.peek() != null) if (tmp.pop() == s.pop()) matches++
            if (matches == expected) return true
            return false
        }

        /**
         *
         * **WARNING**: this **CAN** modify [tokenList]
         *
         * it is recommended to use ***if (Parser.peek()) Parser.pop()*** to avoid accidental modifications, where
         * **Parser** is an instance of [IsSequenceOnce]
         *
         * @return false if [str][value] does not match
         *
         * otherwise returns true
         * @see toString
         * @see peek
         */
        fun pop(): Boolean {
            val s = stackMethodFunction(value)
            if (s.peek() == null) return false
            val expected = s.size
            var matches = 0
            while (this@Parser.peek() != null && s.peek() != null) if (this@Parser.pop().equals(s.pop())) matches++
            if (matches == expected) return true
            return false
        }

        infix fun and(right: IsSequenceOnce): IsSequenceOnce {
            val x = IsSequenceOnce(this.value)
            x.left = this
            x.right = right
            val y = IsSequenceOnce(right.value)
            x.left = x
            return y
        }

/*
        val A = parseStream.IsSequenceOnce("A")
        val B = parseStream.IsSequenceOnce("B")
        val C = parseStream.IsSequenceOnce("C")
*/
    }
}""",
                        "MacroTools.kt", "linuxMain/kotlin/preprocessor/core/MacroTools.kt", """@file:Suppress("unused")

package preprocessor.core

import preprocessor.base.globalVariables
import preprocessor.utils.`class`.extensions.tokenize
import preprocessor.utils.core.abort
import preprocessor.utils.core.algorithms.Stack
import preprocessor.utils.core.realloc

/**
 *
 * This class is used to house all macro definitions
 *
 * @sample Tests.generalUsage
 *
 * @see Directives
 * @see Directives.Define
 *
 */
class Macro {
    /**
     * this class is used to obtain predefined values such as directive names and types
     *
     * all preprocessor directives start with a [#][Macro.Directives.value]
     *
     * this CAN be changed if desired to run multiple variants of this preprocessor on the same file
     * @see Define
     */
    @Suppress("MemberVisibilityCanBePrivate")
    inner class Directives {
        /**
         * the preprocessor [directive][Directives] identification token
         *
         * this defaults to **#**
         */
        var value: String = "#"

        /**
         * contains predefined [values][Define.value] and [types][Define.Types] for the
         * [#][Macro.Directives.value][define][Define.value] directive
         * @see Directives
         */
        inner class Define {
            /**
             * the ***[#][Macro.Directives.value]define*** directive associates an identifier to a replacement list
             * the Default value of this is **define**
             * @see Types
             */
            val value: String = "define"

            /**
             * the valid types of a definition directive macro
             * @see Object
             * @see Function
             */
            inner class Types {
                /**
                 * the Object type denotes the standard macro definition, in which all text is matched with
                 *
                 * making **Object** lowercase conflicts with the top level declaration **object**
                 *
                 * @see Types
                 * @see Function
                 * @sample objectSample
                 * @sample objectUsage
                 */
                @Suppress("PropertyName")
                val Object: String = "object"
                /**
                 * the Function type denotes the Function macro definition, in which all text that is followed by
                 * parentheses is matched with
                 *
                 * making **Function** lowercase must mean [Object] must also be lowercase
                 * to maintain naming pairs (as **Object, function**, and **object, Function**
                 * just looks weird)
                 *
                 * unfortunately this is impossible as it would conflict with the top level
                 * declaration **object**
                 *
                 * @see Types
                 * @see Object
                 * @sample functionSample
                 * @sample functionUsage
                 */
                @Suppress("PropertyName")
                val Function: String = "function"

                private fun objectSample() {
                    /* ignore this block comment

                    #define object value
                    object
                    object(object).object[object]

                    ignore this block comment */
                }

                private fun objectUsage() {
                    val c = mutableListOf(Macro())
                    c[0].fileName = "test"
                    c[0].macros[0].fullMacro = "A B00"
                    c[0].macros[0].identifier = "A"
                    c[0].macros[0].replacementList = "B00"
                    c[0].macros[0].type =
                        Macro().Directives().Define().Types().Object
                    if (preprocessor.base.globalVariables.flags.debug) println(c[0].macros[0].type)
                }

                private fun functionSample() {
                    /* ignore this block comment

                    #define object value
                    #define function value
                    object
                    function(object).object[function(function()]

                    ignore this block comment */
                }

                private fun functionUsage() {
                    val c = mutableListOf(Macro())
                    c[0].fileName = "test"
                    c[0].macros[0].fullMacro = "A() B00"
                    c[0].macros[0].identifier = "A"
                    c[0].macros[0].replacementList = "B00"
                    c[0].macros[0].type =
                        Macro().Directives().Define().Types().Function
                    if (preprocessor.base.globalVariables.flags.debug) println(c[0].macros[0].type)
                }
            }
        }
    }

    /**
     * the internals of the Macro class
     *
     * this is where all macros are kept, this is managed via [realloc]
     * @sample Tests.generalUsage
     */
    inner class MacroInternal {
//        /**
//         * the current size of the [macro][MacroInternal] list
//         *
//         * can be used to obtain the last added macro
//         *
//         * @sample Tests.sizem
//         */
//        var size: Int = 0
        /**
         * the full macro definition
         *
         * contains the full line at which the definition appears
         */
        var fullMacro: String? = null
        /**
         * contains the definition **identifier**
         */
        var identifier: String? = null
        /**
         * contains the definition **type**
         */
        var type: String? = null
        /**
         * contains the definition **arguments**,
         * valid only for
         * [Function][Macro.Directives.Define.Types.Function]
         * type definitions
         *
         * this defaults to **null**
         *
         */
        var arguments: MutableList<String>? = null
        /**
         * this contains the definition replacement list
         */
        var replacementList: String? = null
    }

//    /**
//     * the current size of the [macro][Macro] list
//     *
//     * can be used to obtain the last added [macro group][MacroInternal]
//     *
//     * @sample Tests.size
//     */
//    var size: Int = 0
    /**
     * the name of the file containing this [macro][Macro] list
     */
    var fileName: String? = null
    /**
     * the [macro][MacroInternal] list
     */
    var macros: MutableList<MacroInternal>

    init {
//        this.size = 1
        macros = mutableListOf(MacroInternal())
    }

    private class Tests {
        fun generalUsage() {
            val c = mutableListOf(Macro())
            c[0].fileName = "test"
            c[0].macros[0].fullMacro = "A B"
            c[0].macros[0].identifier = "A"
            c[0].macros[0].replacementList = "B00"
            if (preprocessor.base.globalVariables.flags.debug) println(c[0].macros[0].replacementList)
            realloc(c, c.size + 1)
            c[1].fileName = "test"
            c[1].macros[0].fullMacro = "A B"
            c[1].macros[0].identifier = "A"
            c[1].macros[0].replacementList = "B10"
            if (preprocessor.base.globalVariables.flags.debug) println(c[1].macros[0].replacementList)
            realloc(c[1].macros, c[1].macros.size + 1)
            c[1].fileName = "test"
            c[1].macros[1].fullMacro = "A B"
            c[1].macros[1].identifier = "A"
            c[1].macros[1].replacementList = "B11"
            if (preprocessor.base.globalVariables.flags.debug) println(c[1].macros[1].replacementList)
            realloc(c[1].macros, c[1].macros.size + 1)
            c[1].fileName = "test"
            c[1].macros[2].fullMacro = "A B"
            c[1].macros[2].identifier = "A"
            c[1].macros[2].replacementList = "B12"
            if (preprocessor.base.globalVariables.flags.debug) println(c[1].macros[2].replacementList)
        }

        fun reallocUsage() {
            val c = mutableListOf(Macro())
            // allocate a new index
            realloc(c, c.size + 1)
            // assign some values
            c[0].fileName = "test"
            c[1].fileName = "test"
        }

        fun reallocUsageInternal() {
            val c = mutableListOf(Macro())
            // allocate a new index
            realloc(c[0].macros, c[0].macros.size + 1)
            // assign some values
            c[0].macros[0].fullMacro = "A A"
            c[0].macros[1].fullMacro = "A B"
        }

        fun size() {
            val c = mutableListOf(Macro())
            // allocate a new macro
            realloc(c[0].macros, c[0].macros.size + 1)
            c[0].macros[1].fullMacro = "A B"
            if (preprocessor.base.globalVariables.flags.debug) println(c[0].macros[1].replacementList)
            // obtain base index
            val index = c.size - 1
            // obtain last macro index
            val macroIndex = c[0].macros.size - 1
            if (c[index].macros[macroIndex].fullMacro.equals(c[0].macros[1].fullMacro))
                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "index matches")
        }

        fun sizem() {
            val c = mutableListOf(Macro())
            c[0].fileName = "test1"
            realloc(c, c.size + 1)
            c[1].fileName = "test2"
            val index = c.size - 1
            if (c[index].fileName.equals(c[1].fileName))
                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "index matches")
        }
    }
}

/**
 * checks if the desired macro exists in the [Macro] list
 */
fun macroExists(token: String, type: String, index: Int, macro: MutableList<Macro>): Int {
    globalVariables.status.currentMacroExists = false
    // if empty return 0 and do not set globalVariables.currentMacroExists
    if (macro[index].macros[0].fullMacro == null) return 0
    var i = 0
    while (i <= macro[index].macros.lastIndex) {
        if (macro[index].macros[i].identifier.equals(token) && macro[index].macros[i].type.equals(type)) {
            // Line is longer than allowed by code style (> 120 columns)
            if (preprocessor.base.globalVariables.flags.debug) println(
                preprocessor.base.globalVariables.depthAsString() +
                        "token and type matches existing definition ${'$'}{macro[index].macros[i].identifier} type " +
                        "${'$'}{macro[index].macros[i].type}"
            )
            globalVariables.status.currentMacroExists = true
            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "returning ${'$'}i")
            return i
        }
        // Line is longer than allowed by code style (> 120 columns)
        else if (preprocessor.base.globalVariables.flags.debug) println(
            preprocessor.base.globalVariables.depthAsString() +
                    "token ${'$'}token or type ${'$'}type does not match current definition token " +
                    "${'$'}{macro[index].macros[i].identifier} type ${'$'}{macro[index].macros[i].type}"
        )
        i++
    }
    return i
}

/**
 * lists the current macros in a [Macro] list
 */
fun macroList(index: Int = 0, macro: MutableList<Macro>) {
    if (macro[index].macros[0].fullMacro == null) return
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "LISTING macros")
    var i = 0
    while (i <= macro[index].macros.lastIndex) {
        // Line is longer than allowed by code style (> 120 columns)
        if (preprocessor.base.globalVariables.flags.debug) println(
            preprocessor.base.globalVariables.depthAsString() +
                    "[${'$'}i].fullMacro       = ${'$'}{macro[index].macros[i].fullMacro}"
        )
        if (preprocessor.base.globalVariables.flags.debug) println(
            preprocessor.base.globalVariables.depthAsString() +
                    "[${'$'}i].type            = ${'$'}{macro[index].macros[i].type}"
        )
        if (preprocessor.base.globalVariables.flags.debug) println(
            preprocessor.base.globalVariables.depthAsString() +
                    "[${'$'}i].identifier      = " +
                    "${'$'}{macro[index].macros[i].identifier}"
        )
        if (macro[index].macros[i].arguments != null)
        // Line is longer than allowed by code style (> 120 columns)
            if (preprocessor.base.globalVariables.flags.debug) println(
                preprocessor.base.globalVariables.depthAsString() +
                        "[${'$'}i].arguments       = ${'$'}{macro[index].macros[i].arguments}"
            )
        // Line is longer than allowed by code style (> 120 columns)
        if (preprocessor.base.globalVariables.flags.debug) println(
            preprocessor.base.globalVariables.depthAsString() +
                    "[${'$'}i].replacementList = ${'$'}{macro[index].macros[i].replacementList
                        ?.replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())}"
        )
        i++
    }
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "LISTED macros")
}

/**
 * lists the current macros in a [Macro] list
 *
 * this version lists ALL [Macro]s in the current [Macro] list in all available file index's
 */
fun macroList(macro: MutableList<Macro>) {
    if (macro.size == 0) {
        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "macro list is empty")
        return
    }
    var i = 0
    while (i < macro.size) {
        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "LISTING macros for file ${'$'}{macro[i].fileName}")
        macroList(i, macro)
        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "LISTED macros for file ${'$'}{macro[i].fileName}")
        i++
    }
}

/**
 * converts a [List] and a [List] into a [Macro] array
 */
fun toMacro(definition: List<String>?, replacementList: List<String>?): MutableList<Macro>? {
    if (definition == null) return null
    if (replacementList == null) return toMacro(definition)
    // Line is longer than allowed by code style (> 120 columns)
    if (preprocessor.base.globalVariables.flags.debug) println(
        preprocessor.base.globalVariables.depthAsString() +
                "${'$'}{definition.size} == ${'$'}{replacementList.size} is " +
                "${'$'}{definition.size == replacementList.size}"
    )
    if (definition.size != replacementList.size) {
        // Line is longer than allowed by code style (> 120 columns)
        abort(
            preprocessor.base.globalVariables.depthAsString() +
                    "size mismatch: expected ${'$'}{definition.size}, got ${'$'}{replacementList.size}"
        )
    }
    val associatedArguments = mutableListOf(Macro())
    var i = 0
    associatedArguments[0].macros[i].fullMacro =
        "${'$'}{Macro().Directives().Define().value} ${'$'}{definition[i]} ${'$'}{replacementList[i]}"
    associatedArguments[0].macros[i].type = Macro().Directives().Define().Types().Object
    associatedArguments[0].macros[i].identifier = definition[i]
    associatedArguments[0].macros[i].replacementList = replacementList[i]
    i++
    while (i <= definition.lastIndex) {
        // Line is longer than allowed by code style (> 120 columns)
        realloc(
            associatedArguments[0].macros,
            associatedArguments[0].macros.size + 1
        )
        associatedArguments[0].macros[i].fullMacro =
            "${'$'}{Macro().Directives().Define().value} ${'$'}{definition[i]} ${'$'}{replacementList[i]}"
        associatedArguments[0].macros[i].type = Macro().Directives().Define().Types().Object
        associatedArguments[0].macros[i].identifier = definition[i]
        associatedArguments[0].macros[i].replacementList = replacementList[i]
        i++
    }
    macroList(macro = associatedArguments)
    return associatedArguments
}

/**
 * converts an [MutableList] and a [List] into a [Macro] array
 */
fun toMacro(definition: MutableList<String>?, replacementList: List<String>?): MutableList<Macro>? =
    toMacro(definition?.toList(), replacementList)

/**
 * converts an [List] and a [MutableList] into a [Macro] array
 */
fun toMacro(definition: List<String>?, replacementList: MutableList<String>?): MutableList<Macro>? =
    toMacro(definition, replacementList?.toList())

/**
 * converts an [MutableList] and a [MutableList] into a [Macro] array
 */
fun toMacro(definition: MutableList<String>?, replacementList: MutableList<String>?): MutableList<Macro>? =
    toMacro(definition?.toList(), replacementList?.toList())

/**
 * converts an [MutableList] into a [Macro] array, value is null
 */
fun toMacro(definition: MutableList<String>?): MutableList<Macro>? =
    toMacro(definition?.toList())

/**
 * converts an [List] into a [Macro] array, value is null
 */
fun toMacro(definition: List<String>?): MutableList<Macro>? {
    return when {
        definition == null -> null
        definition.isNotEmpty() -> mutableListOf(Macro()).also { var0 ->
            definition.forEach {
                processDefine("#define ${'$'}it", var0)
            }
        }.also {
            macroList(macro = it)
        }
        else -> null
    }
}

/**
 * prepares the input **line** for consumption by the macro [Parser]
 * @return an [Stack] which is [split][tokenize] by the global variable **tokens**
 *
 * this [Stack] preserves the tokens in which it is split by
 * @see Parser
 */
fun parserPrep(line: String): Stack<String> = Stack<String>().also { it.addLast(line.tokenize(globalVariables.tokens, true)) }
""",
                        "Lexer.kt", "linuxMain/kotlin/preprocessor/core/Lexer.kt", """package preprocessor.core

import preprocessor.utils.core.algorithms.Stack
import preprocessor.utils.`class`.extensions.toStack

/**
 * a minimal Lexer implementation
 *
 * we traditionally process the input file character by character
 *
 * append each processed character to a buffer
 *
 * then return that buffer when a specific delimiter is found
 *
 * @param stm the byte buffer to read from, set up via
 * [fileToByteArray][preprocessor.utils.conversion.fileToByteArray]
 * @param delimiter the delimiters to split the input by
 * @see Parser
 * @see Lexer.lex
 */
@Suppress("unused")
class Lexer(stm: ByteArray, delimiter: String) {
    private val f: ByteArray = stm
    private var i = 0
    private fun hasNext() = i<f.size
    private fun next() = f[i++]
    private val delimiters: String = delimiter
    private val d: Stack<String> = delimiters.toStack()
    /**
     * this is the current line the Lexer of on
     *
     */
    var currentLine: String? = null

    /**
     *
     */
    inner class InternalLineInfo {
        /**
         *
         */
        inner class Default {
            /**
             *
             */
            var column: Int = 1
            /**
             *
             */
            var line: Int = 1
        }

        /**
         *
         */
        var column: Int = Default().column
        /**
         *
         */
        var line: Int = Default().line
    }

    private var lineInfo: InternalLineInfo = InternalLineInfo()

    /**
     * returns an exact duplicate of the current [Lexer]
     */
    fun clone(): Lexer {
        return Lexer(this.f.copyOf(), this.delimiters)
    }

    /**
     * returns a duplicate of this [Lexer] with the specified delimiters
     */
    fun clone(newDelimiters: String): Lexer {
        return Lexer(this.f.copyOf(), newDelimiters)
    }

    /**
     * sets the variable [currentLine] to the
     * [buffer][ByteArray] (converted to a
     * [String]) when a specific
     * [delimiter][Lexer] is found
     *
     * sets the variable [currentLine] to
     * **null** upon EOF
     */
    fun lex() {
        /*
        in order to make a Lexer, we traditionally process the input file
        character by character, appending each to a buffer, then returning
        that buffer when a specific delimiter is found
        */
        if (!hasNext()) {
            currentLine = null
            return
        }
        val b = StringBuilder()
        while (hasNext()) {
            val s = next().toChar().toString()
            b.append(s)
            if (s == "\n") {
                lineInfo.column = lineInfo.Default().column
                lineInfo.line++
            } else lineInfo.column++
            if (d.contains(s)) break
        }
        currentLine = b.toString()
        return
    }
}
""",
                        "process.kt", "linuxMain/kotlin/preprocessor/core/process.kt", """package preprocessor.core

import preprocessor.utils.extra.Balanced
import preprocessor.utils.extra.extractArguments
import preprocessor.base.globalVariables
import preprocessor.utils.core.abort
import preprocessor.utils.extra.parse
//import preprocessor.utils.conversion.fileToByteArray
import preprocessor.utils.core.realloc
//import java.io.File
//
///**
// * pre-processes a file **src**
// *
// * the result is saved in "${'$'}src${'$'}{globalVariables.preprocessedExtension}${'$'}extensions"
// *
// * @param src the file to be modified
// * @param extension the extension of file specified in **src**
// * @param macro a [Macro] array
// */
//fun process(
//    src: String,
//    extension: String,
//    macro: MutableList<Macro>
//) {
//    val destinationPreProcessed = File("${'$'}src${'$'}{globalVariables.preprocessedExtension}.${'$'}extension")
//    var index = macro.size - 1
//    if (macro[index].fileName != null) {
//        index++
//        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "reallocating to ${'$'}index")
//        realloc(macro, index + 1)
//    }
//    macro[index].fileName = src.substringAfterLast('/')
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "registered macro definition for ${'$'}{macro[index].fileName} at index ${'$'}index")
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "processing ${'$'}{macro[index].fileName} -> ${'$'}{destinationPreProcessed.name}")
//    destinationPreProcessed.createNewFile()
//    val lex = Lexer(fileToByteArray(File(src)), globalVariables.tokensNewLine)
//    lex.lex()
//    if (preprocessor.base.globalVariables.flags.debug) println(
//        preprocessor.base.globalVariables.depthAsString() + "lex.currentLine is ${'$'}{lex.currentLine
//            ?.replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())}"
//    )
//    while (lex.currentLine != null) {
//        val out = parse(lex, macro)
//        if (out == null) return
//        var input = lex.currentLine as String
//        if (input[input.length - 1] == '\n') {
//            input = input.dropLast(1)
//        }
//        if (preprocessor.base.globalVariables.flags.debug) println(
//            preprocessor.base.globalVariables.depthAsString() + "\ninput = ${'$'}input"
//                .replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())
//        )
//        if (preprocessor.base.globalVariables.flags.debug) println(
//            preprocessor.base.globalVariables.depthAsString() + "output = ${'$'}out\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
//                .replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())
//        )
//        if (globalVariables.firstLine) {
//            destinationPreProcessed.writeText(out + "\n")
//            globalVariables.firstLine = false
//        } else destinationPreProcessed.appendText(out + "\n")
//        lex.lex()
//    }
//}
//

/**
 * adds each **line** to the given [macro][Macro] list
 *
 * assumes each **line** is a valid **[#][Macro.Directives.value][define][Macro.Directives.Define.value]** directive
 */
fun processDefine(line: String, macro: MutableList<Macro>) {
    val fullMacro: String = line.trimStart().removePrefix(Macro().Directives().value).trimStart()
    if (fullMacro.substringAfter(' ').isBlank()) {
        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "empty define statement")
        return
    }

    val index = macro.size - 1
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "saving macro in to index ${'$'}index")
    var macroIndex = macro[index].macros.size - 1
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "saving macro in to macro index ${'$'}macroIndex")
    // to include the ability to redefine existing definitions, we must save to local variables first
    // determine token type
    // Line is longer than allowed by code style (> 120 columns)
    val type: String = if (fullMacro.substringAfter(' ')
            .trimStart()
            .substringBefore(' ')
            .trimStart() == fullMacro.substringAfter(' ')
            .trimStart()
            .substringBefore(' ')
            .trimStart().substringBefore('(')
    )
        Macro().Directives().Define().Types().Object
    else
        Macro().Directives().Define().Types().Function
    var token: String
    if (type == Macro().Directives().Define().Types().Object) {
        var empty = false
        // object
        token =
            fullMacro.substringAfter(' ').trimStart().substringBefore(' ').trimStart()
        if (token[token.length - 1] == '\n') {
            token = token.dropLast(1)
            empty = true
        }
        val i = macroExists(token, type, index, macro)
        macroIndex = if (globalVariables.status.currentMacroExists) {
            i
        } else {
            if (macro[index].macros[macroIndex].fullMacro != null) {
                realloc(
                    macro[index].macros,
                    macro[index].macros.size + 1
                )
            }
            macro[index].macros.size - 1
        }
        macro[index].macros[macroIndex].fullMacro = line.trimStart().trimEnd()
        macro[index].macros[macroIndex].identifier = token
        macro[index].macros[macroIndex].type = type
        if (!empty) {
            macro[index].macros[macroIndex].replacementList =
                fullMacro.substringAfter(' ').trimStart().substringAfter(' ').trimStart().trimStart()
        } else macro[index].macros[macroIndex].replacementList = ""
    } else {
        // function
        token =
            fullMacro.substringAfter(' ').substringBefore('(').trimStart()
        val i = macroExists(token, type, index, macro)
        macroIndex = if (globalVariables.status.currentMacroExists) {
            i
        } else {
            if (macro[index].macros[macroIndex].fullMacro != null) {
                realloc(
                    macro[index].macros,
                    macro[index].macros.size + 1
                )
            }
            macro[index].macros.size - 1
        }
        macro[index].macros[macroIndex].fullMacro = line.trimStart().trimEnd()
        macro[index].macros[macroIndex].identifier = token
        macro[index].macros[macroIndex].type = type
        // obtain the function arguments
        val t = macro[index].macros[macroIndex].fullMacro?.substringAfter(' ')!!
        val b = Balanced()
        macro[index].macros[macroIndex].arguments = extractArguments(b.extractText(t).drop(1).dropLast(1))
        macro[index].macros[macroIndex].replacementList = if (b.end[0] >= t.length) null
        else t.substring(b.end[0] + 1).trimStart()
    }
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "type       = ${'$'}{macro[index].macros[macroIndex].type}")
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "token      = ${'$'}{macro[index].macros[macroIndex].identifier}")
    if (macro[index].macros[macroIndex].arguments != null)
        if (preprocessor.base.globalVariables.flags.debug) println(
            preprocessor.base.globalVariables.depthAsString() +
                    "arguments  = ${'$'}{macro[index].macros[macroIndex].arguments}"
        )
    if (preprocessor.base.globalVariables.flags.debug) println(
        preprocessor.base.globalVariables.depthAsString() +
                "replacementList      = ${'$'}{macro[index].macros[macroIndex].replacementList
                    ?.replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())
                }"
    )
    macroList(index, macro)
    // definition names do not expand
    // definition values do expand
}
"""
                    ),
                    listOf(
                        "base",
                        "globalVariables.kt", "linuxMain/kotlin/preprocessor/base/globalVariables.kt", """package preprocessor.base

import preprocessor.globals.Globals

/**
 * @see Globals
 */
val globalVariables: Globals = Globals()
"""
                    ),
                    listOf(
                        "globals",
                        "Globals.kt", "linuxMain/kotlin/preprocessor/globals/Globals.kt", """package preprocessor.globals

import preprocessor.core.Macro
import preprocessor.utils.core.basename
//import java.io.File

/**
 * the globals class contains all global variables used by this library
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class Globals {

    val version = 1.0

    val flags = Flags()
    val status = Status()

    /**
     * `<space> or <tab>`
     * @see tokens
     */
    val tokensSpace: String = " \t"

    /**
     * `<newline>`
     *
     * (
     *
     * \n or
     *
     * "new
     *
     * line"
     *
     * )
     * @see tokens
     */
    val tokensNewLine: String = "\n"

    /**
     * ```
     * \
     * "
     * /
     * *
     * ```
     * [Macro.Directives.value]
     * ```
     * (
     * )
     * .
     * ,
     * -
     * >
     * {
     * }
     * [
     * ]
     * ```
     * @see tokens
     */
    val tokensExtra: String = "\\\"'/*${'$'}{Macro().Directives().value}().,->{}[]"
    /**
     * ```
     * +
     * -
     * *
     * /
     * ```
     * @see tokens
     */
    val tokensMath: String = "+-*/"

    /**
     * the Default list of tokens
     *
     * this is used in general tokenization and [Macro] expansion
     *
     * **tokens = [tokensSpace] + [tokensNewLine] + [tokensExtra] + [tokensMath]**
     */
    val tokens: String = tokensSpace + tokensNewLine + tokensExtra + tokensMath

    /**
     * the current depth
     */
    var depth: Int = 0
    /**
     * returns a depth string
     */
    fun depthAsString(): String = "    ".repeat(depth) + "depth:${'$'}depth > "

//    /**
//     * the current project directory that this task has been called from
//     * @see projectDirectoryBaseName
//     * @see rootDirectory
//     */
//    var projectDirectory: File? = null
//    /**
//     * the basename of [projectDirectory]
//     * @see rootDirectoryBaseName
//     */
//    var projectDirectoryBaseName: String? = null
//    /**
//     * the root project directory
//     * @see rootDirectoryBaseName
//     * @see projectDirectory
//     */
//    var rootDirectory: File? = null
//    /**
//     * the basename of [rootDirectory]
//     * @see projectDirectoryBaseName
//     */
//    var rootDirectoryBaseName: String? = null
//
//    /**
//     * the Default [macro][Macro] list
//     */
//    var kppMacroList: MutableList<Macro> = mutableListOf(Macro())
//
//    /**
//     * the directory that **kpp** is contained in
//     */
//    var kppDir: String? = null
//    /**
//     * the directory that **kpp** is contained in
//     */
//    var kppDirAsFile: File? = null
//    /**
//     * the suffix to give files that have been processed by kpp
//     */
//    var preprocessedExtension: String = ".preprocessed"
//
//    /**
//     * initializes the global variables
//     *
//     * it is recommended to call this function as `Globals().initGlobals(rootDir, projectDir)`
//     *
//     * replace `Globals()` with your instance of the `Globals` class
//     * @sample globalsSample
//     */
//    fun initGlobals(rootDir: File, projectDir: File) {
//        projectDirectory = projectDir
//        projectDirectoryBaseName = basename(projectDirectory)
//        rootDirectory = rootDir
//        rootDirectoryBaseName = basename(rootDirectory)
//        kppDir = rootDirectory.toString() + "/kpp"
//        kppDirAsFile = File(kppDir)
//    }
//
//    /**
//     * initializes the global variables
//     *
//     * it is recommended to call this function as `Globals().initGlobals(rootDir, projectDir)`
//     *
//     * replace `Globals()` with your instance of the `Globals` class
//     * @sample globalsSample
//     */
//    fun initGlobals(rootDir: String, projectDir: String) {
//        initGlobals(File(rootDir), File(projectDir))
//    }
}

//private fun globalsSample(rootDir: File, projectDir: File) {
//    val globals = Globals()
//    globals.initGlobals(rootDir, projectDir)
//    //rootDir is usually provided within the task itself
//    //projectDir is usually provided within the task itself
//}""",
                        "Flags.kt", "linuxMain/kotlin/preprocessor/globals/Flags.kt", """package preprocessor.globals

/**
 * global flags that affect how the preprocessor behaves
 */
class Flags {
    /**
     * prints debug output if this value is true
     */
    var debug: Boolean = false
}
""",
                        "Status.kt", "linuxMain/kotlin/preprocessor/globals/Status.kt", """package preprocessor.globals

/**
 * status variables
 */
class Status {
    /**
     * this is used by [testFile][preprocessor.utils.Sync.testFile]
     */
    var currentFileContainsPreprocessor: Boolean = false

    /*
        TODO: implement file cache
        var currentFileIsCashed: Boolean = false
        var cachedFileContainsPreprocessor: Boolean = false
    */

    /**
     *
     */
    var firstLine: Boolean = true
    /**
     *
     */
    var currentMacroExists: Boolean = false
    /**
     *
     */
    var abortOnComplete: Boolean = true

}
"""
                    ),
                    listOf(
                        "test",
                        "Tests.kt", "linuxMain/kotlin/preprocessor/test/Tests.kt", """package preprocessor.test

import preprocessor.test.tests.*
import preprocessor.utils.core.abort

/**
 * holds all the tests for this library
 */
class Tests {
    private fun begin(name: String = "Tests", message: String = "starting ${'$'}name"): Unit =
        println(message)

    private fun end(name: String = "Tests", message: String = "${'$'}name finished"): Unit =
        println(message)

    /**
     * if this value is true, the function will abort if all tests pass
     */
    var abortOnComplete: Boolean = false

    fun doAll() {
        begin()
        general()
        stringize()
        selfReferencing()
        if (abortOnComplete) {
            end()
//            abort("All Tests Passed")
        }
    }
}""",
                        listOf(
                            "tests",
                            "selfReferencing.kt", "linuxMain/kotlin/preprocessor/test/tests/selfReferencing.kt", """package preprocessor.test.tests

import preprocessor.test.init
import preprocessor.test.utils.expect
import preprocessor.utils.extra.parse

/**
 * tests self referencing macros
 */
fun selfReferencing() {
    val m = init()
    parse("#define a a", m)
    expect("a", "a", m)
    parse("#define d l k j\n", m)
    expect("d", "l k j", m)
    parse("#define a(x) b() x\n" +
                "#define b(x) c() x\n" +
                "#define c(x) d a() x\n", m
    )
    expect("a(\"3\" \"2\" \"1\" \"x\")", "l k j a() \"3\" \"2\" \"1\" \"x\"", m)
    parse("#define x(x) x\n", m)
    expect("x(a(\"3\" \"2\" \"1\" \"x\"))", "l k j a() \"3\" \"2\" \"1\" \"x\"", m)
}""",
                            "stringize.kt", "linuxMain/kotlin/preprocessor/test/tests/stringize.kt", """package preprocessor.test.tests

import preprocessor.test.init
import preprocessor.test.utils.expect
import preprocessor.utils.extra.parse

/**
 * tests stringize
 */
fun stringize() {
    val m = init()
    parse(" #define s(x) #x -> x", m)
    expect("s(a)", "\"a\" -> a", m)

    parse("#define a b", m)
    expect("s(a b a)", "\"a b a\" -> b b b", m)
    expect("s(a    b    a)", "\"a b a\" -> b b b", m)
}""",
                            "general.kt", "linuxMain/kotlin/preprocessor/test/tests/general.kt", """package preprocessor.test.tests

import preprocessor.test.init
import preprocessor.test.utils.expect
import preprocessor.utils.core.abort
import preprocessor.utils.extra.parse

/**
 * tests general capabilities
 */
fun general() {
    val m = init()
    parse(
        "#define a(b) b\n" +
                "#define c() d c\n" +
                "#define d Once\n",
        m
    )
    expect("a(    c()    )", "Once c", m)

    parse("#define c() d d", m)
    expect("a(    c()    )", "Once Once", m)
    expect("d", "Once", m)

    parse(
        "#define a(b, y) b y\n" +
                "#define c() d c\n" +
                "#define e() d c\n",
        m
    )
    expect("a(    c()    ,    e()    )", "Once c Once c", m)

    parse("#define a b", m)
    expect("a", "b", m)

    parse("#define f g", m)
    expect("f", "g", m)

    parse("#define x y", m)
    expect("x", "y", m)
    expect("a", "b", m)
    expect("aa", "aa", m)
    expect("a a", "b b", m)
    expect("a N a", "b N b", m)

    parse("#define a(x) {x}", m)
    expect("a()k", "{}k", m)

    parse("#define a(x) {b()x}", m)
    parse("#define b(x) {x}", m)
    expect("a()k", "{{}}k", m)

    parse("#define a(x) {b() x}", m)
    parse("#define b(x) { x}", m)
    expect("a()k", "{{ } }k", m)

    parse("#define a(x) [b() x]", m)
    parse("#define b(x) [1 x", m)
    expect("a(2)", "[[1 2]", m)

    parse("#define b(x) [1 x]", m)
    expect("a(2)", "[[1 ] 2]", m)

    parse("#define b(x) [1 x ]", m)
    expect("a(2)", "[[1 ] 2]", m)

    parse("#define n(x) x", m)
    expect("n(\nG\n)", "G", m)
}"""
                        ),
                        "init.kt", "linuxMain/kotlin/preprocessor/test/init.kt", """package preprocessor.test

import preprocessor.core.Macro

/**
 * initializes the Macro list
 * @return a new Macro list
 */
fun init(): MutableList<Macro> {
    return mutableListOf<Macro>(Macro())
}""",
                        listOf(
                            "utils",
                            "expect.kt", "linuxMain/kotlin/preprocessor/test/utils/expect.kt", """package preprocessor.test.utils

import preprocessor.core.Macro
import preprocessor.utils.core.abort
import preprocessor.utils.extra.parse

/**
 * [parses][parse] given [input] against its result against [output]
 *
 * @return true if [output] matches
 * otherwise false
 *
 * @param input the string to be checked
 * @param output the string that should result from the parsing of [input]
 * @param macro the given macro list
 */
fun expect(input: String, output: String, macro: MutableList<Macro>): Boolean {
    val check = parse(input, macro)
    return when {
        check == null -> false
        check.equals(output) -> {
            println("expect(\"${'$'}input\", \"${'$'}output\", <macro>) passed")
            true
        }
        else -> {
            println("expect(\"${'$'}input\", \"${'$'}output\", <macro>) failed")
            val t = "    "
            val e = t + "expected  : "
            val g = t + "got       : "
            val i = t + "input was : "
            println(e + output.replace("\n", "\n" + " ".repeat(e.length)))
            println(g +  check.replace("\n", "\n" + " ".repeat(g.length)))
            println(i +  input.replace("\n", "\n" + " ".repeat(i.length)))
            abort()
        }
    }
}
"""
                        )
                    ),
                    listOf(
                        "utils",
                        listOf(
                            "conversion",
                            "fileToByteBuffer.kt", "linuxMain/kotlin/preprocessor/utils/conversion/fileToByteBuffer.kt", """//package preprocessor.utils.conversion
//
//import java.io.File
//import java.io.RandomAccessFile
//import java.nio.ByteArray
//
///**
// * converts a [File] into a [ByteArray]
// * @return the resulting conversion
// * @see stringToByteArray
// */
//fun fileToByteArray(f: File): ByteArray {
//    val file = RandomAccessFile(f, "r")
//    val fileChannel = file.channel
//    val buffer = ByteArray.allocate(fileChannel.size().toInt())
//    fileChannel.read(buffer)
//    buffer.flip()
//    return buffer
//}
"""
                        ),
                        "Sync.kt", "linuxMain/kotlin/preprocessor/utils/Sync.kt", """//package preprocessor.utils
//
//import preprocessor.base.globalVariables
//import preprocessor.core.macroList
//import preprocessor.core.process
//import java.io.File
//import preprocessor.utils.core.*
//import java.nio.file.Files
//
///**
// *
// *//*
//if A/FILE exists
//    if B/FILE does not exist
//        delete A/FILE
//            if A/FILE.PRO exists
//                delete A/FILE.PRO
//    else
//        if A/FILE contains DATA
//            KEEP A/FILE
//            if A/FILE.PRO exists
//                if A/FILE contains DATA
//                    KEEP A/FILE.PRO
//        else delete A/FILE
//            if A/FILE.PRO exists
//                delete A/FILE.PRO
//else if B/FILE exists
//    if B/FILE contains DATA
//        copy B/FILE to A/FILE
// */
//class Sync {
//
//    private val ignore: MutableList<String> = mutableListOf(
//        // ignore png files
//        "png",
//        // ignore proguard files
//        "pro",
//        // ignore gradle files
//        "gradle",
//        // ignore module files
//        "iml",
//        // ignore git files
//        "gitignore"
//    )
//
//    /**
//     * syncs directory **B** (**src**) with directory **A** (**dir**)
//     *
//     * this needs to be called BEFORE [syncA] in order to sync correctly
//     * @see syncA
//     * @sample findSourceFiles
//     */
//    private fun syncB(dir: File, src: File, extension: String? = null) {
//        dir.listFiles().forEach {
//            val a = it
//            val b = File(
//                // Line is longer than allowed by code style (> 120 columns)
//                globalVariables.rootDirectory.toString() + '/' + basename(globalVariables.kppDir) +
//                        a.toString().removePrefix(globalVariables.rootDirectory!!.path)
//            )
//            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "A :     ${'$'}a")
//            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "B : ${'$'}b")
//            if (a.exists()) {
//                if (a.isDirectory) {
//                    // ignore build dir in A
//                    // if build dir exists in B, delete it
//                    val blocked = a.toString() == globalVariables.projectDirectory?.path + "/build"
//                    if (!blocked) {
//                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "entering ${'$'}a")
//                        syncB(a, src, extension)
//                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "leaving ${'$'}a")
//                    } else if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "A is blocked")
//                    if (b.exists()) {
//                        if (blocked) {
//                            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting B")
//                            deleteRecursive(b)
//                        } else if (empty(b)) {
//                            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting B")
//                            delete(b)
//                        }
//                    }
//                    val aPreProcessed = File(a.path + globalVariables.preprocessedExtension + "." + a.extension)
//                    val bPreProcessed = File(b.path + globalVariables.preprocessedExtension + "." + b.extension)
//                    if (aPreProcessed.exists()) {
//                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting preprocessing file associated with A")
//                        delete(aPreProcessed)
//                    }
//                    if (bPreProcessed.exists()) {
//                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting preprocessing file associated with B")
//                        delete(bPreProcessed)
//                    }
//                } else if (a.isFile) {
//                    if (a.path.endsWith(globalVariables.preprocessedExtension + "." + a.extension)) {
//                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "A is preprocessor file")
//                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting A")
//                        delete(a)
//                    } else if (!ignore.contains(a.extension)) {
//                        // if extension is null, test every file
//                        // ignore these extensions
//                        if (it.extension == extension || extension == null) {
//                            if (!b.exists()) {
//                                if (testFile(a!!)) {
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "copying A to B")
//                                    if (!cp(a.path, b.path, true))
//                                        abort(preprocessor.base.globalVariables.depthAsString() + "failed to copy ${'$'}a to ${'$'}b")
//                                } else {
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "B does not exist however A does not contain DATA")
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "B cannot be deleted as it does not exist")
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "A will not be copied to B")
//                                }
//                            } else {
//                                if (!testFile(b)) {
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "B exists however does not contains DATA")
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting B")
//                                    delete(b)
//                                } else if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "B contains DATA")
//                            }
//                        }
//                    } else {
//                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "ignoring extension: ${'$'}{a.extension}")
//                        if (b.exists()) {
//                            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "A is ignoring but B exists, deleting B")
//                            delete(b)
//                        }
//                        val aPreProcessed = File(a.path + globalVariables.preprocessedExtension + "." + a.extension)
//                        val bPreProcessed = File(b.path + globalVariables.preprocessedExtension + "." + b.extension)
//                        if (aPreProcessed.exists()) {
//                            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting preprocessing file associated with A")
//                            delete(aPreProcessed)
//                        }
//                        if (bPreProcessed.exists()) {
//                            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting preprocessing file associated with B")
//                            delete(bPreProcessed)
//                        }
//                    }
//                }
//            } else {
//                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "A does not exist")
//            }
//        }
//    }
//
//    /**
//     * syncs directory **A** (**dir**) with directory **B** (**src**)
//     *
//     * this needs to be called AFTER [syncB] in order to sync correctly
//     * @see syncB
//     * @sample findSourceFiles
//     */
//    private fun syncA(dir: File, src: File, extension: String? = null) {
//        dir.listFiles().forEach {
//            val a = it
//            val b = File(
//                globalVariables.rootDirectory.toString() + '/' + a.toString().removePrefix(globalVariables.kppDir!!)
//            )
//            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "A : ${'$'}a")
//            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "B :     ${'$'}b")
//            run returnPoint@{
//                if (a.exists()) {
//                    if (a.isDirectory) {
//                        // ignore build dir in A
//                        // if build dir exists in B, delete it
//                        val blocked = a.toString() == globalVariables.projectDirectory?.path + "/build"
//                        if (!blocked) {
//                            if (b.path == globalVariables.kppDir) {
//                                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "error: B is kpp dir")
//                                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "kpp should not contain its own directory")
//                                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting A")
//                                deleteRecursive(a)
//                                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "returning to @returnPoint")
//                                return@returnPoint
//                            } else {
//                                if (b.exists()) {
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "entering ${'$'}a")
//                                    syncA(a, src, extension)
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "leaving ${'$'}a")
//                                    if (empty(a)) {
//                                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting A")
//                                        delete(a)
//                                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "returning to @returnPoint")
//                                        return@returnPoint
//                                    }
//                                } else {
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "B does not exist")
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting A")
//                                    deleteRecursive(a)
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "returning to @returnPoint")
//                                    return@returnPoint
//                                }
//                            }
//                        } else if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "A is blocked")
//                        if (b.exists()) {
//                            if (blocked) {
//                                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting A")
//                                deleteRecursive(a)
//                                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "returning to @returnPoint")
//                                return@returnPoint
//                            } else if (empty(a)) {
//                                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting A")
//                                delete(a)
//                                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "returning to @returnPoint")
//                                return@returnPoint
//                            }
//                        }
//                    } else if (a.isFile) {
//                        // if B does not exist, delete A
//                        if (!b.exists()) {
//                            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "B does not exist")
//                            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting A")
//                            delete(a)
//                            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "returning to @returnPoint")
//                            return@returnPoint
//                        }
//                        // if extension is null, test every file
//                        // ignore these extensions
//                        if (!ignore.contains(a.extension)) {
//                            if (it.extension == extension || extension == null) {
//                                if (!testFile(a!!)) {
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "A exists however does not contains DATA")
//                                    if (!a.path.endsWith(globalVariables.preprocessedExtension + "." + extension)) {
//                                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "moving A to B")
//                                        if (!mv(a.path, b.path, verbose = true, overwrite = true))
//                                            abort()
//                                    } else {
//                                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "A is preprocessor file")
//                                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "moving A to B (renamed)")
//                                        if (!mv(
//                                                a.path,
//                                                // Line is longer than allowed by code style (> 120 columns)
//                                                b.path.removeSuffix(
//                                                    "." +
//                                                            globalVariables.preprocessedExtension +
//                                                            "." +
//                                                            extension
//                                                ),
//                                                verbose = true,
//                                                overwrite = true
//                                            )
//                                        )
//                                            abort()
//                                    }
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "returning to @returnPoint")
//                                    return@returnPoint
//                                } else {
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "A contains DATA")
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "processing A")
//                                    if (a.extension == "") {
//                                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "error: cannot process a file with no extension")
//                                        return@returnPoint
//                                    }
//                                    macroList(globalVariables.kppMacroList)
//                                    process(a.path, a.extension, globalVariables.kppMacroList)
//                                    macroList(globalVariables.kppMacroList)
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "moving resulting preprocessing file A to B (renamed)")
//                                    if (!mv(
//                                            a.path + globalVariables.preprocessedExtension + "." + a.extension,
//                                            b.path,
//                                            verbose = true,
//                                            overwrite = true
//                                        )
//                                    )
//                                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "returning to @returnPoint")
//                                    return@returnPoint
//                                }
//                            }
//                        } else {
//                            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "ignoring extension: ${'$'}{a.extension}")
//                            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "returning to @returnPoint")
//                            return@returnPoint
//                        }
//                    }
//                } else {
//                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "A does not exist")
//                }
//            }
//        }
//    }
//
//    /**
//     * self explanatory
//     *
//     * this function finds and processes all source files in the directory **dir** with the extension **extension**
//     * @param dir the directory to search in
//     * @param extension the extension that each file must end
//     * @param extension if **null** then any file is accepted
//     * @sample findSourceFilesSample
//     */
//    @Suppress("MemberVisibilityCanBePrivate")
//    fun findSourceFiles(dir: File, extension: String? = null) {
//        // sync dir with kppDir
//        syncB(dir, globalVariables.kppDirAsFile as File, extension)
//        // sync kppDir with dir, calling process on a valid processing file
//        syncA(globalVariables.kppDirAsFile as File, dir, extension)
//    }
//
//
//    /**
//     * self explanatory
//     *
//     * this function finds and processes all source files in the directory **dir** with the extension **extension**
//     * @param dir the directory to search in
//     * @param extension the extension that each file must end
//     * @param extension if **null** then any file is accepted
//     * @sample findSourceFilesSample
//     */
//
//    @Suppress("MemberVisibilityCanBePrivate")
//    fun findSourceFilesOrNull(dir: File?, extension: String? = null) {
//        if (dir == null) abort(preprocessor.base.globalVariables.depthAsString() + "dir cannot be null")
//        findSourceFiles(dir, extension)
//    }
//
//    /**
//     * self explanatory
//     *
//     * this function finds and processes all source files in the directory **dir** with the extension **extension**
//     * @param dir the directory to search in
//     * @param extension the extension that each file must end
//     * @param extension if **null** then any file is accepted
//     * @sample findSourceFilesSample
//     */
//    @Suppress("MemberVisibilityCanBePrivate")
//    fun findSourceFiles(dir: String, extension: String? = null) {
//        findSourceFiles(File(dir), extension)
//    }
//
//    private fun findSourceFilesSample() {
//        val path =
//            globalVariables.projectDirectory.toString()
//        // find all source files with kotlin extension
//        findSourceFiles(path, "kt")
//        // find all source files, regardless of its extension
//        findSourceFiles(path)
//    }
//
//    /**
//     * test if file **src** contains any preprocessor directives
//     */
//    private fun testFile(src: File): Boolean {
//        globalVariables.currentFileContainsPreprocessor = false
//        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "testing file: ${'$'}{src.path}")
//        val lines: List<String> = Files.readAllLines(src.toPath())
//        lines.forEach { line ->
//            checkIfPreprocessorIsNeeded(line)
//        }
//        return globalVariables.currentFileContainsPreprocessor
//    }
//
//    private fun checkIfPreprocessorIsNeeded(line: String) {
//        if (line.trimStart().startsWith(preprocessor.core.Macro().Directives().value)) globalVariables.currentFileContainsPreprocessor = true
//    }
//}""",
                        listOf(
                            "class",
                            listOf(
                                "extensions",
                                "Comparable.kt", "linuxMain/kotlin/preprocessor/utils/class/extensions/Comparable.kt", """@file:Suppress("unused")

package preprocessor.utils.`class`.extensions

infix fun <T: Comparable<T>> T.isGreaterThan(i: T) = this > i
infix fun <T: Comparable<T>> T.isGreaterThanOrEqualTo(i: T) = this >= i
infix fun <T: Comparable<T>> T.isLessThan(i: T) = this < i
infix fun <T: Comparable<T>> T.isLessThanOrEqualTo(i: T) = this <= i
infix fun <T: Comparable<T>> T.isEqualTo(i: T) = this == i
infix fun <T: Comparable<T>> T.isNotEqualTo(i: T) = this != i
""",
                                "Boolean.kt", "linuxMain/kotlin/preprocessor/utils/class/extensions/Boolean.kt", """@file:Suppress("unused")

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
    println("x = ${'$'}x") // x = abc
    val y = x.ifTrue(x.startsWith('a')) {
        it.length
    }
    println("y = ${'$'}y") // y = 3
    println("x = ${'$'}x") // x = abc
    val yx = x.ifTrueReturn(x.startsWith('a')) {
        x = it.drop(1) // i want "it" to modify "x" itself
    }
    println("yx = ${'$'}yx") // yx = true
    println("x = ${'$'}x") // x = abc // should be "bc"
}
""",
                                "String.kt", "linuxMain/kotlin/preprocessor/utils/class/extensions/String.kt", """@file:Suppress("unused")

// find         : \{\n    return if \(this \=\= null\) null\n    else this\.(.*)\n\}
// replace with : = this\?\.${'$'}1


package preprocessor.utils.`class`.extensions

import preprocessor.core.Parser
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
    delimiters.forEach {str ->
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
fun String?.tokenizeVararg(vararg delimiters: String, returnDelimiters: Boolean = false): List<String>? = this?.tokenizeVararg(delimiters = *delimiters, returnDelimiters = returnDelimiters)
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
    val d = delimiters.toStack()
    d.forEach {str ->
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
fun String?.tokenize(delimiters: String, returnDelimiters: Boolean = false): List<String>? = this?.tokenize(delimiters, returnDelimiters)
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
    val parser = Parser(this) { it.toStack() }
    val t = parser.IsSequenceOneOrMany(token)
    while (parser.peek() != null) {
        if (t.peek()) {
            t.pop()
            str.append(token)
        }
        else str.append(parser.pop()!!)
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
    val parser = Parser(this) { it.toStack() }
    val t = parser.IsSequenceOneOrMany(token)
    while (parser.peek() != null) {
        if (t.peek()) {
            t.pop()
            str.append(replaceWith)
        }
        else str.append(parser.pop()!!)
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
    val parser = Parser(this) { it.toStack() }
    val t = parser.IsSequenceOnce(token)
    while (parser.peek() != null) {
        if (t.peek()) {
            t.pop()
            str.append(to)
        }
        else str.append(parser.pop()!!)
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
    val parser = Parser(this) { it.toStack() }
    val t = parser.IsSequenceOnce(token)
    while (parser.peek() != null) {
        if (t.peek()) {
            t.pop()
            str.append(if (!t.peek()) last else to)
        }
        else str.append(parser.pop()!!)
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
 * converts a [String] into a [Stack]
 * @see Stack.toStringConcat
 * @return the resulting conversion
 */
fun String.toStack(): Stack<String> {
    val deq = Stack<String>()
    var i = 0
    while (i < this.length) deq.addLast(this[i++].toString())
    return deq
}

/**
 * converts a [String] into a [Stack]
 * @see Stack.toStringConcat
 * @return the resulting conversion
 */
fun String?.toStack(): Stack<String>? {
    return if (this == null) return null
    else this.toStack()
}

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

fun String.padExtendEnd(to: Int, str: String) = if(to - this.length isGreaterThan 0) {
    val build = this.toStringBuilder(max(this.length, to));
    val m = str.lastIndex
    var i = 0
    while(build.length isLessThan to) {
        build.append(str[i])
        if (i isEqualTo m) i = 0 else i++
    }
    build.toString()
} else if(to - this.length isEqualTo 0) this else {
    this.padShrinkEnd(to, str)
}

fun String.padExtendEnd(to: Int, char: Char): String = this.padExtendEnd(to, char.toString())

fun String.padExtendEnd(to: Int): String = this.padExtendEnd(to, this[0])

fun String?.padExtendEnd(to: Int): String? = this?.padExtendEnd(to)

fun String?.padExtendEnd(to: Int, char: Char): String? = this?.padExtendEnd(to, char)

fun String.padExtendStart(to: Int, str: String): String = if(to - this.length isGreaterThan 0) {
    val build = this.reversed().toStringBuilder()
    val m = str.lastIndex
    var i = 0
    while(build.length isLessThan to) {
        build.append(str[i])
        if (i isEqualTo m) i = 0 else i++
    }
    build.reverse().toString()
} else if(to - this.length isEqualTo 0) this else {
    this.padShrinkStart(to, str)
}

fun String.padExtendStart(to: Int, char: Char): String = this.padExtendStart(to, char.toString())

fun String.padExtendStart(to: Int): String = this.padExtendStart(to, this[0])

fun String?.padExtendStart(to: Int): String? = this?.padExtendStart(to)

fun String?.padExtendStart(to: Int, char: Char): String? = this?.padExtendStart(to, char)

fun String.padShrinkEnd(to: Int, str: String): String = if(this.length - to isGreaterThan 0) {
    val thisLength = this.length
    val build = this.take(to).toStringBuilder()
    val m = str.lastIndex
    var i = 0
    while(build.length isLessThan thisLength) {
        build.append(str[i])
        if (i isEqualTo m) i = 0 else i++
    }
    build.toString()
} else if(this.length - to isEqualTo 0) this else {
    this.padExtendEnd(to, str)
}

fun String.padShrinkEnd(to: Int, char: Char): String = this.padShrinkEnd(to, char.toString())

fun String.padShrinkEnd(to: Int): String = this.padShrinkEnd(to, this[0])

fun String?.padShrinkEnd(to: Int): String? = this?.padShrinkEnd(to)

fun String?.padShrinkEnd(to: Int, char: Char): String? = this?.padShrinkEnd(to, char)

fun String.padShrinkStart(to: Int, str: String): String = if(this.length - to isGreaterThan 0) {
    val thisLength = this.length
    val build = this.reversed().take(to).toStringBuilder()
    val m = str.lastIndex
    var i = 0
    while(build.length isLessThan thisLength) {
        build.append(str[i])
        if (i isEqualTo m) i = 0 else i++
    }
    build.reverse().toString()
} else if(this.length - to isEqualTo 0) this else {
    this.padExtendStart(to, str)
}

fun String.padShrinkStart(to: Int, char: Char): String = this.padShrinkStart(to, char.toString())

fun String.padShrinkStart(to: Int): String = this.padShrinkStart(to, this[0])

fun String?.padShrinkStart(to: Int): String? = this?.padShrinkStart(to)

fun String?.padShrinkStart(to: Int, char: Char): String? = this?.padShrinkStart(to, char)
"""
                            )
                        ),
                        listOf(
                            "core",
                            "basename.kt", "linuxMain/kotlin/preprocessor/utils/core/basename.kt", """package preprocessor.utils.core

/**
 * returns the basename of a string, if the string is **null* then returns **null**
 */
fun basename(s: Any?): String? {
    return if (s == null || !s.toString().contains('/')) {
        null
    } else s.toString().substringAfterLast('/')
}
""",
                            "copy.kt", "linuxMain/kotlin/preprocessor/utils/core/copy.kt", """//package preprocessor.utils.core
//
//import java.io.File
//import java.io.IOException
//
///**
// * copy one file to another, optionally overwriting it
// * @return true if the operation succeeds, otherwise false
// * @see mv
// */
//fun cp(src: String, dest: String, verbose: Boolean = false, overwrite: Boolean = false): Boolean {
//    return try {
//        File(src).copyTo(File(dest), overwrite)
//        if (verbose) if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "${'$'}src -> ${'$'}dest")
//        true
//    } catch (e: IOException) {
//        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "failed to copy file ${'$'}src to ${'$'}dest")
//        false
//    }
//}
""",
                            "deleteRecursive.kt", "linuxMain/kotlin/preprocessor/utils/core/deleteRecursive.kt", """//package preprocessor.utils.core
//
//import java.io.File
//
///**
// * deletes **src** and all sub directories
// *
// * [abort]s on failure
// */
//fun deleteRecursive(src: File) {
//    if (!src.exists()) {
//        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deletion of ${'$'}{src.path} failed: file or directory does not exist")
//    }
//    if (!src.deleteRecursively()) {
//        abort(preprocessor.base.globalVariables.depthAsString() + "deletion of \"${'$'}{src.path}\" failed")
//    }
//}
""",
                            "empty.kt", "linuxMain/kotlin/preprocessor/utils/core/empty.kt", """//package preprocessor.utils.core
//
//import java.io.File
//
///**
// * returns true if **src** is an empty directory, otherwise returns false
// */
//fun empty(src: File): Boolean {
//    val files = src.listFiles() ?: return true
//    if (files.isEmpty()) return true
//    files.forEach {
//        if (it.isDirectory) return@empty empty(it)
//        else if (it.isFile) return@empty false
//        return@empty true
//    }
//    return false
//}
""",
                            "abort.kt", "linuxMain/kotlin/preprocessor/utils/core/abort.kt", """package preprocessor.utils.core

/**
 * a wrapper for Exception, Default message is **Aborted**
 *
 * if gradle is used, abort using the following
 *
 * import org.gradle.api.GradleException
 *
 * ...
 *
 * throw GradleException(e)
 */
fun abort(e: String = "Aborted"): Nothing {
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "Aborting with error: ${'$'}e")
    else println("Aborting with error: ${'$'}e")
    throw Exception(e).also {ex ->
        println("stack trace:").also {
            ex.printStackTrace()
        }
    }
}
""",
                            "move.kt", "linuxMain/kotlin/preprocessor/utils/core/move.kt", """//package preprocessor.utils.core
//
//import java.io.File
//import java.io.IOException
//
///**
// * moves one file to another, optionally overwriting it
// * @return true if the operation succeeds, otherwise false
// * @see cp
// */
//fun mv(src: String, dest: String, verbose: Boolean = false, overwrite: Boolean = false): Boolean {
//    return try {
//        File(src).copyTo(File(dest), overwrite)
//        if (verbose) if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "${'$'}src -> ${'$'}dest")
//        delete(File(src))
//        true
//    } catch (e: IOException) {
//        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "failed to move ${'$'}src to ${'$'}dest")
//        false
//    }
//}
""",
                            listOf(
                                "algorithms",
                                "LinkedList.kt", "linuxMain/kotlin/preprocessor/utils/core/algorithms/LinkedList.kt", """package preprocessor.utils.core.algorithms

class LinkedList<T> : kotlin.collections.Iterable<T?> {

    inner class Node<T>(value: T?){
        var value:T? = value
        var next: Node<T>? = null
        var previous:Node<T>? = null
    }
    private var head:Node<T>? = null
    fun isEmpty(): Boolean = head == null
    fun first(): Node<T>? = head
    fun last(): Node<T>? {
        var node = head
        if (node != null){
            while (node?.next != null) {
                node = node.next
            }
            return node
        } else {
            return null
        }
    }
    fun count():Int {
        var node = head
        if (node != null){
            var counter = 1
            while (node?.next != null){
                node = node.next
                counter += 1
            }
            return counter
        } else {
            return 0
        }
    }
    fun nodeAtIndex(index: Int) : Node<T>? {
        if (index >= 0) {
            var node = head
            var i = index
            while (node != null) {
                if (i == 0) return node
                i -= 1
                node = node.next
            }
        }
        return null
    }
    fun append(value: T?) {
        var newNode = Node(value)
        var lastNode = this.last()
        if (lastNode != null) {
            newNode.previous = lastNode
            lastNode.next = newNode
        } else {
            head = newNode
        }
    }
    fun appendLast(value: T?) {
        var newNode = Node(value)
        var lastNode = this.last()
        if (lastNode != null) {
            newNode.previous = newNode
            lastNode.next = lastNode
        } else {
            head = newNode
        }
    }
    fun removeNode(node: Node<T>):T? {
        val prev = node.previous
        val next = node.next
        if (prev != null) {
            prev.next = next
        } else {
            head = next
        }
        next?.previous = prev
        node.previous = null
        node.next = null
        return node.value
    }
    fun removeLast() : T? {
        val last = this.last()
        if (last != null) {
            return removeNode(last)
        } else {
            return null
        }
    }
    fun removeAtIndex(index: Int):T? {
        val node = nodeAtIndex(index)
        if (node != null) {
            return removeNode(node)
        } else {
            return null
        }
    }
    override fun toString(): String {
        var s = "["
        var node = head
        while (node != null) {
            s += "${'$'}{node.value}"
            node = node.next
            if (node != null) { s += ", " }
        }
        return s + "]"
    }

    fun contains(element: T): Boolean {
        var node = head
        while (node != null) {
            if (node.value == element) return true
            node = node.next
        }
        return false
    }

    override fun iterator(): kotlin.collections.Iterator<T?> {
        return object : kotlin.collections.Iterator<T?> {
            var node = head
            /**
             * Returns true if the iteration has more elements.
             *
             * @see next
             */
            override fun hasNext(): Boolean = node != null

            /**
             * Returns the next element in the iteration.
             *
             * NOTE: this always returns the **first** element when first called, not the **second** element
             *
             * @see hasNext
             */
            override fun next(): T? {
                if (node == null) throw NoSuchElementException()
                val var0 = node?.value
                node = node?.next
                return var0
            }
        }
    }
    /*
       fun clone(): LinkedList<T> {
            val tmp = LinkedList<T>()
            forEach { tmp.append(it) }
            return tmp
        }
     */
    fun clone(): LinkedList<T> = LinkedList<T>().also {l -> forEach { l.append(it) } }

    fun clear() { while (!isEmpty()) removeLast() }

    fun test() {
        val ll = LinkedList<String>()
        ll.append("John")
        println(ll)
        ll.append("Carl")
        println(ll)
        ll.append("Zack")
        println(ll)
        ll.append("Tim")
        println(ll)
        ll.append("Steve")
        println(ll)
        ll.append("Peter")
        println(ll)
        print("\n\n")
        println("first item: ${'$'}{ll.first()?.value}")
        println("last item: ${'$'}{ll.last()?.value}")
        println("second item: ${'$'}{ll.first()?.next?.value}")
        println("penultimate item: ${'$'}{ll.last()?.previous?.value}")
        println("\n4th item: ${'$'}{ll.nodeAtIndex(3)?.value}")
        println("\nthe list has ${'$'}{ll.count()} items")
    }
}""",
                                "Stack.kt", "linuxMain/kotlin/preprocessor/utils/core/algorithms/Stack.kt", """package preprocessor.utils.core.algorithms

@Suppress("unused")
class Stack<T>() {

    var stack = LinkedList<T>()
    var size = 0
    fun addLast(value: T) = stack.append(value).apply { size = stack.count() }
    fun addLast(list: List<T>) { list.iterator().also { while(it.hasNext()) this.addLast(it.next()) } }
    fun push(value: T) = stack.append(value).apply { size = stack.count() }
    fun peek(): T? = stack.first()?.value
    fun pop(): T? = when { stack.isEmpty() -> throw NoSuchElementException() ; else -> stack.removeAtIndex(0).apply { size = stack.count() } }
    fun contains(s: T?): Boolean = stack.contains(s)
    override fun toString(): String = stack.toString()
    /**
     * returns this stack as a string with each element appended to the end of the string
     */
    fun toStringConcat(): String {
        val result = StringBuilder()
        val dq = stack.iterator()
        while (dq.hasNext()) {
            result.append(dq.next())
        }
        return result.toString()
    }

    fun iterator() = stack.iterator()
    fun forEach(action: (T?) -> Unit) = stack.forEach(action)
    fun clear() = stack.clear().apply { size = stack.count() }
    fun clone() = Stack<T>().also { it.stack = stack.clone() }

    fun test() {
        val s = Stack<String>()
        s.push("John")
        println(s)
        s.push("Carl")
        println(s)
        println("peek item: ${'$'}{s.peek()}")
        println("pop item: ${'$'}{s.pop()}")
        println("peek item: ${'$'}{s.peek()}")
    }
}
"""
                            ),
                            "realloc.kt", "linuxMain/kotlin/preprocessor/utils/core/realloc.kt", """@file:Suppress("unused")
package preprocessor.utils.core

import preprocessor.core.Macro

// NOT AVAILABLE IN KOTLIN-NATIVE
//
//import preprocessor.utils.core.classTools.instanceChain
//import preprocessor.utils.core.classTools.chain
//
//
///**
// * reallocates a [MutableList][kotlin.collections.MutableList] to the specified size
// *
// * this attempts to handle all types supported by [MutableList][kotlin.collections.MutableList] in the
// * initialization of a new element
// *
// * uses unsigned integers that are experimental: [UByte], [UInt], [ULong], [UShort]
// * @param v the list to resize
// * @param size the desired size to resize to
// * @sample reallocTest
// */
//@UseExperimental(ExperimentalUnsignedTypes::class)
//@Suppress("UNCHECKED_CAST")
//fun <E> realloc(v: kotlin.collections.MutableList<E?>, a : Any?, size: Int, isNullable: Boolean = true) {
      // the first parameter <E> (in which E is just the name, like a MACRO) is inferred from the return type of whatever it is invoked on
//    while (v.size != size) {
//        if (size > v.size) {
//            v.add(
//                run {
//                    if (a!!::class.javaPrimitiveType != null) {
//                        if (isNullable) null
//                        /** copied from
//                         * /.../kotlin-stdlib-1.3.21-sources.jar!/kotlin/Primitives.kt
//                         * /.../kotlin-stdlib-1.3.21-sources.jar!/kotlin/Boolean.kt
//                         *
//                         * unsigned integers are experimental: [UByte], [UInt], [ULong], [UShort]
//                         */
//                        else when (a!!) {
//                            is Byte -> 0
//                            is UByte -> 0U
//                            is Short -> 0
//                            is UShort -> 0U
//                            is Int -> 0
//                            is UInt -> 0U
//                            is Long -> 0L
//                            is ULong -> 0UL
//                            is Float -> 0.0F
//                            is Double -> 0.0
//                            is Char -> java.lang.Character.MIN_VALUE // null ('\0') as char
//                            is Boolean -> false
//                            else -> abort(preprocessor.base.globalVariables.depthAsString() + "unknown non-nullable type: ${'$'}{a!!::class.javaPrimitiveType}")
//                        }
//                    } else {
//                        /*
//                        `::class.isInner` does not work with a Security Manager
//                        Exception in thread "main" java.lang.IllegalStateException: No BuiltInsLoader implementation was
//                        found. Please ensure that the META-INF/services/ is not stripped from your application and that
//                        the Java virtual machine is not running under a security manager
//                        */
//                        if (a!!::class.isInner) instanceChain(chain(a!!))
//                        else a!!::class.java.newInstance()
//                    }
//                } as E
//            )
//        } else {
//            v.remove(v.last())
//        }
//    }
//}
//
///**
// * reallocates a [MutableList][kotlin.collections.MutableList] to the specified size
// *
// * this attempts to handle all types supported by [MutableList][kotlin.collections.MutableList] in the
// * initialization of a new element
// *
// *  uses unsigned integers that are experimental: [UByte], [UInt], [ULong], [UShort]
// * @param v the list to resize
// * @param size the desired size to resize to
// * @sample reallocTest
// */
//@UseExperimental(ExperimentalUnsignedTypes::class)
//@Suppress("UNCHECKED_CAST")
//fun <E> realloc(v: kotlin.collections.MutableList<E>, size: Int): Unit = realloc(
//    v = v as kotlin.collections.MutableList<E?>,
//    a = v[0],
//    size = size,
//    isNullable = false
//)
//
///**
// * @see reallocTest
// */
//private class A {
//    inner class B {
//        inner class C {
//            var empty: Int = 0
//        }
//
//        var empty: Int = 0
//        var a: MutableList<C>
//
//        init {
//            a = mutableListOf(C())
//        }
//    }
//
//    var a: MutableList<B>
//
//    init {
//        a = mutableListOf(B())
//    }
//
//    /**
//     * test variable
//     */
//    var empty: Int = 0
//}
//
//fun reallocTest() {
//    val f = mutableListOf<A>()
//    val ff = mutableListOf<Int>()
//    val fff = mutableListOf<Double?>()
//    f.add(A()); f[0].empty = 5
//    ff.add(5)
//    fff.add(5.5)
//
//    realloc(f, 5)
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "f[0].empty = ${'$'}{f[0].empty}")
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "f[4].empty = ${'$'}{f[4].empty}")
//
//    f[0].a[0].empty = 88
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "f[0].a[0].empty = ${'$'}{f[0].a[0].empty}")
//    f[4].a[0].empty = 88
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "f[4].a[0].empty = ${'$'}{f[4].a[0].empty}")
//
//    realloc(f[0].a, 5)
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "f[0].a[4].empty = ${'$'}{f[0].a[4].empty}")
//    realloc(f[4].a, 5)
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "f[4].a[4].empty = ${'$'}{f[4].a[4].empty}")
//
//    realloc(ff, 5)
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "ff[0] = ${'$'}{ff[0]}")
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "ff[4] = ${'$'}{ff[4]}")
//    realloc(fff, 5)
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "fff[0] = ${'$'}{fff[0]}")
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "fff[4] = ${'$'}{fff[4]}")
//    abort()
//}

fun realloc(m: MutableList<Macro.MacroInternal>, newSize: Int) {
    m.add(Macro().MacroInternal())
//    m[0].size = newSize
}

fun realloc(m: MutableList<Macro>, newSize: Int) {
    m.add(Macro())
//    m[0].size = newSize
}""",
                            "delete.kt", "linuxMain/kotlin/preprocessor/utils/core/delete.kt", """//package preprocessor.utils.core
//
//import java.io.File
//
///**
// * deletes **src**
// *
// * [abort]s on failure
// */
//fun delete(src: File) {
//    if (!src.exists()) {
//        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deletion of ${'$'}{src.path} failed: file or directory does not exist")
//    }
//    if (!src.delete()) {
//        abort(preprocessor.base.globalVariables.depthAsString() + "deletion of \"${'$'}{src.path}\" failed")
//    }
//}
""",
                            listOf(
                                "classTools",
                                "getDeclaringUpperLevelClassObject.kt", "linuxMain/kotlin/preprocessor/utils/core/classTools/getDeclaringUpperLevelClassObject.kt", """//package preprocessor.utils.core.classTools
//
///**
// * obtains the parent class of [objectA]
// *
// * @params objectA the current class
// * @return **null** if [objectA] is **null**
// *
// * [objectA] if [objectA] is a top level class
// *
// * otherwise the parent class of [objectA]
// */
//fun getDeclaringUpperLevelClassObject(objectA: Any?): Any? {
//    if (objectA == null) {
//        return null
//    }
//    val cls = objectA.javaClass ?: return objectA
//    val outerCls = cls.enclosingClass
//        ?: // this is top-level class
//        return objectA
//    // get outer class object
//    var outerObj: Any? = null
//    try {
//        val fields = cls.declaredFields
//        for (field in fields) {
//            if (field != null && field.type === outerCls
//                && field.name != null && field.name.startsWith("this${'$'}")
//            ) {
//                /*
//                `field.isAccessible = true` does not work with a Security Manager
//                java.security.AccessControlException: access denied
//                ("java.lang.reflect.ReflectPermission" "suppressAccessChecks")
//                */
//                field.isAccessible = true
//                outerObj = field.get(objectA)
//                break
//            }
//        }
//    } catch (e: Exception) {
//        e.printStackTrace()
//    }
//
//    return outerObj
//}
""",
                                "instanceChain.kt", "linuxMain/kotlin/preprocessor/utils/core/classTools/instanceChain.kt", """//package preprocessor.utils.core.classTools
//
///**
// * creates and returns an instance of the class given as the parameter to
// * [chain][preprocessor.utils.core.classTools.chain]
// *
// * this class can be outer or inner
// * @param chain the current chain, this must originate from a list returned
// *
// * by [chain][preprocessor.utils.core.classTools.chain]
// * @param index used internally to traverse the [chain]
// * @param debug if true, debug output will be printed
// * @sample instanceChainSample
// * @see preprocessor.utils.core.classTools.chain
// */
//fun instanceChain(chain: MutableList<Any>, index: Int = chain.lastIndex, debug: Boolean = false): Any {
//    return if (index == 0) {
//        chain[index]
//    } else {
//        if (debug) if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "chain[${'$'}index] = " + chain[index])
//        val outer = chain[index]
//        val toRun = Class.forName(chain[index].javaClass.name + "${'$'}" + chain[index - 1].javaClass.simpleName)
//        val ctor = toRun.getDeclaredConstructor(chain[index]::class.java)
//        val lowerCInstance = ctor.newInstance(outer)
//        if (debug) if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "lowerCInstance = " + lowerCInstance!!::class.java)
//        if (index == 1) lowerCInstance
//        else instanceChain(
//            chain = chain,
//            index = index - 1,
//            debug = debug
//        )
//    }
//}
//
//private fun instanceChainSample() {
//    class A {
//        inner class B {
//            inner class C {
//                var empty: Int = 0
//            }
//
//            var empty: Int = 0
//            var a: MutableList<C>
//
//            init {
//                a = mutableListOf(C())
//            }
//        }
//
//        var a: MutableList<B>
//
//        init {
//            a = mutableListOf(B())
//        }
//
//        var empty: Int = 0
//    }
//
//    val f = mutableListOf<A>()
//    f.add(A()) // this is required, as i do not know how to do accomplish this in the init block
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "f[0] = ${'$'}{instanceChain(chain(f[0]))}")
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "f[0].a[0] = ${'$'}{instanceChain(chain(f[0].a[0]))}")
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "f[0].a[0].a[0] = ${'$'}{instanceChain(chain(f[0].a[0].a[0]))}")
//}""",
                                "chain.kt", "linuxMain/kotlin/preprocessor/utils/core/classTools/chain.kt", """//package preprocessor.utils.core.classTools
//
//import preprocessor.utils.core.abort
//
///**
// * returns a [MutableList] of classes parenting the current class
// *
// * the top most class is always the last index
// *
// * the last class is always the first index
// *
// * for example: `up(f[0].a[0].a[0])` returns 3 indexes (0, 1, and 2) consisting of the following:
// *
// * index 0 = `f[0].a[0].a[0]`
// *
// * index 1 = `f[0].a[0]`
// *
// * index 2 = `f[0]`
// *
// * @param a the current class
// * @param m used internally to build up a list of classes
// * @param debug if true, debug output will be printed
// * @see getDeclaringUpperLevelClassObject
// */
//fun chain(a: Any, m: MutableList<Any> = mutableListOf(), debug: Boolean = false): MutableList<Any> {
//    m.add(a)
//    if (debug) if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "get upper class of ${'$'}{a.javaClass.name}")
//    val upperC = getDeclaringUpperLevelClassObject(a) ?: abort(preprocessor.base.globalVariables.depthAsString() + "upperC is null o.o")
//    return if (a == upperC) m
//    else chain(upperC, m)
//}
"""
                            )
                        ),
                        listOf(
                            "extra",
                            "Balanced.kt", "linuxMain/kotlin/preprocessor/utils/extra/Balanced.kt", """package preprocessor.utils.extra

/**
 *
 * a class for detecting balanced brackets
 *
 * cant be bothered documenting this
 *
 * modified from the original rosetta code in the **See Also**
 *
 * @see <a href="https://rosettacode.org/wiki/Balanced_brackets#Kotlin">Balanced Brackets</a>
 */
@Suppress("MemberVisibilityCanBePrivate")
class Balanced {
    /**
     *
     */
    class BalanceList {
        /**
         *
         */
        var l: MutableList<Char> = mutableListOf()
        /**
         *
         */
        var r: MutableList<Char> = mutableListOf()

        /**
         *
         */
        fun addPair(l: Char, r: Char) {
            this.l.add(l)
            this.r.add(r)
        }
    }

    /**
     *
     */
    var start: MutableList<Int> = mutableListOf()
    /**
     *
     */
    var end: MutableList<Int> = mutableListOf()
    /**
     *
     */
    var index: Int = 0
    /**
     *
     */
    var countLeft: Int = 0  // number of left brackets so far unmatched
    /**
     *
     */
    var splitterCount: Int = 0
    /**
     *
     */
    var splitterLocation: MutableList<Int> = mutableListOf()
    /**
     *
     */
    var lastRegisteredLeftHandSideBalancer: Char = ' '
    /**
     *
     */
    var lastRegisteredRightHandSideBalancer: Char = ' '
    /**
     *
     */
    var lastCheckString: String = ""

    /**
     *
     */
    fun isBalanced(s: String, balancerLeft: Char, balancerRight: Char): Boolean {
        lastCheckString = s
        lastRegisteredLeftHandSideBalancer = balancerLeft
        lastRegisteredRightHandSideBalancer = balancerRight
        start
        end
        if (s.isEmpty()) return true
        for (c in s) {
            if (c == lastRegisteredLeftHandSideBalancer) {
                countLeft++
                if (countLeft == 1) start.add(index)
            } else if (c == lastRegisteredRightHandSideBalancer) {
                if (countLeft == 1) end.add(index + 1)
                if (countLeft > 0) countLeft--
                else return false
            }
            index++
        }
        return countLeft == 0
    }

    /**
     *
     */
    fun isBalancerR(c: Char, balance: BalanceList): Boolean {
        balance.r.forEach {
            if (c == it) return true
        }
        return false
    }

    /**
     *
     */
    fun isBalancerL(c: Char, balance: BalanceList): Boolean {
        balance.l.forEach {
            if (c == it) return true
        }
        return false
    }

    /**
     *
     */
    fun containsL(c: String, balance: BalanceList): Boolean {
        balance.l.forEach {
            if (c.contains(it)) return true
        }
        return false
    }

    /**
     *
     */
    fun containsR(c: String, balance: BalanceList): Boolean {
        balance.r.forEach {
            if (c.contains(it)) return true
        }
        return false
    }

    /**
     *
     */
    fun isBalancedSplit(s: String, balancer: BalanceList, Splitter: Char): Boolean {
        lastCheckString = s
        lastRegisteredLeftHandSideBalancer = balancer.l[balancer.l.lastIndex]
        lastRegisteredRightHandSideBalancer = balancer.r[balancer.r.lastIndex]
        if (s.isEmpty()) return true
        for (c in s) {
            if (countLeft == 0) if (c == Splitter) {
                splitterCount++
                splitterLocation.add(index)
            }
            if (isBalancerL(c, balancer)) {
                countLeft++
                if (countLeft == 1) start.add(index)
            } else if (isBalancerR(c, balancer)) {
                if (countLeft == 1) end.add(index + 1)
                if (countLeft > 0) countLeft--
                else return false
            }
            index++
        }
        return countLeft == 0
    }

    /**
     *
     */
    fun extractText(text: String): String {
        if (isBalanced(text, '(', ')')) {
            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "text : " + text.substring(start[0], end[0]))
            return text.substring(start[0], end[0])
        }
        return text
    }

    /**
     *
     */
    fun info() {
        if (preprocessor.base.globalVariables.flags.debug) {
            println(preprocessor.base.globalVariables.depthAsString() + "last check string  = ${'$'}lastCheckString")
            println(preprocessor.base.globalVariables.depthAsString() + "left balancer      = ${'$'}lastRegisteredLeftHandSideBalancer")
            println(preprocessor.base.globalVariables.depthAsString() + "right balancer     = ${'$'}lastRegisteredRightHandSideBalancer")
            println(preprocessor.base.globalVariables.depthAsString() + "start index        = ${'$'}start")
            println(preprocessor.base.globalVariables.depthAsString() + "end index          = ${'$'}end")
            println(preprocessor.base.globalVariables.depthAsString() + "current index       = ${'$'}index")
            println(preprocessor.base.globalVariables.depthAsString() + "unmatched brackets = ${'$'}countLeft")
            println(preprocessor.base.globalVariables.depthAsString() + "splitter count     = ${'$'}splitterCount")
            println(preprocessor.base.globalVariables.depthAsString() + "splitter location  = ${'$'}splitterLocation")
        }
    }
}
""",
                            "expand.kt", "linuxMain/kotlin/preprocessor/utils/extra/expand.kt", """package preprocessor.utils.extra

import preprocessor.base.globalVariables
import preprocessor.core.*
import preprocessor.utils.core.abort
import preprocessor.utils.`class`.extensions.collapse
import preprocessor.utils.`class`.extensions.toByteArray
import preprocessor.utils.core.algorithms.Stack
import kotlin.collections.MutableList

fun expecting(what: String, closeValue: String): String {
    return "expecting close value of ${'$'}closeValue for ${'$'}what >"
}

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
    c: Boolean = false,
    newlineFunction :(
        (String) -> String
    )?
): String? {
    val dm = 15
    if (depth > dm) abort("depth exceeded ${'$'}dm")
    preprocessor.base.globalVariables.depth = depth
    var stringize = s
    var concat = c
    if (preprocessor.base.globalVariables.flags.debug) {
        println(preprocessor.base.globalVariables.depthAsString() + "PARAMETERS AT FUNCTION CALL START")
        println(
            preprocessor.base.globalVariables.depthAsString() + "expanding '${'$'}{
            lex.currentLine?.replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())}'"
        )
        println(
            preprocessor.base.globalVariables.depthAsString() + "tokenSequence = ${'$'}{tokenSequence.toStringAsArray()
                .replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())}"
        )
        println(
            preprocessor.base.globalVariables.depthAsString() + "ARG = ${'$'}{ARG.toString()
                .replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())}"
        )
        println(preprocessor.base.globalVariables.depthAsString() + "blacklist = ${'$'}blacklist")
        println(preprocessor.base.globalVariables.depthAsString() + "expanding    : ${'$'}expanding")
        println(preprocessor.base.globalVariables.depthAsString() + "expandingType: ${'$'}expandingType")
        println(preprocessor.base.globalVariables.depthAsString() + "originalExpanding: ${'$'}originalExpanding")
        println(preprocessor.base.globalVariables.depthAsString() + "originalExpandingType: ${'$'}originalExpandingType")
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
            if (preprocessor.base.globalVariables.flags.debug) println(
                preprocessor.base.globalVariables.depthAsString() + "clearing comment token '${'$'}{tokenSequence
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
                    if (lex.currentLine == null) {
                        if (newlineFunction == null) abort(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "no more lines when expecting more lines, unterminated block comment"
                        )
                        else lex.currentLine = newlineFunction(expecting("block comment", "*/"))
                    }
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
            if (preprocessor.base.globalVariables.flags.debug) println(
                preprocessor.base.globalVariables.depthAsString() +
                        "popping double string token '${'$'}doubleString'"
            )
            expansion.append(doubleString.toString())
            doubleString.pop() // pop the first "
            var iterations = 0
            val maxIterations = 1000
            while (iterations <= maxIterations) {
                if (tokenSequence.peek() == null) {
                    lex.lex()
                    // Line is longer than allowed by code style (> 120 columns)
                    if (lex.currentLine == null) {
                        if (newlineFunction == null) abort(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "no more lines when expecting more lines, unterminated double string"
                        )
                        else lex.currentLine = newlineFunction(expecting("double string", "\""))
                    }
                    tokenSequence.tokenList = parserPrep(lex.currentLine as String)
                }
                if (newline.peek()) {
                    if (preprocessor.base.globalVariables.flags.debug) println(
                        preprocessor.base.globalVariables.depthAsString() + "popping newline token '${'$'}{
                        newline
                            .toString()
                            .replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())
                        }'"
                    )
                    newline.pop()
                } else if (doubleString.peek()) {
                    if (preprocessor.base.globalVariables.flags.debug) println(
                        preprocessor.base.globalVariables.depthAsString() +
                                "popping double string token '${'$'}doubleString'"
                    )
                    expansion.append(doubleString.toString())
                    doubleString.pop()
                    break
                } else if (backslash.peek()) {
                    if (preprocessor.base.globalVariables.flags.debug) println(
                        preprocessor.base.globalVariables.depthAsString() +
                                "popping backslash token '${'$'}backslash'"
                    )
                    expansion.append(backslash.toString())
                    backslash.pop()
                    if (doubleString.peek()) {
                        if (preprocessor.base.globalVariables.flags.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "popping double string token '${'$'}doubleString'"
                        )
                        expansion.append(doubleString.toString())
                        doubleString.pop()
                    } else {
                        if (preprocessor.base.globalVariables.flags.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "unknown backslash sequence '${'$'}{tokenSequence.peek()}'"
                        )
                        expansion.append(tokenSequence.pop()!!)
                    }
                } else {
                    val popped = tokenSequence.pop()
                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "popping normal token '${'$'}popped'")
                    expansion.append(popped)
                }
                iterations++
            }
            if (iterations > maxIterations) abort(
                preprocessor.base.globalVariables.depthAsString() + "iterations expired"
            )
        } else if (singleString.peek()) {
            if (preprocessor.base.globalVariables.flags.debug) println(
                preprocessor.base.globalVariables.depthAsString() +
                        "popping single string token '${'$'}singleString'"
            )
            expansion.append(singleString.toString())
            singleString.pop() // pop the first '
            var iterations = 0
            val maxIterations = 1000
            while (iterations <= maxIterations) {
                if (tokenSequence.peek() == null) {
                    lex.lex()
                    // Line is longer than allowed by code style (> 120 columns)
                    if (lex.currentLine == null) {
                        if (newlineFunction == null) abort(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "no more lines when expecting more lines, unterminated single string"
                        )
                        else lex.currentLine = newlineFunction(expecting("single string", "'"))
                    }
                    tokenSequence.tokenList = parserPrep(lex.currentLine as String)
                }
                if (newline.peek()) {
                    if (preprocessor.base.globalVariables.flags.debug) println(
                        preprocessor.base.globalVariables.depthAsString() + "popping newline token '${'$'}{
                        newline
                            .toString()
                            .replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())
                        }'"
                    )
                    newline.pop()
                } else if (singleString.peek()) {
                    if (preprocessor.base.globalVariables.flags.debug) println(
                        preprocessor.base.globalVariables.depthAsString() +
                                "popping single string token '${'$'}singleString'"
                    )
                    expansion.append(singleString.toString())
                    singleString.pop()
                    break
                } else if (backslash.peek()) {
                    if (preprocessor.base.globalVariables.flags.debug) println(
                        preprocessor.base.globalVariables.depthAsString() +
                                "popping backslash token '${'$'}backslash'"
                    )
                    expansion.append(backslash.toString())
                    backslash.pop()
                    if (singleString.peek()) {
                        if (preprocessor.base.globalVariables.flags.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "popping single string token '${'$'}singleString'"
                        )
                        expansion.append(singleString.toString())
                        singleString.pop()
                    } else {
                        if (preprocessor.base.globalVariables.flags.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "unknown backslash sequence '${'$'}{tokenSequence.peek()}'"
                        )
                        expansion.append(tokenSequence.pop()!!)
                    }
                } else {
                    val popped = tokenSequence.pop()
                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "popping normal token '${'$'}popped'")
                    expansion.append(popped)
                }
                iterations++
            }
            if (iterations > maxIterations) abort(
                preprocessor.base.globalVariables.depthAsString() + "iterations expired"
            )
        } else if (emptyParenthesis.peek()) {
            if (preprocessor.base.globalVariables.flags.debug) println(
                preprocessor.base.globalVariables.depthAsString() +
                        "popping empty parenthesis token '${'$'}emptyParenthesis'"
            )
            expansion.append(emptyParenthesis.toString())
            emptyParenthesis.pop()
        } else if (newline.peek()) {
            if (preprocessor.base.globalVariables.flags.debug) println(
                preprocessor.base.globalVariables.depthAsString() + "popping newline token '${'$'}{
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
                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "popping space token '${'$'}space'")
                space.pop()
                expansion.append(" ")
            }
            if (directive.peek()) {
                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "popping directive token '${'$'}directive'")
                directive.pop()

                if (stringize) {
                    stringize = false
                    concat = true
                } else stringize = true

                if (space.peek()) {
                    stringize = false
                    concat = false
                    // case 1, space at start of file followed by define
                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "popping space token '${'$'}space'")
                    space.pop()
                }
                if (abortDef.peek()) abort("#abort found")
                if (ignoreDef.peek()) return null
                if (defineDef.peek()) {
                    stringize = false
                    concat = false
                    // case 2, define at start of line
                    if (preprocessor.base.globalVariables.flags.debug) println(
                        preprocessor.base.globalVariables.depthAsString() +
                                "popping ${'$'}{Macro().Directives().Define().value} statement '${'$'}{tokenSequence
                                    .toString().replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())
                                }'"
                    )
                    processDefine("${'$'}{Macro().Directives().value}${'$'}tokenSequence", macro)
                    tokenSequence.clear()
                }
            }
        } else if (space.peek()) {
            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "popping space token '${'$'}space'")
            space.pop()
            expansion.append(" ")
        } else {
            val index = macro.size - 1
            val ss = tokenSequence.peek()
            val name: String
            if (ss == null) abort(preprocessor.base.globalVariables.depthAsString() + "something is wrong")
            name = ss
            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "popping normal token '${'$'}name'")
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
                if (globalVariables.status.currentMacroExists) {
                    macroFunctionExists = true
                }
                macroObjectIndex = macroExists(
                    name,
                    Macro().Directives().Define().Types().Object,
                    index,
                    macro
                )
                if (globalVariables.status.currentMacroExists) {
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

                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "looking ahead")
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
                        if (globalVariables.status.currentMacroExists) skip = true
                    }
                    if (macroFunctionExists && !skip) {
                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "'${'$'}name' is a function")
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
                            index = index,
                            newlineFunction = newlineFunction
                        )
                        if (preprocessor.base.globalVariables.flags.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "eFS1R.argv = ${'$'}{eFS1R.argv}"
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
                            replacementList = macro[index].macros[eFS1R.macroTypeDependantIndex].replacementList,
                            newlineFunction = newlineFunction
                        )
                        // 3) rescan, if the original macro appears again in the output, it isn't expanded any further
                        if (preprocessor.base.globalVariables.flags.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "rescanning, ARG = ${'$'}ARG"
                        )
                        if (preprocessor.base.globalVariables.flags.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "eFS1R.argv = ${'$'}{eFS1R.argv}"
                        )
                        val eX = if (originalExpanding != null) originalExpanding
                        else if (eFS1R.argv.isNotEmpty()) name
                        else null
                        val eXT = if (originalExpandingType != null) originalExpandingType
                        else if (eFS1R.argv.isNotEmpty()) Macro().Directives().Define().Types().Function
                        else null
                        if (preprocessor.base.globalVariables.flags.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "eX = ${'$'}eX"
                        )
                        if (preprocessor.base.globalVariables.flags.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "eXT = ${'$'}eXT"
                        )
                        if (preprocessor.base.globalVariables.flags.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "blacklisting ${'$'}name"
                        )
                        if (macroUnexpanded != null) {
                            if (preprocessor.base.globalVariables.flags.debug) println(
                                preprocessor.base.globalVariables.depthAsString() +
                                        "macroUnexpanded " +
                                        "${'$'}{macroUnexpanded[index].macros[eFS1R.macroTypeDependantIndex].identifier} of type " +
                                        "${'$'}{macroUnexpanded[index].macros[eFS1R.macroTypeDependantIndex].type} has value " +
                                        "${'$'}{macroUnexpanded[index].macros[eFS1R.macroTypeDependantIndex].replacementList
                                            ?.replace(
                                                "\n",
                                                "\n" + preprocessor.base.globalVariables.depthAsString()
                                            )}"
                            )
                            abort("1")
                        }
                        if (preprocessor.base.globalVariables.flags.debug) {
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
                            newlineFunction = newlineFunction,
                            string = e1!!
                        )

                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() +
                                "expansion = '${'$'}{expansion.toString()}'")
                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() +
                                "e2 = '${'$'}e2'")

                        // 4) put the result of substitution back into the source code
                        if (e2 != null) expansion.append(e2)
                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() +
                                "expansion = '${'$'}{expansion.toString()}'")
                    } else {
                        if (preprocessor.base.globalVariables.flags.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "'${'$'}name' is a function but no associated function macro exists"
                        )
                        expansion.append(name)
                    }
                } else {
                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "'${'$'}name' is an object")
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
                            if (preprocessor.base.globalVariables.flags.debug) println(
                                preprocessor.base.globalVariables.depthAsString() +
                                        "but it is currently being expanded"
                            )
                            expansion.append(name)
                        } else {
                            if (preprocessor.base.globalVariables.flags.debug) println(
                                preprocessor.base.globalVariables.depthAsString() +
                                        "and is not currently being expanded"
                            )

                            val macroTypeDependantIndex = macroObjectIndex
                            // Line is longer than allowed by code style (> 120 columns)
                            if (preprocessor.base.globalVariables.flags.debug) println(
                                preprocessor.base.globalVariables.depthAsString() +
                                        "${'$'}{macro[index].macros[macroTypeDependantIndex].identifier} of type " +
                                        "${'$'}{macro[index].macros[macroTypeDependantIndex].type} has value " +
                                        "${'$'}{macro[index].macros[macroTypeDependantIndex].replacementList
                                            ?.replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())}"
                            )
                            if (stringize) {
                                if (macroUnexpanded != null) {
                                    if (preprocessor.base.globalVariables.flags.debug) println(
                                        preprocessor.base.globalVariables.depthAsString() +
                                                "macroUnexpanded " +
                                                "${'$'}{macroUnexpanded[index].macros[macroTypeDependantIndex].identifier} of type " +
                                                "${'$'}{macroUnexpanded[index].macros[macroTypeDependantIndex].type} has value " +
                                                "${'$'}{macroUnexpanded[index].macros[macroTypeDependantIndex].replacementList
                                                    ?.replace(
                                                        "\n",
                                                        "\n" + preprocessor.base.globalVariables.depthAsString()
                                                    )}"
                                    )
                                    expansion.append(
                                        "\"${'$'}{macroUnexpanded[index].macros[macroTypeDependantIndex].replacementList
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
                                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "ARG = ${'$'}ARG")
                                        if (!ARG.contains(name)) {
                                            if (preprocessor.base.globalVariables.flags.debug) println(
                                                preprocessor.base.globalVariables.depthAsString() +
                                                        "blacklisting ${'$'}name"
                                            )
                                            blacklist.add(name)
                                        } else {
                                            if (preprocessor.base.globalVariables.flags.debug) println(
                                                preprocessor.base.globalVariables.depthAsString() +
                                                        "${'$'}name is an argument"
                                            )
                                        }
                                    } else {
                                        if (preprocessor.base.globalVariables.flags.debug) println(
                                            preprocessor.base.globalVariables.depthAsString() +
                                                    "warning: ARG is null"
                                        )
                                        if (preprocessor.base.globalVariables.flags.debug) println(
                                            preprocessor.base.globalVariables.depthAsString() +
                                                    "blacklisting ${'$'}name"
                                        )
                                        blacklist.add(name)
                                    }
                                    val parser =
                                        Parser(lex.currentLine as String) { parserPrep(it) }
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
                                        c = concat,
                                        newlineFunction = newlineFunction
                                    )!!
                                    preprocessor.base.globalVariables.depth = depth
                                    if (preprocessor.base.globalVariables.flags.debug) println(
                                        preprocessor.base.globalVariables.depthAsString() +
                                                "macro Object expansion ${'$'}name returned ${'$'}e"
                                    )
                                    if (stringize) stringize = false
                                    if (concat) concat = false
                                    expansion.append(e)
                                }
                            }
                        }
                    } else {
                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "but does not exist as a macro")
                        expansion.append(name)
                    }
                }
            } else expansion.append(tokenSequence.pop()!!)
        }
        iterations++
    }
    if (iterations > maxIterations) if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "iterations expired")
    if (preprocessor.base.globalVariables.flags.debug) {
        println(preprocessor.base.globalVariables.depthAsString() + "expansion = ${'$'}expansion")
        println(preprocessor.base.globalVariables.depthAsString() + "PARAMETERS AT FUNCTION CALL END")
        println(preprocessor.base.globalVariables.depthAsString() + "expanding '${'$'}{lex.currentLine?.replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())}'")
        println(preprocessor.base.globalVariables.depthAsString() + "tokenSequence = ${'$'}{tokenSequence.toStringAsArray()}")
        println(preprocessor.base.globalVariables.depthAsString() + "ARG = ${'$'}ARG")
        println(preprocessor.base.globalVariables.depthAsString() + "blacklist = ${'$'}blacklist")
        println(preprocessor.base.globalVariables.depthAsString() + "expanding    : ${'$'}expanding")
        println(preprocessor.base.globalVariables.depthAsString() + "expandingType: ${'$'}expandingType")
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
    index: Int,
    newlineFunction :(
        (String) -> String
    )?
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
        if (if (newline.peek()) {
                newline.pop()
                true
            } else tokenSequence.peek() == null
        ) {
            if (tokenSequence.peek() == null) {
                if (preprocessor.base.globalVariables.flags.debug) println(
                    preprocessor.base.globalVariables.depthAsString() +
                            "ran out of tokens, grabbing more tokens from the next line"
                )
                lex.lex()
                if (lex.currentLine == null) {
                    if (newlineFunction == null) abort(
                        preprocessor.base.globalVariables.depthAsString() +
                                "no more lines when expecting more lines, unterminated parenthesis"
                    )
                    else lex.currentLine = newlineFunction(expecting("parenthesis", ")"))
                }
                tokenSequence.tokenList = parserPrep(lex.currentLine as String)
            }
        }
        if (preprocessor.base.globalVariables.flags.debug) println(
            preprocessor.base.globalVariables.depthAsString() +
                    "popping '${'$'}{tokenSequence.peek()}'"
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
    if (iterations > maxIterations) if (preprocessor.base.globalVariables.flags.debug) println(
        preprocessor.base.globalVariables.depthAsString() + "iterations expired"
    )
    argc++
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "argc = ${'$'}argc")
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "argv = ${'$'}argv")
    val macroTypeDependantIndex = macroFunctionIndex
    // Line is longer than allowed by code style (> 120 columns)
    if (preprocessor.base.globalVariables.flags.debug) println(
        preprocessor.base.globalVariables.depthAsString() +
                "${'$'}{macro[index].macros[macroTypeDependantIndex].identifier} of type " +
                "${'$'}{macro[index].macros[macroTypeDependantIndex].type} has value " +
                "${'$'}{macro[index].macros[macroTypeDependantIndex].replacementList
                    ?.replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())}"
    )
    if (preprocessor.base.globalVariables.flags.debug) println(
        preprocessor.base.globalVariables.depthAsString() +
                "macro  args = ${'$'}{macro[index].macros[macroTypeDependantIndex].arguments}"
    )
    val macroUnexpanded: MutableList<String> = mutableListOf()
    var i = 0
    if (preprocessor.base.globalVariables.flags.debug) println(
        preprocessor.base.globalVariables.depthAsString() +
                "expanding arguments: ${'$'}argc arguments to expand"
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
                Parser(lex.currentLine as String) { parserPrep(it) }
            val e = expand(
                depth = depth + 1,
                lex = lex,
                tokenSequence = parser,
                macro = macro,
                expanding = argv[i].substringBefore('('),
                expandingType = if (argv[i].contains('('))
                    Macro().Directives().Define().Types().Function
                else
                    Macro().Directives().Define().Types().Object,
                newlineFunction = newlineFunction
            )
            preprocessor.base.globalVariables.depth = depth
            if (preprocessor.base.globalVariables.flags.debug) println(
                preprocessor.base.globalVariables.depthAsString() +
                        "macro expansion '${'$'}{argv[i]}' returned ${'$'}e"
            )
            argv[i] = e!!.trimStart().trimEnd()
        }
        i++
    }
    if (preprocessor.base.globalVariables.flags.debug) println(
        preprocessor.base.globalVariables.depthAsString() +
                "expanded arguments: ${'$'}argc arguments expanded"
    )
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "target args = ${'$'}argv")
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "unexpanded target args = ${'$'}macroUnexpanded")
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
    replacementList: String?,
    newlineFunction :(
        (String) -> String
    )?
): String? = if (macro[index].macros[macroTypeDependantIndex].replacementList == null) null
else {
    val lex = Lexer(
        replacementList!!.toByteArray(),
        globalVariables.tokensNewLine
    )
    lex.lex()
    if (lex.currentLine != null) {
        val parser = Parser(lex.currentLine as String) { parserPrep(it) }
        val associatedArguments: MutableList<Macro>? = toMacro(arguments, argv)
        val associatedArgumentsUnexpanded: MutableList<Macro>? = toMacro(
            definition = arguments,
            replacementList = macroUnexpanded
        )
        if (associatedArguments == null || associatedArgumentsUnexpanded == null) abort("arguments are null")
        // Line is longer than allowed by code style (> 120 columns)
        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "arguments" + arguments)
        val e = expand(
            depth = depth + 1,
            lex = lex,
            tokenSequence = parser,
            macro = associatedArguments,
            macroUnexpanded = associatedArgumentsUnexpanded,
            ARG = arguments,
            expanding = name,
            expandingType = Macro().Directives().Define().Types().Function,
            newlineFunction = newlineFunction
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
    newlineFunction :(
        (String) -> String
    )?,
    string: String
): String? {
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() +
            "string = '${'$'}string'")
    val lex2 = Lexer(string.toByteArray(), globalVariables.tokensNewLine)
    lex2.lex()
    if (lex2.currentLine != null) {
        val parser = Parser(lex2.currentLine as String) { parserPrep(it) }
        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() +
                "lex.currentLine = '${'$'}{lex.currentLine}'")
        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() +
                "lex2.currentLine = '${'$'}{lex2.currentLine}'")
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
            c = c,
            newlineFunction = newlineFunction
        )
        preprocessor.base.globalVariables.depth = depth
        return e
    } else return null
}""",
                            "extractArguments.kt", "linuxMain/kotlin/preprocessor/utils/extra/extractArguments.kt", """package preprocessor.utils.extra

import preprocessor.utils.core.abort

/**
 * extracts the arguments of a function and puts them into an array
 *
 * @returns an array of parameters
 */
fun extractArguments(arg: String): MutableList<String>? {
    fun filterSplit(arg: String, ex: Balanced, b: Balanced.BalanceList): MutableList<String> {
        val arguments: MutableList<String> = mutableListOf()
        if (ex.containsL(arg, b)) {
            if (ex.isBalancedSplit(arg, b, ',')) {
                ex.info()
                if (ex.splitterCount == 0) {
                    arguments.add(arg)
                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + arguments[0])
                } else {
                    var s: String = arg.substring(0, ex.splitterLocation[0]).trimStart()
                    arguments.add(s)
                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + arguments[0])
                    var i = 0
                    while (i < ex.splitterLocation.lastIndex) {
                        s = arg.substring(ex.splitterLocation[i] + 1, ex.splitterLocation[i + 1]).trimStart()
                        arguments.add(s)
                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + arguments[i])
                        i++
                    }
                    s = arg.substring(ex.splitterLocation[i] + 1, ex.index).trimStart()
                    arguments.add(s)
                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + arguments[i])
                }
            } else {
                ex.info()
                abort(preprocessor.base.globalVariables.depthAsString() + "unBalanced code")
            }
        } else if (ex.containsR(arg, b)) {
            // unBalanced
            abort(preprocessor.base.globalVariables.depthAsString() + "unBalanced code")
        } else {
            val a: MutableList<String> = arg.split(',').toMutableList()
            // next, remove whitespaces from the start and end of each index string
            var i = 0
            while (i <= a.lastIndex) {
                val s: String = a[i].trimStart().trimEnd()
                arguments.add(s)
                i++
            }
        }
        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "arguments List = ${'$'}arguments")
        return arguments
    }
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "extracting arguments for ${'$'}arg")
    // first, determine the positions of all tokens
    val balance = Balanced.BalanceList()
    balance.addPair('(', ')')
    balance.addPair('{', '}')
    balance.addPair('[', ']')
    val ex = Balanced()
    return filterSplit(arg, ex, balance)
}
""",
                            "parse.kt", "linuxMain/kotlin/preprocessor/utils/extra/parse.kt", """package preprocessor.utils.extra

import preprocessor.base.globalVariables
import preprocessor.core.Macro
import preprocessor.core.parserPrep
import preprocessor.core.Lexer
import preprocessor.core.Parser
import preprocessor.utils.`class`.extensions.toByteArray
import preprocessor.utils.core.algorithms.Stack

/**
 * parses a line
 * @param lex the current [Lexer]
 * @param macro the [Macro] list
 */
fun parse(lex: Lexer, macro: MutableList<Macro>): String? = parse(lex, macro, null)

/**
 * parses a line
 * @param lex the current [Lexer]
 * @param macro the [Macro] list
 * @param newlineFunction the function to try if [lex].lex() fails
 */
fun parse(lex: Lexer, macro: MutableList<Macro>, newlineFunction :((String) -> String)?): String? {
    preprocessor.base.globalVariables.depth = 0
    return expand(
        lex = lex,
        tokenSequence = Parser(tokens = lex.currentLine as String, stackMethod = { parserPrep(it) }),
        macro = macro,
        newlineFunction = newlineFunction
    )
}

/**
 * parses a line
 * @param string the string to parse
 * @param macro the [Macro] list
 */
fun parse(
    string: String,
    macro: MutableList<Macro>
): String? = parse(string, macro, null)

/**
 * parses a line
 * @param string the string to parse
 * @param macro the [Macro] list
 * @param newlineFunction the function to try if [lex].lex() fails
 */
fun parse(
    string: String,
    macro: MutableList<Macro>,
    newlineFunction :((String) -> String)?
): String? {
    if (string.isEmpty()) return string
    val lex = Lexer(
        string.toByteArray(),
        globalVariables.tokensNewLine
    )
    lex.lex()
    if (lex.currentLine == null) return null
    val str = StringBuilder()
    while (lex.currentLine != null) {
        val p = parse(lex, macro, newlineFunction)
        if (p == null) return null
        str.append(p)
        lex.lex()
    }
    return str.toString()
}
"""
                        )
                    )
                ),
                listOf(
                    "sample",
                    "Commands.kt", "linuxMain/kotlin/sample/Commands.kt", """package sample

import preprocessor.utils.`class`.extensions.*
import preprocessor.utils.core.algorithms.LinkedList

@Suppress("unused")
class Commands(repl: REPL) {
    inner class cmd {
        var command: String? = null
        var description: String? = null
        var alias: String? = null
        var function: (() -> Unit)? = null
    }

    var command = LinkedList<cmd>()
    val repl = repl
    val defaultCommandHeader = "command"
    val defaultDescriptionHeader = "description"

    fun add(command: String): Commands = this.also {
        it.command.append(
            cmd()
                .also {
                    it.command = command
                }
                .also {
                    it.description = "No description provided"
                }
        )
    }
    fun add(command: String, description: String): Commands = this.also {
        it.command.append(
            cmd()
                .also {
                    it.command = command
                }.also {
                    it.description = description
                }
        )
    }
    fun add(command: String, description: String, function: () -> Unit): Commands = this.also {
        it.command.append(
            cmd()
                .also {
                    it.command = command
                }.also {
                    it.function = function
                }.also {
                    it.description = description
                }
        )
    }
    fun add(command: String, function: () -> Unit): Commands = this.also {
        it.command.append(
            cmd()
                .also {
                    it.command = command
                }.also {
                    it.function = function
                }
                .also {
                    it.description = "No description provided"
                }
        )
    }
    fun alias(command: String, value: String): Commands = this.also {
        it.command.append(
            cmd()
                .also {
                    it.command = value
                }
                .also {
                    it.alias = command
                }
                .also {
                    it.description = "alias for ${'$'}value"
                }
        )
    }
    fun get(command: String) : (() -> Unit)? {
        if (repl.debug) println("repl.debug = ${'$'}{repl.debug}")
        this.command.forEach {
            when {
                it != null -> when {
                    it.command != null || it.alias != null -> when {
                        ifUnconditionalReturn(
                            ifTrueReturn(
                                it.alias.equals(command)
                            ) {
                                if (repl.debug) println("alias equals command")
                            }.ifFalseReturn {
                                if (repl.debug) println("alias does not equal command")
                            } && ifFalseReturn(
                                !it.command.equals(command)
                            ) {
                                if (repl.debug) println("command equals command, this is a looping alias")
                            }.ifTrueReturn {
                                if (repl.debug) println("command does not equal command, this is not a looping alias")
                            } && ifTrueReturn(
                                it.function == null
                            ) {
                                if (repl.debug) println("function is null")
                            }.ifFalseReturn {
                                if (repl.debug) println("function is not null")
                            }
                        )
                            {
                                if (repl.debug) println("alias found")
                            }.ifTrueReturn {
                            if (repl.debug) println("and it matches ${'$'}command")
                            }.ifFalseReturn {
                            if (repl.debug) println("and it does not match ${'$'}command")
                            } -> {
                                val t = get(it.command!!)
                                return t
                            }
                        ifUnconditionalReturn(it.command.equals(command) && it.alias == null) {
                            if (repl.debug) println("command found")
                        }.ifTrueReturn {
                            if (repl.debug) println("and it matches ${'$'}command")
                        }.ifFalseReturn {
                            if (repl.debug) println("and it does not match ${'$'}command")
                        } -> return it.function
                    }
                }
            }
        }
        return null
    }
    fun longestCommand(): Int {
        var currentLength = 0
        this.command.forEach {
            if (it != null) {
                if (it.command != null) {
                    var thisLength = it.command!!.length
                    if (thisLength isGreaterThan currentLength ) currentLength = thisLength
                    thisLength = defaultCommandHeader.length
                    if (thisLength isGreaterThan currentLength) currentLength = thisLength
                }
            }
        }
        return currentLength
    }
    fun longestAlias(): Int {
        var currentLength = 0
        this.command.forEach {
            if (it != null) {
                if (it.alias != null) {
                    var thisLength = it.alias!!.length
                    if (thisLength isGreaterThan currentLength ) currentLength = thisLength
                    thisLength = defaultCommandHeader.length
                    if (thisLength isGreaterThan currentLength) currentLength = thisLength
                }
            }
        }
        return currentLength
    }
    fun longestDescription(): Int {
        var currentLength = 0
        this.command.forEach {
            if (it != null) {
                if (it.description != null) {
                    var thisLineCurrentLength = 0
                    it.description!!.lines().forEach {
                        var thisLineLength = it.length
                        if (thisLineLength isGreaterThan thisLineCurrentLength) thisLineCurrentLength = thisLineLength
                        thisLineLength = defaultDescriptionHeader.length
                        if (thisLineLength isGreaterThan thisLineCurrentLength) thisLineCurrentLength = thisLineLength
                    }
                    val thisLength = thisLineCurrentLength
                    if (thisLength isGreaterThan currentLength) currentLength = thisLength
                }
            }
        }
        return currentLength
    }
    inner class Format {
        val seperationLength = 2
        val spacing = 2
        inner class PrettyPrint {
            inner class Frame {
                inner class Corner {
                    var topRight: String = "┓"
                    var bottomRight: String = "┛"
                    var bottomLeft: String = "┗"
                    var topLeft: String = "┏"
                }
                inner class Wall {
                    var top: String = "━"
                    var right: String = "┃"
                    var bottom: String = "━"
                    var left: String = "┃"
                }
                inner class Intersection {
                    var top: String = "┳"
                    var right: String = "┣"
                    var bottom: String = "┻"
                    var left: String = "┫"
                    var all: String = "╋"
                }
            }
            fun single(command: String, description: String) {
                val lengthAlias = longestAlias()
                val lengthCommand = longestCommand()
                val length = if (lengthAlias isGreaterThan lengthCommand) lengthAlias else lengthCommand
                if(length isLessThanOrEqualTo 0) return
                println(
                    // top
                    Frame().Corner().topLeft
                            + "".padExtendEnd(seperationLength, Frame().Wall().top)
                            + "".padExtendEnd(length, Frame().Wall().top)
                            + "".padExtendEnd(seperationLength, Frame().Wall().top)
                            + Frame().Intersection().top
                            + "".padExtendEnd(seperationLength, Frame().Wall().top)
                            + "".padExtendEnd(longestDescription(), Frame().Wall().top)
                            + "".padExtendEnd(seperationLength, Frame().Wall().top)
                            + Frame().Corner().topRight
                            + "\n"
                            // middle top
                            + Frame().Wall().left
                            + "".padExtendEnd(seperationLength, ' ')
                            + defaultCommandHeader.padExtendEnd(length, ' ')
                            + "".padExtendEnd(seperationLength, ' ')
                            + "┃"
                            + "".padExtendEnd(seperationLength, ' ')
                            + defaultDescriptionHeader.padExtendEnd(longestDescription(), ' ')
                            + "".padExtendEnd(seperationLength, ' ')
                            + Frame().Wall().right
                            + "\n"
                            // seperator
                            + Frame().Intersection().right
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + "".padExtendEnd(length, Frame().Wall().bottom)
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + Frame().Intersection().all
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + "".padExtendEnd(longestDescription(), Frame().Wall().bottom)
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + Frame().Intersection().left
                            + "\n"
                            // middle
                            + Frame().Wall().left
                            + "".padExtendEnd(seperationLength, ' ')
                            + command.padExtendEnd(length, ' ')
                            + "".padExtendEnd(seperationLength, ' ')
                            + "┃"
                            + "".padExtendEnd(seperationLength, ' ')
                            + description.padExtendEnd(longestDescription(), ' ')
                            + "".padExtendEnd(seperationLength, ' ')
                            + Frame().Wall().right
                            + "\n"
                            // bottom
                            + Frame().Corner().bottomLeft
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + "".padExtendEnd(length, Frame().Wall().bottom)
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + Frame().Intersection().bottom
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + "".padExtendEnd(longestDescription(), Frame().Wall().bottom)
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + Frame().Corner().bottomRight
                )
            }
            fun top(command: String, description: String) {
                val lengthAlias = longestAlias()
                val lengthCommand = longestCommand()
                val length = if (lengthAlias isGreaterThan lengthCommand) lengthAlias else lengthCommand
                if(length isLessThanOrEqualTo 0) return
                println(
                    Frame().Corner().topLeft
                            + "".padExtendEnd(seperationLength, Frame().Wall().top)
                            + "".padExtendEnd(length, Frame().Wall().top)
                            + "".padExtendEnd(seperationLength, Frame().Wall().top)
                            + Frame().Intersection().top
                            + "".padExtendEnd(seperationLength, Frame().Wall().top)
                            + "".padExtendEnd(longestDescription(), Frame().Wall().top)
                            + "".padExtendEnd(seperationLength, Frame().Wall().top)
                            + Frame().Corner().topRight
                )
                middleTop(defaultCommandHeader, defaultDescriptionHeader)
                middleTop(command, description)
            }
            fun middleTop(command: String, description: String) {
                val lengthAlias = longestAlias()
                val lengthCommand = longestCommand()
                val length = if (lengthAlias isGreaterThan lengthCommand) lengthAlias else lengthCommand
                if(length isLessThanOrEqualTo 0) return
                println(
                    Frame().Wall().left
                            + "".padExtendEnd(seperationLength, ' ')
                            + command.padExtendEnd(length, ' ')
                            + "".padExtendEnd(seperationLength, ' ')
                            + "┃"
                            + "".padExtendEnd(seperationLength, ' ')
                            + description.padExtendEnd(longestDescription(), ' ')
                            + "".padExtendEnd(seperationLength, ' ')
                            + Frame().Wall().right
                )
                middleSeperator()
            }
            fun middleBottom(command: String, description: String) {
                Debugger().breakPoint()
                val lengthAlias = longestAlias()
                val lengthCommand = longestCommand()
                val length = if (lengthAlias isGreaterThan lengthCommand) lengthAlias else lengthCommand
                if(length isLessThanOrEqualTo 0) return
                println(
                    Frame().Wall().left
                            + "".padExtendEnd(seperationLength, ' ')
                            + command.padExtendEnd(length, ' ')
                            + "".padExtendEnd(seperationLength, ' ')
                            + "┃"
                            + "".padExtendEnd(seperationLength, ' ')
                            + description.padExtendEnd(longestDescription(), ' ')
                            + "".padExtendEnd(seperationLength, ' ')
                            + Frame().Wall().right
                )
            }
            fun middleSeperator() {
                val lengthAlias = longestAlias()
                val lengthCommand = longestCommand()
                val length = if (lengthAlias isGreaterThan lengthCommand) lengthAlias else lengthCommand
                if(length isLessThanOrEqualTo 0) return
                println(
                    Frame().Intersection().right
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + "".padExtendEnd(length, Frame().Wall().bottom)
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + Frame().Intersection().all
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + "".padExtendEnd(longestDescription(), Frame().Wall().bottom)
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + Frame().Intersection().left
                )
            }
            fun bottom(command: String, description: String) {
                val lengthAlias = longestAlias()
                val lengthCommand = longestCommand()
                val length = if (lengthAlias isGreaterThan lengthCommand) lengthAlias else lengthCommand
                if(length isLessThanOrEqualTo 0) return
                middleBottom(command, description)
                println(
                    Frame().Corner().bottomLeft
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + "".padExtendEnd(length, Frame().Wall().bottom)
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + Frame().Intersection().bottom
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + "".padExtendEnd(longestDescription(), Frame().Wall().bottom)
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + Frame().Corner().bottomRight
                )
            }
        }
        fun normal(command: String, description: String) {
            val lengthAlias = longestAlias()
            val lengthCommand = longestCommand()
            val length = if (lengthAlias isGreaterThan lengthCommand) lengthAlias else lengthCommand
            if(length isLessThanOrEqualTo 0) return
            println(command.padExtendEnd(length+seperationLength, ' ') + description)
        }
    }
    fun listCommands() {
        for ((index, it) in this.command.withIndex()) {
            when {
                it != null -> when {
                    (it.command != null && it.description != null) -> when {
                        this.command.count() == 1 -> Format().PrettyPrint().single(
                            when {
                                it.alias != null -> it.alias
                                else -> it.command
                            }!!, it.description!!)
                        else -> when(index) {
                            0 -> Format().PrettyPrint().top(
                                when {
                                    it.alias != null -> it.alias
                                    else -> it.command
                                }!!, it.description!!
                            )
                            this.command.count() - 1, this.command.count() -> Format().PrettyPrint().bottom(
                                when {
                                    it.alias != null -> it.alias
                                    else -> it.command
                                }!!, it.description!!
                            )
                            else -> Format().PrettyPrint().middleTop(
                                when {
                                    it.alias != null -> it.alias
                                    else -> it.command
                                }!!, it.description!!
                            )
                        }
                    }
                }
            }
        }
    }
}""",
                    listOf(
                        "kotlin",
                        "Grammar.kt", "linuxMain/kotlin/sample/kotlin/Grammar.kt", """package sample.kotlin

import preprocessor.base.globalVariables
import preprocessor.core.Lexer
import preprocessor.utils.`class`.extensions.toByteArray

class Grammar {
    fun lexer(contents: String) {
        val lex = Lexer(
            contents.toByteArray(),
            globalVariables.tokensNewLine
        )
        lex.lex()
        if (lex.currentLine == null) return
        while (lex.currentLine != null) {
            lex
            lex.lex()
        }
    }
}""",
                        "Kotlin.kt", "linuxMain/kotlin/sample/kotlin/Kotlin.kt", """@file:Suppress("unused")

package sample

import preprocessor.base.globalVariables
import preprocessor.core.Lexer
import preprocessor.core.Parser
import preprocessor.test.init
import preprocessor.utils.`class`.extensions.toByteArray
import preprocessor.utils.`class`.extensions.toStack
import preprocessor.utils.core.algorithms.Stack
import preprocessor.utils.extra.parse

class Kotlin(contents: String) {
    val parseStream = Parser(contents) { it.toStack() }
    inner class Lexer {
        val A = parseStream.IsSequenceOnce("A")
        val B = parseStream.IsSequenceOnce("B")
        val AB = A and B
        init {
            println(parseStream.toStringAsArray())
            println(A.peek())
            println("A.value = " + A.value)
            println("A.left?.value = " + A.left?.value)
            println("A.right?.value = " + A.right?.value)
            println("B.value = " + B.value)
            println("B.left?.value = " + B.left?.value)
            println("B.right?.value = " + B.right?.value)
            println("AB.value = " + AB.value)
            println("AB.left?.value = " + AB.left?.value)
            println("AB.right?.value = " + AB.right?.value)
        }
    }

    inner class Parser {
        fun parse() {
        }
    }
}

private infix fun Parser.IsSequenceOnce.or(and: Parser.IsSequenceOnce): Parser.IsSequenceOnce {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

fun Parser.isSequenceOnceArray(): Parser {
    return clone()
}"""
                    ),
                    "Hierarchy.kt", "linuxMain/kotlin/sample/Hierarchy.kt", """
class Hierarchy {
    private val indentation = 4

    private fun indent(depth: Int) = " ".repeat(indentation).repeat(depth)
    fun printHierarchy() = printHierarchy(0, rootFileSystem)
    private fun printHierarchy(depth: Int = 0, rootFileSystem: List<Any>) {
        var indexI = 0
        for (any in rootFileSystem) {
            when(any) {
                is List<*> -> printHierarchy(depth+1, any as List<Any>)
                else ->  when(indexI) {
                    0 -> println("${'$'}{indent(if (depth == 0) 0 else depth)}directory = ${'$'}any").also {indexI++}
                    1 -> println("${'$'}{indent(depth + 1)}file: ${'$'}any").also {indexI++}
                    2 -> println("${'$'}{indent(depth + 1)}full file path: ${'$'}any").also {indexI++}
                    3 -> {
                        println("${'$'}{indent(depth + 1)}content: ${'$'}any")
                        indexI = 1
                    }
                }
            }
        }
    }
    fun listFiles() = listFiles(rootFileSystem)
    private fun listFiles(rootFileSystem: List<Any>) {
        var indexI = 0
        for (any in rootFileSystem) {
            when(any) {
                is List<*> -> listFiles(any as List<Any>)
                else ->  when(indexI) {
                    0 -> println("directory = ${'$'}any").also {indexI++}
                    1 -> println("file: ${'$'}any").also {indexI++}
                    2 -> println("full file path: ${'$'}any").also {indexI++}
                    3 -> {
                       indexI = 1 // skip contents
                    }
                }
            }
        }
    }
    fun find(file: String) = find(file, rootFileSystem)
    private fun find(file: String, rootFileSystem: List<Any>) {
        var indexI = 0
        for (any in rootFileSystem) {
            when(any) {
                is List<*> -> find(file, any as List<Any>)
                else ->  when(indexI) {
                    0 -> println("directory = ${'$'}any").also {indexI++}
                    1 -> println("file: ${'$'}any").also {indexI++}
                    2 -> println("full file path: ${'$'}any").also {indexI++}
                    3 -> {
                       indexI = 1 // skip contents
                    }
                }
            }
        }
    }

    private val rootFileSystem = listOf(
        "Root",
        listOf(
            "linuxMain",
            listOf(
                "kotlin",
                listOf(
                    "preprocessor",
                    listOf(
                        "core",
                        "Parser.kt", "linuxMain/kotlin/preprocessor/core/Parser.kt", \"\"\"package preprocessor.core

import preprocessor.base.globalVariables
import preprocessor.utils.core.abort
import preprocessor.utils.core.algorithms.Stack
import preprocessor.utils.`class`.extensions.tokenize

/**
 * a minimal Parser implementation
 * @param tokens a list of tokens to split by
 * @see preprocessor.globals.Globals.tokens
 * @see Lexer
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class Parser(tokens: String, stackMethod: (String) -> Stack<String>) {
    val stackMethodFunction = stackMethod

    /**
     * a line represented as an [Stack] as returned by [parserPrep]
     * @see preprocessor.globals.Globals.tokens
     */
    var tokenList: Stack<String> = stackMethodFunction(tokens)

    /**
     *
     */
    inner class InternalLineInfo {
        /**
         *
         */
        inner class Default {
            /**
             *
             */
            var column: Int = 1
            /**
             *
             */
            var line: Int = 1
        }

        /**
         *
         */
        var column: Int = Default().column
        /**
         *
         */
        var line: Int = Default().line
    }

    /**
     *
     */
    var lineInfo: InternalLineInfo = InternalLineInfo()

    /**
     * @return a new independent instance of the current [Parser]
     */
    fun clone(): Parser {
        return Parser(tokenList.clone().toStringConcat(), stackMethodFunction)
    }

    /**
     * wrapper for [Stack.peek]
     * @return [tokenList].[peek()][Stack.peek]
     */
    fun peek(): String? {
        return tokenList.peek()
    }

    /**
     * wrapper for [Stack.pop]
     * @return [tokenList].[pop()][Stack.pop] on success
     *
     * **null** on failure
     *
     * [aborts][abort] if the [tokenList] is corrupted
     */
    fun pop(): String? {
        if (tokenList.peek() == null) {
            try {
                tokenList.pop()
            } catch (e: NoSuchElementException) {
                return null
            }
            abort(preprocessor.base.globalVariables.depthAsString() + "token list is corrupted, or the desired exception 'java.util.NoSuchElementException' was not caught")
        }
        val returnValue = tokenList.pop() ?: abort(preprocessor.base.globalVariables.depthAsString() + "token list is corrupted")
        var i = 0
        while (i < returnValue.length) {
            if (returnValue[i] == '\n') {
                lineInfo.column = lineInfo.Default().column
                lineInfo.line++
            } else lineInfo.column++
            i++
        }
        return returnValue
    }

    /**
     * wrapper for [Stack.clear]
     * @return [tokenList].[clear()][Stack.clear]
     */
    fun clear() {
        tokenList.clear()
    }

    /**
     * @return the current [tokens][preprocessor.globals.Globals.tokens] left, converted into a [String]
     * @see stackToString
     */
    override fun toString(): String {
        return tokenList.toStringConcat()
    }

    /**
     * @return the current [tokens][preprocessor.globals.Globals.tokens] left, directly returns
     * [tokenList].[toString()][Stack.toString]
     */
    fun toStringAsArray(): String {
        return tokenList.toString()
    }

    /**
     * matches a sequence of **str** zero or more times
     * @see IsSequenceOneOrMany
     * @see IsSequenceOnce
     */
    inner class IsSequenceZeroOrMany(str: String) {
        val value: String = str
        private val seq: IsSequenceOneOrMany = IsSequenceOneOrMany(value)

        /**
         * returns the value of [IsSequenceOneOrMany.toString]
         *
         * the original [tokenList] is [cloned][Parser.clone] to prevent modifications to the original [list][tokenList]
         *
         * @return the result of [IsSequenceOneOrMany.toString], which can be an empty string
         * @see IsSequenceOneOrMany
         * @see peek
         * @see pop
         */
        override fun toString(): String {
            return seq.toString()
        }

        /**
         * this function always returns true
         * @see toString
         * @see pop
         */
        fun peek(): Boolean {
            return true
        }

        /**
         *
         * **WARNING**: this **CAN** modify [tokenList]
         *
         * **this relies on the safety of [IsSequenceOneOrMany.pop]**
         *
         * this function always returns true
         * @see IsSequenceOneOrMany
         * @see toString
         * @see peek
         */
        fun pop(): Boolean {
            while (seq.peek()) seq.pop()
            return true
        }
    }

    /**
     * matches a sequence of **str** one or more times
     * @see IsSequenceZeroOrMany
     * @see IsSequenceOnce
     */
    inner class IsSequenceOneOrMany(str: String) {
        val value: String = str

        /**
         * returns the accumulative value of [IsSequenceOnce.toString]
         *
         * the original [tokenList] is [cloned][Parser.clone] to prevent modifications to the original [list][tokenList]
         *
         * @return the accumalative result of [IsSequenceOnce.toString], which can be an empty string
         * @see IsSequenceOnce
         * @see peek
         * @see pop
         */
        override fun toString(): String {
            val o = clone().IsSequenceOnce(value)
            val result = StringBuilder()
            while (o.peek()) {
                result.append(o.toString())
                o.pop()
            }
            return result.toString()
        }

        /**
         *
         * the original [tokenList] is [cloned][Parser.clone] to prevent modifications to the original [list][tokenList]
         *
         * @return true if [value] matches at least once
         * @see IsSequenceOnce
         * @see toString
         * @see pop
         */
        fun peek(): Boolean {
            val o = clone().IsSequenceOnce(value)
            var matches = 0
            while (o.peek()) {
                matches++
                o.pop()
            }
            if (matches != 0) return true
            return false
        }

        /**
         *
         * **WARNING**: this **CAN** modify [tokenList]
         *
         * it is recommended to use ***if (Parser.peek()) Parser.pop()*** to avoid accidental modifications, where
         * **Parser** is an instance of [IsSequenceOneOrMany]
         *
         * @return true if [value] matches at least once
         * @see IsSequenceOnce
         * @see IsSequenceOnce.pop
         * @see toString
         * @see peek
         */
        fun pop(): Boolean {
            val o = IsSequenceOnce(value)
            var matches = 0
            while (o.peek()) {
                matches++
                o.pop()
            }
            if (matches != 0) return true
            return false
        }
    }

    /**
     * matches a sequence of **str**
     *
     * this is the base class of which all Parser sequence classes use
     *
     * based on [Stack] functions [toString][Stack.toString], [peek][Stack.peek], and
     * [pop][Stack.pop]
     *
     * @param str a string to match
     * @see IsSequenceZeroOrMany
     * @see IsSequenceOneOrMany
     */
    inner class IsSequenceOnce(str: String) {
        var left: IsSequenceOnce = this
        var right: IsSequenceOnce? = null
        var side = left

        val parent = this@Parser
        /**
         * the string to match
         */
        val value = str

        /**
         * returns the accumulative value of [Parser.peek] and [Parser.pop]
         *
         * the original [tokenList] is [cloned][Parser.clone] to prevent modifications to the original [list][tokenList]
         *
         * @return "" if either [tokenList] or [str][value] is empty
         *
         * otherwise the result of [Parser.peek], which can never be an empty string
         * @see peek
         * @see pop
         */
        override fun toString(): String {
            val tmp = tokenList.clone()
            val s = stackMethodFunction(value)
            if (s.peek() == null || tmp.peek() == null) return ""
            val result = StringBuilder()
            while (tmp.peek() != null && s.peek() != null) {
                val x = tmp.peek()
                if (tmp.pop() == s.pop()) result.append(x)
            }
            return result.toString()
        }

        /**
         * the original [tokenList] is [cloned][Parser.clone] to prevent modifications to the original [list][tokenList]
         *
         * @return false if [str][value] does not match
         *
         * otherwise returns true
         * @see toString
         * @see pop
         */
        fun peek(): Boolean {
            val tmp = tokenList.clone()
            val s = stackMethodFunction(value)
            if (s.peek() == null || tmp.peek() == null) return false
            val expected = s.size
            var matches = 0
            while (tmp.peek() != null && s.peek() != null) if (tmp.pop() == s.pop()) matches++
            if (matches == expected) return true
            return false
        }

        /**
         *
         * **WARNING**: this **CAN** modify [tokenList]
         *
         * it is recommended to use ***if (Parser.peek()) Parser.pop()*** to avoid accidental modifications, where
         * **Parser** is an instance of [IsSequenceOnce]
         *
         * @return false if [str][value] does not match
         *
         * otherwise returns true
         * @see toString
         * @see peek
         */
        fun pop(): Boolean {
            val s = stackMethodFunction(value)
            if (s.peek() == null) return false
            val expected = s.size
            var matches = 0
            while (this@Parser.peek() != null && s.peek() != null) if (this@Parser.pop().equals(s.pop())) matches++
            if (matches == expected) return true
            return false
        }

        infix fun and(right: IsSequenceOnce): IsSequenceOnce {
            val x = IsSequenceOnce(this.value)
            x.left = this
            x.right = right
            val y = IsSequenceOnce(right.value)
            x.left = x
            return y
        }

/*
        val A = parseStream.IsSequenceOnce("A")
        val B = parseStream.IsSequenceOnce("B")
        val C = parseStream.IsSequenceOnce("C")
*/
    }
}\"\"\",
                        "MacroTools.kt", "linuxMain/kotlin/preprocessor/core/MacroTools.kt", \"\"\"@file:Suppress("unused")

package preprocessor.core

import preprocessor.base.globalVariables
import preprocessor.utils.`class`.extensions.tokenize
import preprocessor.utils.core.abort
import preprocessor.utils.core.algorithms.Stack
import preprocessor.utils.core.realloc

/**
 *
 * This class is used to house all macro definitions
 *
 * @sample Tests.generalUsage
 *
 * @see Directives
 * @see Directives.Define
 *
 */
class Macro {
    /**
     * this class is used to obtain predefined values such as directive names and types
     *
     * all preprocessor directives start with a [#][Macro.Directives.value]
     *
     * this CAN be changed if desired to run multiple variants of this preprocessor on the same file
     * @see Define
     */
    @Suppress("MemberVisibilityCanBePrivate")
    inner class Directives {
        /**
         * the preprocessor [directive][Directives] identification token
         *
         * this defaults to **#**
         */
        var value: String = "#"

        /**
         * contains predefined [values][Define.value] and [types][Define.Types] for the
         * [#][Macro.Directives.value][define][Define.value] directive
         * @see Directives
         */
        inner class Define {
            /**
             * the ***[#][Macro.Directives.value]define*** directive associates an identifier to a replacement list
             * the Default value of this is **define**
             * @see Types
             */
            val value: String = "define"

            /**
             * the valid types of a definition directive macro
             * @see Object
             * @see Function
             */
            inner class Types {
                /**
                 * the Object type denotes the standard macro definition, in which all text is matched with
                 *
                 * making **Object** lowercase conflicts with the top level declaration **object**
                 *
                 * @see Types
                 * @see Function
                 * @sample objectSample
                 * @sample objectUsage
                 */
                @Suppress("PropertyName")
                val Object: String = "object"
                /**
                 * the Function type denotes the Function macro definition, in which all text that is followed by
                 * parentheses is matched with
                 *
                 * making **Function** lowercase must mean [Object] must also be lowercase
                 * to maintain naming pairs (as **Object, function**, and **object, Function**
                 * just looks weird)
                 *
                 * unfortunately this is impossible as it would conflict with the top level
                 * declaration **object**
                 *
                 * @see Types
                 * @see Object
                 * @sample functionSample
                 * @sample functionUsage
                 */
                @Suppress("PropertyName")
                val Function: String = "function"

                private fun objectSample() {
                    /* ignore this block comment

                    #define object value
                    object
                    object(object).object[object]

                    ignore this block comment */
                }

                private fun objectUsage() {
                    val c = mutableListOf(Macro())
                    c[0].fileName = "test"
                    c[0].macros[0].fullMacro = "A B00"
                    c[0].macros[0].identifier = "A"
                    c[0].macros[0].replacementList = "B00"
                    c[0].macros[0].type =
                        Macro().Directives().Define().Types().Object
                    if (preprocessor.base.globalVariables.flags.debug) println(c[0].macros[0].type)
                }

                private fun functionSample() {
                    /* ignore this block comment

                    #define object value
                    #define function value
                    object
                    function(object).object[function(function()]

                    ignore this block comment */
                }

                private fun functionUsage() {
                    val c = mutableListOf(Macro())
                    c[0].fileName = "test"
                    c[0].macros[0].fullMacro = "A() B00"
                    c[0].macros[0].identifier = "A"
                    c[0].macros[0].replacementList = "B00"
                    c[0].macros[0].type =
                        Macro().Directives().Define().Types().Function
                    if (preprocessor.base.globalVariables.flags.debug) println(c[0].macros[0].type)
                }
            }
        }
    }

    /**
     * the internals of the Macro class
     *
     * this is where all macros are kept, this is managed via [realloc]
     * @sample Tests.generalUsage
     */
    inner class MacroInternal {
//        /**
//         * the current size of the [macro][MacroInternal] list
//         *
//         * can be used to obtain the last added macro
//         *
//         * @sample Tests.sizem
//         */
//        var size: Int = 0
        /**
         * the full macro definition
         *
         * contains the full line at which the definition appears
         */
        var fullMacro: String? = null
        /**
         * contains the definition **identifier**
         */
        var identifier: String? = null
        /**
         * contains the definition **type**
         */
        var type: String? = null
        /**
         * contains the definition **arguments**,
         * valid only for
         * [Function][Macro.Directives.Define.Types.Function]
         * type definitions
         *
         * this defaults to **null**
         *
         */
        var arguments: MutableList<String>? = null
        /**
         * this contains the definition replacement list
         */
        var replacementList: String? = null
    }

//    /**
//     * the current size of the [macro][Macro] list
//     *
//     * can be used to obtain the last added [macro group][MacroInternal]
//     *
//     * @sample Tests.size
//     */
//    var size: Int = 0
    /**
     * the name of the file containing this [macro][Macro] list
     */
    var fileName: String? = null
    /**
     * the [macro][MacroInternal] list
     */
    var macros: MutableList<MacroInternal>

    init {
//        this.size = 1
        macros = mutableListOf(MacroInternal())
    }

    private class Tests {
        fun generalUsage() {
            val c = mutableListOf(Macro())
            c[0].fileName = "test"
            c[0].macros[0].fullMacro = "A B"
            c[0].macros[0].identifier = "A"
            c[0].macros[0].replacementList = "B00"
            if (preprocessor.base.globalVariables.flags.debug) println(c[0].macros[0].replacementList)
            realloc(c, c.size + 1)
            c[1].fileName = "test"
            c[1].macros[0].fullMacro = "A B"
            c[1].macros[0].identifier = "A"
            c[1].macros[0].replacementList = "B10"
            if (preprocessor.base.globalVariables.flags.debug) println(c[1].macros[0].replacementList)
            realloc(c[1].macros, c[1].macros.size + 1)
            c[1].fileName = "test"
            c[1].macros[1].fullMacro = "A B"
            c[1].macros[1].identifier = "A"
            c[1].macros[1].replacementList = "B11"
            if (preprocessor.base.globalVariables.flags.debug) println(c[1].macros[1].replacementList)
            realloc(c[1].macros, c[1].macros.size + 1)
            c[1].fileName = "test"
            c[1].macros[2].fullMacro = "A B"
            c[1].macros[2].identifier = "A"
            c[1].macros[2].replacementList = "B12"
            if (preprocessor.base.globalVariables.flags.debug) println(c[1].macros[2].replacementList)
        }

        fun reallocUsage() {
            val c = mutableListOf(Macro())
            // allocate a new index
            realloc(c, c.size + 1)
            // assign some values
            c[0].fileName = "test"
            c[1].fileName = "test"
        }

        fun reallocUsageInternal() {
            val c = mutableListOf(Macro())
            // allocate a new index
            realloc(c[0].macros, c[0].macros.size + 1)
            // assign some values
            c[0].macros[0].fullMacro = "A A"
            c[0].macros[1].fullMacro = "A B"
        }

        fun size() {
            val c = mutableListOf(Macro())
            // allocate a new macro
            realloc(c[0].macros, c[0].macros.size + 1)
            c[0].macros[1].fullMacro = "A B"
            if (preprocessor.base.globalVariables.flags.debug) println(c[0].macros[1].replacementList)
            // obtain base index
            val index = c.size - 1
            // obtain last macro index
            val macroIndex = c[0].macros.size - 1
            if (c[index].macros[macroIndex].fullMacro.equals(c[0].macros[1].fullMacro))
                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "index matches")
        }

        fun sizem() {
            val c = mutableListOf(Macro())
            c[0].fileName = "test1"
            realloc(c, c.size + 1)
            c[1].fileName = "test2"
            val index = c.size - 1
            if (c[index].fileName.equals(c[1].fileName))
                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "index matches")
        }
    }
}

/**
 * checks if the desired macro exists in the [Macro] list
 */
fun macroExists(token: String, type: String, index: Int, macro: MutableList<Macro>): Int {
    globalVariables.status.currentMacroExists = false
    // if empty return 0 and do not set globalVariables.currentMacroExists
    if (macro[index].macros[0].fullMacro == null) return 0
    var i = 0
    while (i <= macro[index].macros.lastIndex) {
        if (macro[index].macros[i].identifier.equals(token) && macro[index].macros[i].type.equals(type)) {
            // Line is longer than allowed by code style (> 120 columns)
            if (preprocessor.base.globalVariables.flags.debug) println(
                preprocessor.base.globalVariables.depthAsString() +
                        "token and type matches existing definition ${'$'}{'${'$'}'}{macro[index].macros[i].identifier} type " +
                        "${'$'}{'${'$'}'}{macro[index].macros[i].type}"
            )
            globalVariables.status.currentMacroExists = true
            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "returning ${'$'}{'${'$'}'}i")
            return i
        }
        // Line is longer than allowed by code style (> 120 columns)
        else if (preprocessor.base.globalVariables.flags.debug) println(
            preprocessor.base.globalVariables.depthAsString() +
                    "token ${'$'}{'${'$'}'}token or type ${'$'}{'${'$'}'}type does not match current definition token " +
                    "${'$'}{'${'$'}'}{macro[index].macros[i].identifier} type ${'$'}{'${'$'}'}{macro[index].macros[i].type}"
        )
        i++
    }
    return i
}

/**
 * lists the current macros in a [Macro] list
 */
fun macroList(index: Int = 0, macro: MutableList<Macro>) {
    if (macro[index].macros[0].fullMacro == null) return
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "LISTING macros")
    var i = 0
    while (i <= macro[index].macros.lastIndex) {
        // Line is longer than allowed by code style (> 120 columns)
        if (preprocessor.base.globalVariables.flags.debug) println(
            preprocessor.base.globalVariables.depthAsString() +
                    "[${'$'}{'${'$'}'}i].fullMacro       = ${'$'}{'${'$'}'}{macro[index].macros[i].fullMacro}"
        )
        if (preprocessor.base.globalVariables.flags.debug) println(
            preprocessor.base.globalVariables.depthAsString() +
                    "[${'$'}{'${'$'}'}i].type            = ${'$'}{'${'$'}'}{macro[index].macros[i].type}"
        )
        if (preprocessor.base.globalVariables.flags.debug) println(
            preprocessor.base.globalVariables.depthAsString() +
                    "[${'$'}{'${'$'}'}i].identifier      = " +
                    "${'$'}{'${'$'}'}{macro[index].macros[i].identifier}"
        )
        if (macro[index].macros[i].arguments != null)
        // Line is longer than allowed by code style (> 120 columns)
            if (preprocessor.base.globalVariables.flags.debug) println(
                preprocessor.base.globalVariables.depthAsString() +
                        "[${'$'}{'${'$'}'}i].arguments       = ${'$'}{'${'$'}'}{macro[index].macros[i].arguments}"
            )
        // Line is longer than allowed by code style (> 120 columns)
        if (preprocessor.base.globalVariables.flags.debug) println(
            preprocessor.base.globalVariables.depthAsString() +
                    "[${'$'}{'${'$'}'}i].replacementList = ${'$'}{'${'$'}'}{macro[index].macros[i].replacementList
                        ?.replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())}"
        )
        i++
    }
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "LISTED macros")
}

/**
 * lists the current macros in a [Macro] list
 *
 * this version lists ALL [Macro]s in the current [Macro] list in all available file index's
 */
fun macroList(macro: MutableList<Macro>) {
    if (macro.size == 0) {
        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "macro list is empty")
        return
    }
    var i = 0
    while (i < macro.size) {
        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "LISTING macros for file ${'$'}{'${'$'}'}{macro[i].fileName}")
        macroList(i, macro)
        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "LISTED macros for file ${'$'}{'${'$'}'}{macro[i].fileName}")
        i++
    }
}

/**
 * converts a [List] and a [List] into a [Macro] array
 */
fun toMacro(definition: List<String>?, replacementList: List<String>?): MutableList<Macro>? {
    if (definition == null) return null
    if (replacementList == null) return toMacro(definition)
    // Line is longer than allowed by code style (> 120 columns)
    if (preprocessor.base.globalVariables.flags.debug) println(
        preprocessor.base.globalVariables.depthAsString() +
                "${'$'}{'${'$'}'}{definition.size} == ${'$'}{'${'$'}'}{replacementList.size} is " +
                "${'$'}{'${'$'}'}{definition.size == replacementList.size}"
    )
    if (definition.size != replacementList.size) {
        // Line is longer than allowed by code style (> 120 columns)
        abort(
            preprocessor.base.globalVariables.depthAsString() +
                    "size mismatch: expected ${'$'}{'${'$'}'}{definition.size}, got ${'$'}{'${'$'}'}{replacementList.size}"
        )
    }
    val associatedArguments = mutableListOf(Macro())
    var i = 0
    associatedArguments[0].macros[i].fullMacro =
        "${'$'}{'${'$'}'}{Macro().Directives().Define().value} ${'$'}{'${'$'}'}{definition[i]} ${'$'}{'${'$'}'}{replacementList[i]}"
    associatedArguments[0].macros[i].type = Macro().Directives().Define().Types().Object
    associatedArguments[0].macros[i].identifier = definition[i]
    associatedArguments[0].macros[i].replacementList = replacementList[i]
    i++
    while (i <= definition.lastIndex) {
        // Line is longer than allowed by code style (> 120 columns)
        realloc(
            associatedArguments[0].macros,
            associatedArguments[0].macros.size + 1
        )
        associatedArguments[0].macros[i].fullMacro =
            "${'$'}{'${'$'}'}{Macro().Directives().Define().value} ${'$'}{'${'$'}'}{definition[i]} ${'$'}{'${'$'}'}{replacementList[i]}"
        associatedArguments[0].macros[i].type = Macro().Directives().Define().Types().Object
        associatedArguments[0].macros[i].identifier = definition[i]
        associatedArguments[0].macros[i].replacementList = replacementList[i]
        i++
    }
    macroList(macro = associatedArguments)
    return associatedArguments
}

/**
 * converts an [MutableList] and a [List] into a [Macro] array
 */
fun toMacro(definition: MutableList<String>?, replacementList: List<String>?): MutableList<Macro>? =
    toMacro(definition?.toList(), replacementList)

/**
 * converts an [List] and a [MutableList] into a [Macro] array
 */
fun toMacro(definition: List<String>?, replacementList: MutableList<String>?): MutableList<Macro>? =
    toMacro(definition, replacementList?.toList())

/**
 * converts an [MutableList] and a [MutableList] into a [Macro] array
 */
fun toMacro(definition: MutableList<String>?, replacementList: MutableList<String>?): MutableList<Macro>? =
    toMacro(definition?.toList(), replacementList?.toList())

/**
 * converts an [MutableList] into a [Macro] array, value is null
 */
fun toMacro(definition: MutableList<String>?): MutableList<Macro>? =
    toMacro(definition?.toList())

/**
 * converts an [List] into a [Macro] array, value is null
 */
fun toMacro(definition: List<String>?): MutableList<Macro>? {
    return when {
        definition == null -> null
        definition.isNotEmpty() -> mutableListOf(Macro()).also { var0 ->
            definition.forEach {
                processDefine("#define ${'$'}{'${'$'}'}it", var0)
            }
        }.also {
            macroList(macro = it)
        }
        else -> null
    }
}

/**
 * prepares the input **line** for consumption by the macro [Parser]
 * @return an [Stack] which is [split][tokenize] by the global variable **tokens**
 *
 * this [Stack] preserves the tokens in which it is split by
 * @see Parser
 */
fun parserPrep(line: String): Stack<String> = Stack<String>().also { it.addLast(line.tokenize(globalVariables.tokens, true)) }
\"\"\",
                        "Lexer.kt", "linuxMain/kotlin/preprocessor/core/Lexer.kt", \"\"\"package preprocessor.core

import preprocessor.utils.core.algorithms.Stack
import preprocessor.utils.`class`.extensions.toStack

/**
 * a minimal Lexer implementation
 *
 * we traditionally process the input file character by character
 *
 * append each processed character to a buffer
 *
 * then return that buffer when a specific delimiter is found
 *
 * @param stm the byte buffer to read from, set up via
 * [fileToByteArray][preprocessor.utils.conversion.fileToByteArray]
 * @param delimiter the delimiters to split the input by
 * @see Parser
 * @see Lexer.lex
 */
@Suppress("unused")
class Lexer(stm: ByteArray, delimiter: String) {
    private val f: ByteArray = stm
    private var i = 0
    private fun hasNext() = i<f.size
    private fun next() = f[i++]
    private val delimiters: String = delimiter
    private val d: Stack<String> = delimiters.toStack()
    /**
     * this is the current line the Lexer of on
     *
     */
    var currentLine: String? = null

    /**
     *
     */
    inner class InternalLineInfo {
        /**
         *
         */
        inner class Default {
            /**
             *
             */
            var column: Int = 1
            /**
             *
             */
            var line: Int = 1
        }

        /**
         *
         */
        var column: Int = Default().column
        /**
         *
         */
        var line: Int = Default().line
    }

    private var lineInfo: InternalLineInfo = InternalLineInfo()

    /**
     * returns an exact duplicate of the current [Lexer]
     */
    fun clone(): Lexer {
        return Lexer(this.f.copyOf(), this.delimiters)
    }

    /**
     * returns a duplicate of this [Lexer] with the specified delimiters
     */
    fun clone(newDelimiters: String): Lexer {
        return Lexer(this.f.copyOf(), newDelimiters)
    }

    /**
     * sets the variable [currentLine] to the
     * [buffer][ByteArray] (converted to a
     * [String]) when a specific
     * [delimiter][Lexer] is found
     *
     * sets the variable [currentLine] to
     * **null** upon EOF
     */
    fun lex() {
        /*
        in order to make a Lexer, we traditionally process the input file
        character by character, appending each to a buffer, then returning
        that buffer when a specific delimiter is found
        */
        if (!hasNext()) {
            currentLine = null
            return
        }
        val b = StringBuilder()
        while (hasNext()) {
            val s = next().toChar().toString()
            b.append(s)
            if (s == "\n") {
                lineInfo.column = lineInfo.Default().column
                lineInfo.line++
            } else lineInfo.column++
            if (d.contains(s)) break
        }
        currentLine = b.toString()
        return
    }
}
\"\"\",
                        "process.kt", "linuxMain/kotlin/preprocessor/core/process.kt", \"\"\"package preprocessor.core

import preprocessor.utils.extra.Balanced
import preprocessor.utils.extra.extractArguments
import preprocessor.base.globalVariables
import preprocessor.utils.core.abort
import preprocessor.utils.extra.parse
//import preprocessor.utils.conversion.fileToByteArray
import preprocessor.utils.core.realloc
//import java.io.File
//
///**
// * pre-processes a file **src**
// *
// * the result is saved in "${'$'}{'${'$'}'}src${'$'}{'${'$'}'}{globalVariables.preprocessedExtension}${'$'}{'${'$'}'}extensions"
// *
// * @param src the file to be modified
// * @param extension the extension of file specified in **src**
// * @param macro a [Macro] array
// */
//fun process(
//    src: String,
//    extension: String,
//    macro: MutableList<Macro>
//) {
//    val destinationPreProcessed = File("${'$'}{'${'$'}'}src${'$'}{'${'$'}'}{globalVariables.preprocessedExtension}.${'$'}{'${'$'}'}extension")
//    var index = macro.size - 1
//    if (macro[index].fileName != null) {
//        index++
//        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "reallocating to ${'$'}{'${'$'}'}index")
//        realloc(macro, index + 1)
//    }
//    macro[index].fileName = src.substringAfterLast('/')
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "registered macro definition for ${'$'}{'${'$'}'}{macro[index].fileName} at index ${'$'}{'${'$'}'}index")
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "processing ${'$'}{'${'$'}'}{macro[index].fileName} -> ${'$'}{'${'$'}'}{destinationPreProcessed.name}")
//    destinationPreProcessed.createNewFile()
//    val lex = Lexer(fileToByteArray(File(src)), globalVariables.tokensNewLine)
//    lex.lex()
//    if (preprocessor.base.globalVariables.flags.debug) println(
//        preprocessor.base.globalVariables.depthAsString() + "lex.currentLine is ${'$'}{'${'$'}'}{lex.currentLine
//            ?.replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())}"
//    )
//    while (lex.currentLine != null) {
//        val out = parse(lex, macro)
//        if (out == null) return
//        var input = lex.currentLine as String
//        if (input[input.length - 1] == '\n') {
//            input = input.dropLast(1)
//        }
//        if (preprocessor.base.globalVariables.flags.debug) println(
//            preprocessor.base.globalVariables.depthAsString() + "\ninput = ${'$'}{'${'$'}'}input"
//                .replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())
//        )
//        if (preprocessor.base.globalVariables.flags.debug) println(
//            preprocessor.base.globalVariables.depthAsString() + "output = ${'$'}{'${'$'}'}out\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
//                .replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())
//        )
//        if (globalVariables.firstLine) {
//            destinationPreProcessed.writeText(out + "\n")
//            globalVariables.firstLine = false
//        } else destinationPreProcessed.appendText(out + "\n")
//        lex.lex()
//    }
//}
//

/**
 * adds each **line** to the given [macro][Macro] list
 *
 * assumes each **line** is a valid **[#][Macro.Directives.value][define][Macro.Directives.Define.value]** directive
 */
fun processDefine(line: String, macro: MutableList<Macro>) {
    val fullMacro: String = line.trimStart().removePrefix(Macro().Directives().value).trimStart()
    if (fullMacro.substringAfter(' ').isBlank()) {
        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "empty define statement")
        return
    }

    val index = macro.size - 1
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "saving macro in to index ${'$'}{'${'$'}'}index")
    var macroIndex = macro[index].macros.size - 1
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "saving macro in to macro index ${'$'}{'${'$'}'}macroIndex")
    // to include the ability to redefine existing definitions, we must save to local variables first
    // determine token type
    // Line is longer than allowed by code style (> 120 columns)
    val type: String = if (fullMacro.substringAfter(' ')
            .trimStart()
            .substringBefore(' ')
            .trimStart() == fullMacro.substringAfter(' ')
            .trimStart()
            .substringBefore(' ')
            .trimStart().substringBefore('(')
    )
        Macro().Directives().Define().Types().Object
    else
        Macro().Directives().Define().Types().Function
    var token: String
    if (type == Macro().Directives().Define().Types().Object) {
        var empty = false
        // object
        token =
            fullMacro.substringAfter(' ').trimStart().substringBefore(' ').trimStart()
        if (token[token.length - 1] == '\n') {
            token = token.dropLast(1)
            empty = true
        }
        val i = macroExists(token, type, index, macro)
        macroIndex = if (globalVariables.status.currentMacroExists) {
            i
        } else {
            if (macro[index].macros[macroIndex].fullMacro != null) {
                realloc(
                    macro[index].macros,
                    macro[index].macros.size + 1
                )
            }
            macro[index].macros.size - 1
        }
        macro[index].macros[macroIndex].fullMacro = line.trimStart().trimEnd()
        macro[index].macros[macroIndex].identifier = token
        macro[index].macros[macroIndex].type = type
        if (!empty) {
            macro[index].macros[macroIndex].replacementList =
                fullMacro.substringAfter(' ').trimStart().substringAfter(' ').trimStart().trimStart()
        } else macro[index].macros[macroIndex].replacementList = ""
    } else {
        // function
        token =
            fullMacro.substringAfter(' ').substringBefore('(').trimStart()
        val i = macroExists(token, type, index, macro)
        macroIndex = if (globalVariables.status.currentMacroExists) {
            i
        } else {
            if (macro[index].macros[macroIndex].fullMacro != null) {
                realloc(
                    macro[index].macros,
                    macro[index].macros.size + 1
                )
            }
            macro[index].macros.size - 1
        }
        macro[index].macros[macroIndex].fullMacro = line.trimStart().trimEnd()
        macro[index].macros[macroIndex].identifier = token
        macro[index].macros[macroIndex].type = type
        // obtain the function arguments
        val t = macro[index].macros[macroIndex].fullMacro?.substringAfter(' ')!!
        val b = Balanced()
        macro[index].macros[macroIndex].arguments = extractArguments(b.extractText(t).drop(1).dropLast(1))
        macro[index].macros[macroIndex].replacementList = if (b.end[0] >= t.length) null
        else t.substring(b.end[0] + 1).trimStart()
    }
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "type       = ${'$'}{'${'$'}'}{macro[index].macros[macroIndex].type}")
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "token      = ${'$'}{'${'$'}'}{macro[index].macros[macroIndex].identifier}")
    if (macro[index].macros[macroIndex].arguments != null)
        if (preprocessor.base.globalVariables.flags.debug) println(
            preprocessor.base.globalVariables.depthAsString() +
                    "arguments  = ${'$'}{'${'$'}'}{macro[index].macros[macroIndex].arguments}"
        )
    if (preprocessor.base.globalVariables.flags.debug) println(
        preprocessor.base.globalVariables.depthAsString() +
                "replacementList      = ${'$'}{'${'$'}'}{macro[index].macros[macroIndex].replacementList
                    ?.replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())
                }"
    )
    macroList(index, macro)
    // definition names do not expand
    // definition values do expand
}
\"\"\"
                    ),
                    listOf(
                        "base",
                        "globalVariables.kt", "linuxMain/kotlin/preprocessor/base/globalVariables.kt", \"\"\"package preprocessor.base

import preprocessor.globals.Globals

/**
 * @see Globals
 */
val globalVariables: Globals = Globals()
\"\"\"
                    ),
                    listOf(
                        "globals",
                        "Globals.kt", "linuxMain/kotlin/preprocessor/globals/Globals.kt", \"\"\"package preprocessor.globals

import preprocessor.core.Macro
import preprocessor.utils.core.basename
//import java.io.File

/**
 * the globals class contains all global variables used by this library
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class Globals {

    val version = 1.0

    val flags = Flags()
    val status = Status()

    /**
     * `<space> or <tab>`
     * @see tokens
     */
    val tokensSpace: String = " \t"

    /**
     * `<newline>`
     *
     * (
     *
     * \n or
     *
     * "new
     *
     * line"
     *
     * )
     * @see tokens
     */
    val tokensNewLine: String = "\n"

    /**
     * ```
     * \
     * "
     * /
     * *
     * ```
     * [Macro.Directives.value]
     * ```
     * (
     * )
     * .
     * ,
     * -
     * >
     * {
     * }
     * [
     * ]
     * ```
     * @see tokens
     */
    val tokensExtra: String = "\\\"'/*${'$'}{'${'$'}'}{Macro().Directives().value}().,->{}[]"
    /**
     * ```
     * +
     * -
     * *
     * /
     * ```
     * @see tokens
     */
    val tokensMath: String = "+-*/"

    /**
     * the Default list of tokens
     *
     * this is used in general tokenization and [Macro] expansion
     *
     * **tokens = [tokensSpace] + [tokensNewLine] + [tokensExtra] + [tokensMath]**
     */
    val tokens: String = tokensSpace + tokensNewLine + tokensExtra + tokensMath

    /**
     * the current depth
     */
    var depth: Int = 0
    /**
     * returns a depth string
     */
    fun depthAsString(): String = "    ".repeat(depth) + "depth:${'$'}{'${'$'}'}depth > "

//    /**
//     * the current project directory that this task has been called from
//     * @see projectDirectoryBaseName
//     * @see rootDirectory
//     */
//    var projectDirectory: File? = null
//    /**
//     * the basename of [projectDirectory]
//     * @see rootDirectoryBaseName
//     */
//    var projectDirectoryBaseName: String? = null
//    /**
//     * the root project directory
//     * @see rootDirectoryBaseName
//     * @see projectDirectory
//     */
//    var rootDirectory: File? = null
//    /**
//     * the basename of [rootDirectory]
//     * @see projectDirectoryBaseName
//     */
//    var rootDirectoryBaseName: String? = null
//
//    /**
//     * the Default [macro][Macro] list
//     */
//    var kppMacroList: MutableList<Macro> = mutableListOf(Macro())
//
//    /**
//     * the directory that **kpp** is contained in
//     */
//    var kppDir: String? = null
//    /**
//     * the directory that **kpp** is contained in
//     */
//    var kppDirAsFile: File? = null
//    /**
//     * the suffix to give files that have been processed by kpp
//     */
//    var preprocessedExtension: String = ".preprocessed"
//
//    /**
//     * initializes the global variables
//     *
//     * it is recommended to call this function as `Globals().initGlobals(rootDir, projectDir)`
//     *
//     * replace `Globals()` with your instance of the `Globals` class
//     * @sample globalsSample
//     */
//    fun initGlobals(rootDir: File, projectDir: File) {
//        projectDirectory = projectDir
//        projectDirectoryBaseName = basename(projectDirectory)
//        rootDirectory = rootDir
//        rootDirectoryBaseName = basename(rootDirectory)
//        kppDir = rootDirectory.toString() + "/kpp"
//        kppDirAsFile = File(kppDir)
//    }
//
//    /**
//     * initializes the global variables
//     *
//     * it is recommended to call this function as `Globals().initGlobals(rootDir, projectDir)`
//     *
//     * replace `Globals()` with your instance of the `Globals` class
//     * @sample globalsSample
//     */
//    fun initGlobals(rootDir: String, projectDir: String) {
//        initGlobals(File(rootDir), File(projectDir))
//    }
}

//private fun globalsSample(rootDir: File, projectDir: File) {
//    val globals = Globals()
//    globals.initGlobals(rootDir, projectDir)
//    //rootDir is usually provided within the task itself
//    //projectDir is usually provided within the task itself
//}\"\"\",
                        "Flags.kt", "linuxMain/kotlin/preprocessor/globals/Flags.kt", \"\"\"package preprocessor.globals

/**
 * global flags that affect how the preprocessor behaves
 */
class Flags {
    /**
     * prints debug output if this value is true
     */
    var debug: Boolean = false
}
\"\"\",
                        "Status.kt", "linuxMain/kotlin/preprocessor/globals/Status.kt", \"\"\"package preprocessor.globals

/**
 * status variables
 */
class Status {
    /**
     * this is used by [testFile][preprocessor.utils.Sync.testFile]
     */
    var currentFileContainsPreprocessor: Boolean = false

    /*
        TODO: implement file cache
        var currentFileIsCashed: Boolean = false
        var cachedFileContainsPreprocessor: Boolean = false
    */

    /**
     *
     */
    var firstLine: Boolean = true
    /**
     *
     */
    var currentMacroExists: Boolean = false
    /**
     *
     */
    var abortOnComplete: Boolean = true

}
\"\"\"
                    ),
                    listOf(
                        "test",
                        "Tests.kt", "linuxMain/kotlin/preprocessor/test/Tests.kt", \"\"\"package preprocessor.test

import preprocessor.test.tests.*
import preprocessor.utils.core.abort

/**
 * holds all the tests for this library
 */
class Tests {
    private fun begin(name: String = "Tests", message: String = "starting ${'$'}{'${'$'}'}name"): Unit =
        println(message)

    private fun end(name: String = "Tests", message: String = "${'$'}{'${'$'}'}name finished"): Unit =
        println(message)

    /**
     * if this value is true, the function will abort if all tests pass
     */
    var abortOnComplete: Boolean = false

    fun doAll() {
        begin()
        general()
        stringize()
        selfReferencing()
        if (abortOnComplete) {
            end()
//            abort("All Tests Passed")
        }
    }
}\"\"\",
                        listOf(
                            "tests",
                            "selfReferencing.kt", "linuxMain/kotlin/preprocessor/test/tests/selfReferencing.kt", \"\"\"package preprocessor.test.tests

import preprocessor.test.init
import preprocessor.test.utils.expect
import preprocessor.utils.extra.parse

/**
 * tests self referencing macros
 */
fun selfReferencing() {
    val m = init()
    parse("#define a a", m)
    expect("a", "a", m)
    parse("#define d l k j\n", m)
    expect("d", "l k j", m)
    parse("#define a(x) b() x\n" +
                "#define b(x) c() x\n" +
                "#define c(x) d a() x\n", m
    )
    expect("a(\"3\" \"2\" \"1\" \"x\")", "l k j a() \"3\" \"2\" \"1\" \"x\"", m)
    parse("#define x(x) x\n", m)
    expect("x(a(\"3\" \"2\" \"1\" \"x\"))", "l k j a() \"3\" \"2\" \"1\" \"x\"", m)
}\"\"\",
                            "stringize.kt", "linuxMain/kotlin/preprocessor/test/tests/stringize.kt", \"\"\"package preprocessor.test.tests

import preprocessor.test.init
import preprocessor.test.utils.expect
import preprocessor.utils.extra.parse

/**
 * tests stringize
 */
fun stringize() {
    val m = init()
    parse(" #define s(x) #x -> x", m)
    expect("s(a)", "\"a\" -> a", m)

    parse("#define a b", m)
    expect("s(a b a)", "\"a b a\" -> b b b", m)
    expect("s(a    b    a)", "\"a b a\" -> b b b", m)
}\"\"\",
                            "general.kt", "linuxMain/kotlin/preprocessor/test/tests/general.kt", \"\"\"package preprocessor.test.tests

import preprocessor.test.init
import preprocessor.test.utils.expect
import preprocessor.utils.core.abort
import preprocessor.utils.extra.parse

/**
 * tests general capabilities
 */
fun general() {
    val m = init()
    parse(
        "#define a(b) b\n" +
                "#define c() d c\n" +
                "#define d Once\n",
        m
    )
    expect("a(    c()    )", "Once c", m)

    parse("#define c() d d", m)
    expect("a(    c()    )", "Once Once", m)
    expect("d", "Once", m)

    parse(
        "#define a(b, y) b y\n" +
                "#define c() d c\n" +
                "#define e() d c\n",
        m
    )
    expect("a(    c()    ,    e()    )", "Once c Once c", m)

    parse("#define a b", m)
    expect("a", "b", m)

    parse("#define f g", m)
    expect("f", "g", m)

    parse("#define x y", m)
    expect("x", "y", m)
    expect("a", "b", m)
    expect("aa", "aa", m)
    expect("a a", "b b", m)
    expect("a N a", "b N b", m)

    parse("#define a(x) {x}", m)
    expect("a()k", "{}k", m)

    parse("#define a(x) {b()x}", m)
    parse("#define b(x) {x}", m)
    expect("a()k", "{{}}k", m)

    parse("#define a(x) {b() x}", m)
    parse("#define b(x) { x}", m)
    expect("a()k", "{{ } }k", m)

    parse("#define a(x) [b() x]", m)
    parse("#define b(x) [1 x", m)
    expect("a(2)", "[[1 2]", m)

    parse("#define b(x) [1 x]", m)
    expect("a(2)", "[[1 ] 2]", m)

    parse("#define b(x) [1 x ]", m)
    expect("a(2)", "[[1 ] 2]", m)

    parse("#define n(x) x", m)
    expect("n(\nG\n)", "G", m)
}\"\"\"
                        ),
                        "init.kt", "linuxMain/kotlin/preprocessor/test/init.kt", \"\"\"package preprocessor.test

import preprocessor.core.Macro

/**
 * initializes the Macro list
 * @return a new Macro list
 */
fun init(): MutableList<Macro> {
    return mutableListOf<Macro>(Macro())
}\"\"\",
                        listOf(
                            "utils",
                            "expect.kt", "linuxMain/kotlin/preprocessor/test/utils/expect.kt", \"\"\"package preprocessor.test.utils

import preprocessor.core.Macro
import preprocessor.utils.core.abort
import preprocessor.utils.extra.parse

/**
 * [parses][parse] given [input] against its result against [output]
 *
 * @return true if [output] matches
 * otherwise false
 *
 * @param input the string to be checked
 * @param output the string that should result from the parsing of [input]
 * @param macro the given macro list
 */
fun expect(input: String, output: String, macro: MutableList<Macro>): Boolean {
    val check = parse(input, macro)
    return when {
        check == null -> false
        check.equals(output) -> {
            println("expect(\"${'$'}{'${'$'}'}input\", \"${'$'}{'${'$'}'}output\", <macro>) passed")
            true
        }
        else -> {
            println("expect(\"${'$'}{'${'$'}'}input\", \"${'$'}{'${'$'}'}output\", <macro>) failed")
            val t = "    "
            val e = t + "expected  : "
            val g = t + "got       : "
            val i = t + "input was : "
            println(e + output.replace("\n", "\n" + " ".repeat(e.length)))
            println(g +  check.replace("\n", "\n" + " ".repeat(g.length)))
            println(i +  input.replace("\n", "\n" + " ".repeat(i.length)))
            abort()
        }
    }
}
\"\"\"
                        )
                    ),
                    listOf(
                        "utils",
                        listOf(
                            "conversion",
                            "fileToByteBuffer.kt", "linuxMain/kotlin/preprocessor/utils/conversion/fileToByteBuffer.kt", \"\"\"//package preprocessor.utils.conversion
//
//import java.io.File
//import java.io.RandomAccessFile
//import java.nio.ByteArray
//
///**
// * converts a [File] into a [ByteArray]
// * @return the resulting conversion
// * @see stringToByteArray
// */
//fun fileToByteArray(f: File): ByteArray {
//    val file = RandomAccessFile(f, "r")
//    val fileChannel = file.channel
//    val buffer = ByteArray.allocate(fileChannel.size().toInt())
//    fileChannel.read(buffer)
//    buffer.flip()
//    return buffer
//}
\"\"\"
                        ),
                        "Sync.kt", "linuxMain/kotlin/preprocessor/utils/Sync.kt", \"\"\"//package preprocessor.utils
//
//import preprocessor.base.globalVariables
//import preprocessor.core.macroList
//import preprocessor.core.process
//import java.io.File
//import preprocessor.utils.core.*
//import java.nio.file.Files
//
///**
// *
// *//*
//if A/FILE exists
//    if B/FILE does not exist
//        delete A/FILE
//            if A/FILE.PRO exists
//                delete A/FILE.PRO
//    else
//        if A/FILE contains DATA
//            KEEP A/FILE
//            if A/FILE.PRO exists
//                if A/FILE contains DATA
//                    KEEP A/FILE.PRO
//        else delete A/FILE
//            if A/FILE.PRO exists
//                delete A/FILE.PRO
//else if B/FILE exists
//    if B/FILE contains DATA
//        copy B/FILE to A/FILE
// */
//class Sync {
//
//    private val ignore: MutableList<String> = mutableListOf(
//        // ignore png files
//        "png",
//        // ignore proguard files
//        "pro",
//        // ignore gradle files
//        "gradle",
//        // ignore module files
//        "iml",
//        // ignore git files
//        "gitignore"
//    )
//
//    /**
//     * syncs directory **B** (**src**) with directory **A** (**dir**)
//     *
//     * this needs to be called BEFORE [syncA] in order to sync correctly
//     * @see syncA
//     * @sample findSourceFiles
//     */
//    private fun syncB(dir: File, src: File, extension: String? = null) {
//        dir.listFiles().forEach {
//            val a = it
//            val b = File(
//                // Line is longer than allowed by code style (> 120 columns)
//                globalVariables.rootDirectory.toString() + '/' + basename(globalVariables.kppDir) +
//                        a.toString().removePrefix(globalVariables.rootDirectory!!.path)
//            )
//            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "A :     ${'$'}{'${'$'}'}a")
//            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "B : ${'$'}{'${'$'}'}b")
//            if (a.exists()) {
//                if (a.isDirectory) {
//                    // ignore build dir in A
//                    // if build dir exists in B, delete it
//                    val blocked = a.toString() == globalVariables.projectDirectory?.path + "/build"
//                    if (!blocked) {
//                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "entering ${'$'}{'${'$'}'}a")
//                        syncB(a, src, extension)
//                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "leaving ${'$'}{'${'$'}'}a")
//                    } else if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "A is blocked")
//                    if (b.exists()) {
//                        if (blocked) {
//                            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting B")
//                            deleteRecursive(b)
//                        } else if (empty(b)) {
//                            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting B")
//                            delete(b)
//                        }
//                    }
//                    val aPreProcessed = File(a.path + globalVariables.preprocessedExtension + "." + a.extension)
//                    val bPreProcessed = File(b.path + globalVariables.preprocessedExtension + "." + b.extension)
//                    if (aPreProcessed.exists()) {
//                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting preprocessing file associated with A")
//                        delete(aPreProcessed)
//                    }
//                    if (bPreProcessed.exists()) {
//                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting preprocessing file associated with B")
//                        delete(bPreProcessed)
//                    }
//                } else if (a.isFile) {
//                    if (a.path.endsWith(globalVariables.preprocessedExtension + "." + a.extension)) {
//                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "A is preprocessor file")
//                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting A")
//                        delete(a)
//                    } else if (!ignore.contains(a.extension)) {
//                        // if extension is null, test every file
//                        // ignore these extensions
//                        if (it.extension == extension || extension == null) {
//                            if (!b.exists()) {
//                                if (testFile(a!!)) {
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "copying A to B")
//                                    if (!cp(a.path, b.path, true))
//                                        abort(preprocessor.base.globalVariables.depthAsString() + "failed to copy ${'$'}{'${'$'}'}a to ${'$'}{'${'$'}'}b")
//                                } else {
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "B does not exist however A does not contain DATA")
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "B cannot be deleted as it does not exist")
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "A will not be copied to B")
//                                }
//                            } else {
//                                if (!testFile(b)) {
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "B exists however does not contains DATA")
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting B")
//                                    delete(b)
//                                } else if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "B contains DATA")
//                            }
//                        }
//                    } else {
//                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "ignoring extension: ${'$'}{'${'$'}'}{a.extension}")
//                        if (b.exists()) {
//                            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "A is ignoring but B exists, deleting B")
//                            delete(b)
//                        }
//                        val aPreProcessed = File(a.path + globalVariables.preprocessedExtension + "." + a.extension)
//                        val bPreProcessed = File(b.path + globalVariables.preprocessedExtension + "." + b.extension)
//                        if (aPreProcessed.exists()) {
//                            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting preprocessing file associated with A")
//                            delete(aPreProcessed)
//                        }
//                        if (bPreProcessed.exists()) {
//                            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting preprocessing file associated with B")
//                            delete(bPreProcessed)
//                        }
//                    }
//                }
//            } else {
//                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "A does not exist")
//            }
//        }
//    }
//
//    /**
//     * syncs directory **A** (**dir**) with directory **B** (**src**)
//     *
//     * this needs to be called AFTER [syncB] in order to sync correctly
//     * @see syncB
//     * @sample findSourceFiles
//     */
//    private fun syncA(dir: File, src: File, extension: String? = null) {
//        dir.listFiles().forEach {
//            val a = it
//            val b = File(
//                globalVariables.rootDirectory.toString() + '/' + a.toString().removePrefix(globalVariables.kppDir!!)
//            )
//            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "A : ${'$'}{'${'$'}'}a")
//            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "B :     ${'$'}{'${'$'}'}b")
//            run returnPoint@{
//                if (a.exists()) {
//                    if (a.isDirectory) {
//                        // ignore build dir in A
//                        // if build dir exists in B, delete it
//                        val blocked = a.toString() == globalVariables.projectDirectory?.path + "/build"
//                        if (!blocked) {
//                            if (b.path == globalVariables.kppDir) {
//                                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "error: B is kpp dir")
//                                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "kpp should not contain its own directory")
//                                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting A")
//                                deleteRecursive(a)
//                                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "returning to @returnPoint")
//                                return@returnPoint
//                            } else {
//                                if (b.exists()) {
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "entering ${'$'}{'${'$'}'}a")
//                                    syncA(a, src, extension)
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "leaving ${'$'}{'${'$'}'}a")
//                                    if (empty(a)) {
//                                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting A")
//                                        delete(a)
//                                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "returning to @returnPoint")
//                                        return@returnPoint
//                                    }
//                                } else {
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "B does not exist")
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting A")
//                                    deleteRecursive(a)
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "returning to @returnPoint")
//                                    return@returnPoint
//                                }
//                            }
//                        } else if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "A is blocked")
//                        if (b.exists()) {
//                            if (blocked) {
//                                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting A")
//                                deleteRecursive(a)
//                                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "returning to @returnPoint")
//                                return@returnPoint
//                            } else if (empty(a)) {
//                                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting A")
//                                delete(a)
//                                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "returning to @returnPoint")
//                                return@returnPoint
//                            }
//                        }
//                    } else if (a.isFile) {
//                        // if B does not exist, delete A
//                        if (!b.exists()) {
//                            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "B does not exist")
//                            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deleting A")
//                            delete(a)
//                            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "returning to @returnPoint")
//                            return@returnPoint
//                        }
//                        // if extension is null, test every file
//                        // ignore these extensions
//                        if (!ignore.contains(a.extension)) {
//                            if (it.extension == extension || extension == null) {
//                                if (!testFile(a!!)) {
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "A exists however does not contains DATA")
//                                    if (!a.path.endsWith(globalVariables.preprocessedExtension + "." + extension)) {
//                                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "moving A to B")
//                                        if (!mv(a.path, b.path, verbose = true, overwrite = true))
//                                            abort()
//                                    } else {
//                                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "A is preprocessor file")
//                                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "moving A to B (renamed)")
//                                        if (!mv(
//                                                a.path,
//                                                // Line is longer than allowed by code style (> 120 columns)
//                                                b.path.removeSuffix(
//                                                    "." +
//                                                            globalVariables.preprocessedExtension +
//                                                            "." +
//                                                            extension
//                                                ),
//                                                verbose = true,
//                                                overwrite = true
//                                            )
//                                        )
//                                            abort()
//                                    }
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "returning to @returnPoint")
//                                    return@returnPoint
//                                } else {
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "A contains DATA")
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "processing A")
//                                    if (a.extension == "") {
//                                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "error: cannot process a file with no extension")
//                                        return@returnPoint
//                                    }
//                                    macroList(globalVariables.kppMacroList)
//                                    process(a.path, a.extension, globalVariables.kppMacroList)
//                                    macroList(globalVariables.kppMacroList)
//                                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "moving resulting preprocessing file A to B (renamed)")
//                                    if (!mv(
//                                            a.path + globalVariables.preprocessedExtension + "." + a.extension,
//                                            b.path,
//                                            verbose = true,
//                                            overwrite = true
//                                        )
//                                    )
//                                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "returning to @returnPoint")
//                                    return@returnPoint
//                                }
//                            }
//                        } else {
//                            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "ignoring extension: ${'$'}{'${'$'}'}{a.extension}")
//                            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "returning to @returnPoint")
//                            return@returnPoint
//                        }
//                    }
//                } else {
//                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "A does not exist")
//                }
//            }
//        }
//    }
//
//    /**
//     * self explanatory
//     *
//     * this function finds and processes all source files in the directory **dir** with the extension **extension**
//     * @param dir the directory to search in
//     * @param extension the extension that each file must end
//     * @param extension if **null** then any file is accepted
//     * @sample findSourceFilesSample
//     */
//    @Suppress("MemberVisibilityCanBePrivate")
//    fun findSourceFiles(dir: File, extension: String? = null) {
//        // sync dir with kppDir
//        syncB(dir, globalVariables.kppDirAsFile as File, extension)
//        // sync kppDir with dir, calling process on a valid processing file
//        syncA(globalVariables.kppDirAsFile as File, dir, extension)
//    }
//
//
//    /**
//     * self explanatory
//     *
//     * this function finds and processes all source files in the directory **dir** with the extension **extension**
//     * @param dir the directory to search in
//     * @param extension the extension that each file must end
//     * @param extension if **null** then any file is accepted
//     * @sample findSourceFilesSample
//     */
//
//    @Suppress("MemberVisibilityCanBePrivate")
//    fun findSourceFilesOrNull(dir: File?, extension: String? = null) {
//        if (dir == null) abort(preprocessor.base.globalVariables.depthAsString() + "dir cannot be null")
//        findSourceFiles(dir, extension)
//    }
//
//    /**
//     * self explanatory
//     *
//     * this function finds and processes all source files in the directory **dir** with the extension **extension**
//     * @param dir the directory to search in
//     * @param extension the extension that each file must end
//     * @param extension if **null** then any file is accepted
//     * @sample findSourceFilesSample
//     */
//    @Suppress("MemberVisibilityCanBePrivate")
//    fun findSourceFiles(dir: String, extension: String? = null) {
//        findSourceFiles(File(dir), extension)
//    }
//
//    private fun findSourceFilesSample() {
//        val path =
//            globalVariables.projectDirectory.toString()
//        // find all source files with kotlin extension
//        findSourceFiles(path, "kt")
//        // find all source files, regardless of its extension
//        findSourceFiles(path)
//    }
//
//    /**
//     * test if file **src** contains any preprocessor directives
//     */
//    private fun testFile(src: File): Boolean {
//        globalVariables.currentFileContainsPreprocessor = false
//        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "testing file: ${'$'}{'${'$'}'}{src.path}")
//        val lines: List<String> = Files.readAllLines(src.toPath())
//        lines.forEach { line ->
//            checkIfPreprocessorIsNeeded(line)
//        }
//        return globalVariables.currentFileContainsPreprocessor
//    }
//
//    private fun checkIfPreprocessorIsNeeded(line: String) {
//        if (line.trimStart().startsWith(preprocessor.core.Macro().Directives().value)) globalVariables.currentFileContainsPreprocessor = true
//    }
//}\"\"\",
                        listOf(
                            "class",
                            listOf(
                                "extensions",
                                "Comparable.kt", "linuxMain/kotlin/preprocessor/utils/class/extensions/Comparable.kt", \"\"\"@file:Suppress("unused")

package preprocessor.utils.`class`.extensions

infix fun <T: Comparable<T>> T.isGreaterThan(i: T) = this > i
infix fun <T: Comparable<T>> T.isGreaterThanOrEqualTo(i: T) = this >= i
infix fun <T: Comparable<T>> T.isLessThan(i: T) = this < i
infix fun <T: Comparable<T>> T.isLessThanOrEqualTo(i: T) = this <= i
infix fun <T: Comparable<T>> T.isEqualTo(i: T) = this == i
infix fun <T: Comparable<T>> T.isNotEqualTo(i: T) = this != i
\"\"\",
                                "Boolean.kt", "linuxMain/kotlin/preprocessor/utils/class/extensions/Boolean.kt", \"\"\"@file:Suppress("unused")

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
    println("x = ${'$'}{'${'$'}'}x") // x = abc
    val y = x.ifTrue(x.startsWith('a')) {
        it.length
    }
    println("y = ${'$'}{'${'$'}'}y") // y = 3
    println("x = ${'$'}{'${'$'}'}x") // x = abc
    val yx = x.ifTrueReturn(x.startsWith('a')) {
        x = it.drop(1) // i want "it" to modify "x" itself
    }
    println("yx = ${'$'}{'${'$'}'}yx") // yx = true
    println("x = ${'$'}{'${'$'}'}x") // x = abc // should be "bc"
}
\"\"\",
                                "String.kt", "linuxMain/kotlin/preprocessor/utils/class/extensions/String.kt", \"\"\"@file:Suppress("unused")

// find         : \{\n    return if \(this \=\= null\) null\n    else this\.(.*)\n\}
// replace with : = this\?\.${'$'}{'${'$'}'}1


package preprocessor.utils.`class`.extensions

import preprocessor.core.Parser
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
    delimiters.forEach {str ->
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
fun String?.tokenizeVararg(vararg delimiters: String, returnDelimiters: Boolean = false): List<String>? = this?.tokenizeVararg(delimiters = *delimiters, returnDelimiters = returnDelimiters)
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
    val d = delimiters.toStack()
    d.forEach {str ->
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
fun String?.tokenize(delimiters: String, returnDelimiters: Boolean = false): List<String>? = this?.tokenize(delimiters, returnDelimiters)
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
    val parser = Parser(this) { it.toStack() }
    val t = parser.IsSequenceOneOrMany(token)
    while (parser.peek() != null) {
        if (t.peek()) {
            t.pop()
            str.append(token)
        }
        else str.append(parser.pop()!!)
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
    val parser = Parser(this) { it.toStack() }
    val t = parser.IsSequenceOneOrMany(token)
    while (parser.peek() != null) {
        if (t.peek()) {
            t.pop()
            str.append(replaceWith)
        }
        else str.append(parser.pop()!!)
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
    val parser = Parser(this) { it.toStack() }
    val t = parser.IsSequenceOnce(token)
    while (parser.peek() != null) {
        if (t.peek()) {
            t.pop()
            str.append(to)
        }
        else str.append(parser.pop()!!)
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
    val parser = Parser(this) { it.toStack() }
    val t = parser.IsSequenceOnce(token)
    while (parser.peek() != null) {
        if (t.peek()) {
            t.pop()
            str.append(if (!t.peek()) last else to)
        }
        else str.append(parser.pop()!!)
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
 * converts a [String] into a [Stack]
 * @see Stack.toStringConcat
 * @return the resulting conversion
 */
fun String.toStack(): Stack<String> {
    val deq = Stack<String>()
    var i = 0
    while (i < this.length) deq.addLast(this[i++].toString())
    return deq
}

/**
 * converts a [String] into a [Stack]
 * @see Stack.toStringConcat
 * @return the resulting conversion
 */
fun String?.toStack(): Stack<String>? {
    return if (this == null) return null
    else this.toStack()
}

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

fun String.padExtendEnd(to: Int, str: String) = if(to - this.length isGreaterThan 0) {
    val build = this.toStringBuilder(max(this.length, to));
    val m = str.lastIndex
    var i = 0
    while(build.length isLessThan to) {
        build.append(str[i])
        if (i isEqualTo m) i = 0 else i++
    }
    build.toString()
} else if(to - this.length isEqualTo 0) this else {
    this.padShrinkEnd(to, str)
}

fun String.padExtendEnd(to: Int, char: Char): String = this.padExtendEnd(to, char.toString())

fun String.padExtendEnd(to: Int): String = this.padExtendEnd(to, this[0])

fun String?.padExtendEnd(to: Int): String? = this?.padExtendEnd(to)

fun String?.padExtendEnd(to: Int, char: Char): String? = this?.padExtendEnd(to, char)

fun String.padExtendStart(to: Int, str: String): String = if(to - this.length isGreaterThan 0) {
    val build = this.reversed().toStringBuilder()
    val m = str.lastIndex
    var i = 0
    while(build.length isLessThan to) {
        build.append(str[i])
        if (i isEqualTo m) i = 0 else i++
    }
    build.reverse().toString()
} else if(to - this.length isEqualTo 0) this else {
    this.padShrinkStart(to, str)
}

fun String.padExtendStart(to: Int, char: Char): String = this.padExtendStart(to, char.toString())

fun String.padExtendStart(to: Int): String = this.padExtendStart(to, this[0])

fun String?.padExtendStart(to: Int): String? = this?.padExtendStart(to)

fun String?.padExtendStart(to: Int, char: Char): String? = this?.padExtendStart(to, char)

fun String.padShrinkEnd(to: Int, str: String): String = if(this.length - to isGreaterThan 0) {
    val thisLength = this.length
    val build = this.take(to).toStringBuilder()
    val m = str.lastIndex
    var i = 0
    while(build.length isLessThan thisLength) {
        build.append(str[i])
        if (i isEqualTo m) i = 0 else i++
    }
    build.toString()
} else if(this.length - to isEqualTo 0) this else {
    this.padExtendEnd(to, str)
}

fun String.padShrinkEnd(to: Int, char: Char): String = this.padShrinkEnd(to, char.toString())

fun String.padShrinkEnd(to: Int): String = this.padShrinkEnd(to, this[0])

fun String?.padShrinkEnd(to: Int): String? = this?.padShrinkEnd(to)

fun String?.padShrinkEnd(to: Int, char: Char): String? = this?.padShrinkEnd(to, char)

fun String.padShrinkStart(to: Int, str: String): String = if(this.length - to isGreaterThan 0) {
    val thisLength = this.length
    val build = this.reversed().take(to).toStringBuilder()
    val m = str.lastIndex
    var i = 0
    while(build.length isLessThan thisLength) {
        build.append(str[i])
        if (i isEqualTo m) i = 0 else i++
    }
    build.reverse().toString()
} else if(this.length - to isEqualTo 0) this else {
    this.padExtendStart(to, str)
}

fun String.padShrinkStart(to: Int, char: Char): String = this.padShrinkStart(to, char.toString())

fun String.padShrinkStart(to: Int): String = this.padShrinkStart(to, this[0])

fun String?.padShrinkStart(to: Int): String? = this?.padShrinkStart(to)

fun String?.padShrinkStart(to: Int, char: Char): String? = this?.padShrinkStart(to, char)
\"\"\"
                            )
                        ),
                        listOf(
                            "core",
                            "basename.kt", "linuxMain/kotlin/preprocessor/utils/core/basename.kt", \"\"\"package preprocessor.utils.core

/**
 * returns the basename of a string, if the string is **null* then returns **null**
 */
fun basename(s: Any?): String? {
    return if (s == null || !s.toString().contains('/')) {
        null
    } else s.toString().substringAfterLast('/')
}
\"\"\",
                            "copy.kt", "linuxMain/kotlin/preprocessor/utils/core/copy.kt", \"\"\"//package preprocessor.utils.core
//
//import java.io.File
//import java.io.IOException
//
///**
// * copy one file to another, optionally overwriting it
// * @return true if the operation succeeds, otherwise false
// * @see mv
// */
//fun cp(src: String, dest: String, verbose: Boolean = false, overwrite: Boolean = false): Boolean {
//    return try {
//        File(src).copyTo(File(dest), overwrite)
//        if (verbose) if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "${'$'}{'${'$'}'}src -> ${'$'}{'${'$'}'}dest")
//        true
//    } catch (e: IOException) {
//        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "failed to copy file ${'$'}{'${'$'}'}src to ${'$'}{'${'$'}'}dest")
//        false
//    }
//}
\"\"\",
                            "deleteRecursive.kt", "linuxMain/kotlin/preprocessor/utils/core/deleteRecursive.kt", \"\"\"//package preprocessor.utils.core
//
//import java.io.File
//
///**
// * deletes **src** and all sub directories
// *
// * [abort]s on failure
// */
//fun deleteRecursive(src: File) {
//    if (!src.exists()) {
//        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deletion of ${'$'}{'${'$'}'}{src.path} failed: file or directory does not exist")
//    }
//    if (!src.deleteRecursively()) {
//        abort(preprocessor.base.globalVariables.depthAsString() + "deletion of \"${'$'}{'${'$'}'}{src.path}\" failed")
//    }
//}
\"\"\",
                            "empty.kt", "linuxMain/kotlin/preprocessor/utils/core/empty.kt", \"\"\"//package preprocessor.utils.core
//
//import java.io.File
//
///**
// * returns true if **src** is an empty directory, otherwise returns false
// */
//fun empty(src: File): Boolean {
//    val files = src.listFiles() ?: return true
//    if (files.isEmpty()) return true
//    files.forEach {
//        if (it.isDirectory) return@empty empty(it)
//        else if (it.isFile) return@empty false
//        return@empty true
//    }
//    return false
//}
\"\"\",
                            "abort.kt", "linuxMain/kotlin/preprocessor/utils/core/abort.kt", \"\"\"package preprocessor.utils.core

/**
 * a wrapper for Exception, Default message is **Aborted**
 *
 * if gradle is used, abort using the following
 *
 * import org.gradle.api.GradleException
 *
 * ...
 *
 * throw GradleException(e)
 */
fun abort(e: String = "Aborted"): Nothing {
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "Aborting with error: ${'$'}{'${'$'}'}e")
    else println("Aborting with error: ${'$'}{'${'$'}'}e")
    throw Exception(e).also {ex ->
        println("stack trace:").also {
            ex.printStackTrace()
        }
    }
}
\"\"\",
                            "move.kt", "linuxMain/kotlin/preprocessor/utils/core/move.kt", \"\"\"//package preprocessor.utils.core
//
//import java.io.File
//import java.io.IOException
//
///**
// * moves one file to another, optionally overwriting it
// * @return true if the operation succeeds, otherwise false
// * @see cp
// */
//fun mv(src: String, dest: String, verbose: Boolean = false, overwrite: Boolean = false): Boolean {
//    return try {
//        File(src).copyTo(File(dest), overwrite)
//        if (verbose) if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "${'$'}{'${'$'}'}src -> ${'$'}{'${'$'}'}dest")
//        delete(File(src))
//        true
//    } catch (e: IOException) {
//        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "failed to move ${'$'}{'${'$'}'}src to ${'$'}{'${'$'}'}dest")
//        false
//    }
//}
\"\"\",
                            listOf(
                                "algorithms",
                                "LinkedList.kt", "linuxMain/kotlin/preprocessor/utils/core/algorithms/LinkedList.kt", \"\"\"package preprocessor.utils.core.algorithms

class LinkedList<T> : kotlin.collections.Iterable<T?> {

    inner class Node<T>(value: T?){
        var value:T? = value
        var next: Node<T>? = null
        var previous:Node<T>? = null
    }
    private var head:Node<T>? = null
    fun isEmpty(): Boolean = head == null
    fun first(): Node<T>? = head
    fun last(): Node<T>? {
        var node = head
        if (node != null){
            while (node?.next != null) {
                node = node.next
            }
            return node
        } else {
            return null
        }
    }
    fun count():Int {
        var node = head
        if (node != null){
            var counter = 1
            while (node?.next != null){
                node = node.next
                counter += 1
            }
            return counter
        } else {
            return 0
        }
    }
    fun nodeAtIndex(index: Int) : Node<T>? {
        if (index >= 0) {
            var node = head
            var i = index
            while (node != null) {
                if (i == 0) return node
                i -= 1
                node = node.next
            }
        }
        return null
    }
    fun append(value: T?) {
        var newNode = Node(value)
        var lastNode = this.last()
        if (lastNode != null) {
            newNode.previous = lastNode
            lastNode.next = newNode
        } else {
            head = newNode
        }
    }
    fun appendLast(value: T?) {
        var newNode = Node(value)
        var lastNode = this.last()
        if (lastNode != null) {
            newNode.previous = newNode
            lastNode.next = lastNode
        } else {
            head = newNode
        }
    }
    fun removeNode(node: Node<T>):T? {
        val prev = node.previous
        val next = node.next
        if (prev != null) {
            prev.next = next
        } else {
            head = next
        }
        next?.previous = prev
        node.previous = null
        node.next = null
        return node.value
    }
    fun removeLast() : T? {
        val last = this.last()
        if (last != null) {
            return removeNode(last)
        } else {
            return null
        }
    }
    fun removeAtIndex(index: Int):T? {
        val node = nodeAtIndex(index)
        if (node != null) {
            return removeNode(node)
        } else {
            return null
        }
    }
    override fun toString(): String {
        var s = "["
        var node = head
        while (node != null) {
            s += "${'$'}{'${'$'}'}{node.value}"
            node = node.next
            if (node != null) { s += ", " }
        }
        return s + "]"
    }

    fun contains(element: T): Boolean {
        var node = head
        while (node != null) {
            if (node.value == element) return true
            node = node.next
        }
        return false
    }

    override fun iterator(): kotlin.collections.Iterator<T?> {
        return object : kotlin.collections.Iterator<T?> {
            var node = head
            /**
             * Returns true if the iteration has more elements.
             *
             * @see next
             */
            override fun hasNext(): Boolean = node != null

            /**
             * Returns the next element in the iteration.
             *
             * NOTE: this always returns the **first** element when first called, not the **second** element
             *
             * @see hasNext
             */
            override fun next(): T? {
                if (node == null) throw NoSuchElementException()
                val var0 = node?.value
                node = node?.next
                return var0
            }
        }
    }
    /*
       fun clone(): LinkedList<T> {
            val tmp = LinkedList<T>()
            forEach { tmp.append(it) }
            return tmp
        }
     */
    fun clone(): LinkedList<T> = LinkedList<T>().also {l -> forEach { l.append(it) } }

    fun clear() { while (!isEmpty()) removeLast() }

    fun test() {
        val ll = LinkedList<String>()
        ll.append("John")
        println(ll)
        ll.append("Carl")
        println(ll)
        ll.append("Zack")
        println(ll)
        ll.append("Tim")
        println(ll)
        ll.append("Steve")
        println(ll)
        ll.append("Peter")
        println(ll)
        print("\n\n")
        println("first item: ${'$'}{'${'$'}'}{ll.first()?.value}")
        println("last item: ${'$'}{'${'$'}'}{ll.last()?.value}")
        println("second item: ${'$'}{'${'$'}'}{ll.first()?.next?.value}")
        println("penultimate item: ${'$'}{'${'$'}'}{ll.last()?.previous?.value}")
        println("\n4th item: ${'$'}{'${'$'}'}{ll.nodeAtIndex(3)?.value}")
        println("\nthe list has ${'$'}{'${'$'}'}{ll.count()} items")
    }
}\"\"\",
                                "Stack.kt", "linuxMain/kotlin/preprocessor/utils/core/algorithms/Stack.kt", \"\"\"package preprocessor.utils.core.algorithms

@Suppress("unused")
class Stack<T>() {

    var stack = LinkedList<T>()
    var size = 0
    fun addLast(value: T) = stack.append(value).apply { size = stack.count() }
    fun addLast(list: List<T>) { list.iterator().also { while(it.hasNext()) this.addLast(it.next()) } }
    fun push(value: T) = stack.append(value).apply { size = stack.count() }
    fun peek(): T? = stack.first()?.value
    fun pop(): T? = when { stack.isEmpty() -> throw NoSuchElementException() ; else -> stack.removeAtIndex(0).apply { size = stack.count() } }
    fun contains(s: T?): Boolean = stack.contains(s)
    override fun toString(): String = stack.toString()
    /**
     * returns this stack as a string with each element appended to the end of the string
     */
    fun toStringConcat(): String {
        val result = StringBuilder()
        val dq = stack.iterator()
        while (dq.hasNext()) {
            result.append(dq.next())
        }
        return result.toString()
    }

    fun iterator() = stack.iterator()
    fun forEach(action: (T?) -> Unit) = stack.forEach(action)
    fun clear() = stack.clear().apply { size = stack.count() }
    fun clone() = Stack<T>().also { it.stack = stack.clone() }

    fun test() {
        val s = Stack<String>()
        s.push("John")
        println(s)
        s.push("Carl")
        println(s)
        println("peek item: ${'$'}{'${'$'}'}{s.peek()}")
        println("pop item: ${'$'}{'${'$'}'}{s.pop()}")
        println("peek item: ${'$'}{'${'$'}'}{s.peek()}")
    }
}
\"\"\"
                            ),
                            "realloc.kt", "linuxMain/kotlin/preprocessor/utils/core/realloc.kt", \"\"\"@file:Suppress("unused")
package preprocessor.utils.core

import preprocessor.core.Macro

// NOT AVAILABLE IN KOTLIN-NATIVE
//
//import preprocessor.utils.core.classTools.instanceChain
//import preprocessor.utils.core.classTools.chain
//
//
///**
// * reallocates a [MutableList][kotlin.collections.MutableList] to the specified size
// *
// * this attempts to handle all types supported by [MutableList][kotlin.collections.MutableList] in the
// * initialization of a new element
// *
// * uses unsigned integers that are experimental: [UByte], [UInt], [ULong], [UShort]
// * @param v the list to resize
// * @param size the desired size to resize to
// * @sample reallocTest
// */
//@UseExperimental(ExperimentalUnsignedTypes::class)
//@Suppress("UNCHECKED_CAST")
//fun <E> realloc(v: kotlin.collections.MutableList<E?>, a : Any?, size: Int, isNullable: Boolean = true) {
      // the first parameter <E> (in which E is just the name, like a MACRO) is inferred from the return type of whatever it is invoked on
//    while (v.size != size) {
//        if (size > v.size) {
//            v.add(
//                run {
//                    if (a!!::class.javaPrimitiveType != null) {
//                        if (isNullable) null
//                        /** copied from
//                         * /.../kotlin-stdlib-1.3.21-sources.jar!/kotlin/Primitives.kt
//                         * /.../kotlin-stdlib-1.3.21-sources.jar!/kotlin/Boolean.kt
//                         *
//                         * unsigned integers are experimental: [UByte], [UInt], [ULong], [UShort]
//                         */
//                        else when (a!!) {
//                            is Byte -> 0
//                            is UByte -> 0U
//                            is Short -> 0
//                            is UShort -> 0U
//                            is Int -> 0
//                            is UInt -> 0U
//                            is Long -> 0L
//                            is ULong -> 0UL
//                            is Float -> 0.0F
//                            is Double -> 0.0
//                            is Char -> java.lang.Character.MIN_VALUE // null ('\0') as char
//                            is Boolean -> false
//                            else -> abort(preprocessor.base.globalVariables.depthAsString() + "unknown non-nullable type: ${'$'}{'${'$'}'}{a!!::class.javaPrimitiveType}")
//                        }
//                    } else {
//                        /*
//                        `::class.isInner` does not work with a Security Manager
//                        Exception in thread "main" java.lang.IllegalStateException: No BuiltInsLoader implementation was
//                        found. Please ensure that the META-INF/services/ is not stripped from your application and that
//                        the Java virtual machine is not running under a security manager
//                        */
//                        if (a!!::class.isInner) instanceChain(chain(a!!))
//                        else a!!::class.java.newInstance()
//                    }
//                } as E
//            )
//        } else {
//            v.remove(v.last())
//        }
//    }
//}
//
///**
// * reallocates a [MutableList][kotlin.collections.MutableList] to the specified size
// *
// * this attempts to handle all types supported by [MutableList][kotlin.collections.MutableList] in the
// * initialization of a new element
// *
// *  uses unsigned integers that are experimental: [UByte], [UInt], [ULong], [UShort]
// * @param v the list to resize
// * @param size the desired size to resize to
// * @sample reallocTest
// */
//@UseExperimental(ExperimentalUnsignedTypes::class)
//@Suppress("UNCHECKED_CAST")
//fun <E> realloc(v: kotlin.collections.MutableList<E>, size: Int): Unit = realloc(
//    v = v as kotlin.collections.MutableList<E?>,
//    a = v[0],
//    size = size,
//    isNullable = false
//)
//
///**
// * @see reallocTest
// */
//private class A {
//    inner class B {
//        inner class C {
//            var empty: Int = 0
//        }
//
//        var empty: Int = 0
//        var a: MutableList<C>
//
//        init {
//            a = mutableListOf(C())
//        }
//    }
//
//    var a: MutableList<B>
//
//    init {
//        a = mutableListOf(B())
//    }
//
//    /**
//     * test variable
//     */
//    var empty: Int = 0
//}
//
//fun reallocTest() {
//    val f = mutableListOf<A>()
//    val ff = mutableListOf<Int>()
//    val fff = mutableListOf<Double?>()
//    f.add(A()); f[0].empty = 5
//    ff.add(5)
//    fff.add(5.5)
//
//    realloc(f, 5)
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "f[0].empty = ${'$'}{'${'$'}'}{f[0].empty}")
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "f[4].empty = ${'$'}{'${'$'}'}{f[4].empty}")
//
//    f[0].a[0].empty = 88
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "f[0].a[0].empty = ${'$'}{'${'$'}'}{f[0].a[0].empty}")
//    f[4].a[0].empty = 88
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "f[4].a[0].empty = ${'$'}{'${'$'}'}{f[4].a[0].empty}")
//
//    realloc(f[0].a, 5)
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "f[0].a[4].empty = ${'$'}{'${'$'}'}{f[0].a[4].empty}")
//    realloc(f[4].a, 5)
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "f[4].a[4].empty = ${'$'}{'${'$'}'}{f[4].a[4].empty}")
//
//    realloc(ff, 5)
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "ff[0] = ${'$'}{'${'$'}'}{ff[0]}")
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "ff[4] = ${'$'}{'${'$'}'}{ff[4]}")
//    realloc(fff, 5)
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "fff[0] = ${'$'}{'${'$'}'}{fff[0]}")
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "fff[4] = ${'$'}{'${'$'}'}{fff[4]}")
//    abort()
//}

fun realloc(m: MutableList<Macro.MacroInternal>, newSize: Int) {
    m.add(Macro().MacroInternal())
//    m[0].size = newSize
}

fun realloc(m: MutableList<Macro>, newSize: Int) {
    m.add(Macro())
//    m[0].size = newSize
}\"\"\",
                            "delete.kt", "linuxMain/kotlin/preprocessor/utils/core/delete.kt", \"\"\"//package preprocessor.utils.core
//
//import java.io.File
//
///**
// * deletes **src**
// *
// * [abort]s on failure
// */
//fun delete(src: File) {
//    if (!src.exists()) {
//        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deletion of ${'$'}{'${'$'}'}{src.path} failed: file or directory does not exist")
//    }
//    if (!src.delete()) {
//        abort(preprocessor.base.globalVariables.depthAsString() + "deletion of \"${'$'}{'${'$'}'}{src.path}\" failed")
//    }
//}
\"\"\",
                            listOf(
                                "classTools",
                                "getDeclaringUpperLevelClassObject.kt", "linuxMain/kotlin/preprocessor/utils/core/classTools/getDeclaringUpperLevelClassObject.kt", \"\"\"//package preprocessor.utils.core.classTools
//
///**
// * obtains the parent class of [objectA]
// *
// * @params objectA the current class
// * @return **null** if [objectA] is **null**
// *
// * [objectA] if [objectA] is a top level class
// *
// * otherwise the parent class of [objectA]
// */
//fun getDeclaringUpperLevelClassObject(objectA: Any?): Any? {
//    if (objectA == null) {
//        return null
//    }
//    val cls = objectA.javaClass ?: return objectA
//    val outerCls = cls.enclosingClass
//        ?: // this is top-level class
//        return objectA
//    // get outer class object
//    var outerObj: Any? = null
//    try {
//        val fields = cls.declaredFields
//        for (field in fields) {
//            if (field != null && field.type === outerCls
//                && field.name != null && field.name.startsWith("this${'$'}{'${'$'}'}")
//            ) {
//                /*
//                `field.isAccessible = true` does not work with a Security Manager
//                java.security.AccessControlException: access denied
//                ("java.lang.reflect.ReflectPermission" "suppressAccessChecks")
//                */
//                field.isAccessible = true
//                outerObj = field.get(objectA)
//                break
//            }
//        }
//    } catch (e: Exception) {
//        e.printStackTrace()
//    }
//
//    return outerObj
//}
\"\"\",
                                "instanceChain.kt", "linuxMain/kotlin/preprocessor/utils/core/classTools/instanceChain.kt", \"\"\"//package preprocessor.utils.core.classTools
//
///**
// * creates and returns an instance of the class given as the parameter to
// * [chain][preprocessor.utils.core.classTools.chain]
// *
// * this class can be outer or inner
// * @param chain the current chain, this must originate from a list returned
// *
// * by [chain][preprocessor.utils.core.classTools.chain]
// * @param index used internally to traverse the [chain]
// * @param debug if true, debug output will be printed
// * @sample instanceChainSample
// * @see preprocessor.utils.core.classTools.chain
// */
//fun instanceChain(chain: MutableList<Any>, index: Int = chain.lastIndex, debug: Boolean = false): Any {
//    return if (index == 0) {
//        chain[index]
//    } else {
//        if (debug) if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "chain[${'$'}{'${'$'}'}index] = " + chain[index])
//        val outer = chain[index]
//        val toRun = Class.forName(chain[index].javaClass.name + "${'$'}{'${'$'}'}" + chain[index - 1].javaClass.simpleName)
//        val ctor = toRun.getDeclaredConstructor(chain[index]::class.java)
//        val lowerCInstance = ctor.newInstance(outer)
//        if (debug) if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "lowerCInstance = " + lowerCInstance!!::class.java)
//        if (index == 1) lowerCInstance
//        else instanceChain(
//            chain = chain,
//            index = index - 1,
//            debug = debug
//        )
//    }
//}
//
//private fun instanceChainSample() {
//    class A {
//        inner class B {
//            inner class C {
//                var empty: Int = 0
//            }
//
//            var empty: Int = 0
//            var a: MutableList<C>
//
//            init {
//                a = mutableListOf(C())
//            }
//        }
//
//        var a: MutableList<B>
//
//        init {
//            a = mutableListOf(B())
//        }
//
//        var empty: Int = 0
//    }
//
//    val f = mutableListOf<A>()
//    f.add(A()) // this is required, as i do not know how to do accomplish this in the init block
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "f[0] = ${'$'}{'${'$'}'}{instanceChain(chain(f[0]))}")
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "f[0].a[0] = ${'$'}{'${'$'}'}{instanceChain(chain(f[0].a[0]))}")
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "f[0].a[0].a[0] = ${'$'}{'${'$'}'}{instanceChain(chain(f[0].a[0].a[0]))}")
//}\"\"\",
                                "chain.kt", "linuxMain/kotlin/preprocessor/utils/core/classTools/chain.kt", \"\"\"//package preprocessor.utils.core.classTools
//
//import preprocessor.utils.core.abort
//
///**
// * returns a [MutableList] of classes parenting the current class
// *
// * the top most class is always the last index
// *
// * the last class is always the first index
// *
// * for example: `up(f[0].a[0].a[0])` returns 3 indexes (0, 1, and 2) consisting of the following:
// *
// * index 0 = `f[0].a[0].a[0]`
// *
// * index 1 = `f[0].a[0]`
// *
// * index 2 = `f[0]`
// *
// * @param a the current class
// * @param m used internally to build up a list of classes
// * @param debug if true, debug output will be printed
// * @see getDeclaringUpperLevelClassObject
// */
//fun chain(a: Any, m: MutableList<Any> = mutableListOf(), debug: Boolean = false): MutableList<Any> {
//    m.add(a)
//    if (debug) if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "get upper class of ${'$'}{'${'$'}'}{a.javaClass.name}")
//    val upperC = getDeclaringUpperLevelClassObject(a) ?: abort(preprocessor.base.globalVariables.depthAsString() + "upperC is null o.o")
//    return if (a == upperC) m
//    else chain(upperC, m)
//}
\"\"\"
                            )
                        ),
                        listOf(
                            "extra",
                            "Balanced.kt", "linuxMain/kotlin/preprocessor/utils/extra/Balanced.kt", \"\"\"package preprocessor.utils.extra

/**
 *
 * a class for detecting balanced brackets
 *
 * cant be bothered documenting this
 *
 * modified from the original rosetta code in the **See Also**
 *
 * @see <a href="https://rosettacode.org/wiki/Balanced_brackets#Kotlin">Balanced Brackets</a>
 */
@Suppress("MemberVisibilityCanBePrivate")
class Balanced {
    /**
     *
     */
    class BalanceList {
        /**
         *
         */
        var l: MutableList<Char> = mutableListOf()
        /**
         *
         */
        var r: MutableList<Char> = mutableListOf()

        /**
         *
         */
        fun addPair(l: Char, r: Char) {
            this.l.add(l)
            this.r.add(r)
        }
    }

    /**
     *
     */
    var start: MutableList<Int> = mutableListOf()
    /**
     *
     */
    var end: MutableList<Int> = mutableListOf()
    /**
     *
     */
    var index: Int = 0
    /**
     *
     */
    var countLeft: Int = 0  // number of left brackets so far unmatched
    /**
     *
     */
    var splitterCount: Int = 0
    /**
     *
     */
    var splitterLocation: MutableList<Int> = mutableListOf()
    /**
     *
     */
    var lastRegisteredLeftHandSideBalancer: Char = ' '
    /**
     *
     */
    var lastRegisteredRightHandSideBalancer: Char = ' '
    /**
     *
     */
    var lastCheckString: String = ""

    /**
     *
     */
    fun isBalanced(s: String, balancerLeft: Char, balancerRight: Char): Boolean {
        lastCheckString = s
        lastRegisteredLeftHandSideBalancer = balancerLeft
        lastRegisteredRightHandSideBalancer = balancerRight
        start
        end
        if (s.isEmpty()) return true
        for (c in s) {
            if (c == lastRegisteredLeftHandSideBalancer) {
                countLeft++
                if (countLeft == 1) start.add(index)
            } else if (c == lastRegisteredRightHandSideBalancer) {
                if (countLeft == 1) end.add(index + 1)
                if (countLeft > 0) countLeft--
                else return false
            }
            index++
        }
        return countLeft == 0
    }

    /**
     *
     */
    fun isBalancerR(c: Char, balance: BalanceList): Boolean {
        balance.r.forEach {
            if (c == it) return true
        }
        return false
    }

    /**
     *
     */
    fun isBalancerL(c: Char, balance: BalanceList): Boolean {
        balance.l.forEach {
            if (c == it) return true
        }
        return false
    }

    /**
     *
     */
    fun containsL(c: String, balance: BalanceList): Boolean {
        balance.l.forEach {
            if (c.contains(it)) return true
        }
        return false
    }

    /**
     *
     */
    fun containsR(c: String, balance: BalanceList): Boolean {
        balance.r.forEach {
            if (c.contains(it)) return true
        }
        return false
    }

    /**
     *
     */
    fun isBalancedSplit(s: String, balancer: BalanceList, Splitter: Char): Boolean {
        lastCheckString = s
        lastRegisteredLeftHandSideBalancer = balancer.l[balancer.l.lastIndex]
        lastRegisteredRightHandSideBalancer = balancer.r[balancer.r.lastIndex]
        if (s.isEmpty()) return true
        for (c in s) {
            if (countLeft == 0) if (c == Splitter) {
                splitterCount++
                splitterLocation.add(index)
            }
            if (isBalancerL(c, balancer)) {
                countLeft++
                if (countLeft == 1) start.add(index)
            } else if (isBalancerR(c, balancer)) {
                if (countLeft == 1) end.add(index + 1)
                if (countLeft > 0) countLeft--
                else return false
            }
            index++
        }
        return countLeft == 0
    }

    /**
     *
     */
    fun extractText(text: String): String {
        if (isBalanced(text, '(', ')')) {
            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "text : " + text.substring(start[0], end[0]))
            return text.substring(start[0], end[0])
        }
        return text
    }

    /**
     *
     */
    fun info() {
        if (preprocessor.base.globalVariables.flags.debug) {
            println(preprocessor.base.globalVariables.depthAsString() + "last check string  = ${'$'}{'${'$'}'}lastCheckString")
            println(preprocessor.base.globalVariables.depthAsString() + "left balancer      = ${'$'}{'${'$'}'}lastRegisteredLeftHandSideBalancer")
            println(preprocessor.base.globalVariables.depthAsString() + "right balancer     = ${'$'}{'${'$'}'}lastRegisteredRightHandSideBalancer")
            println(preprocessor.base.globalVariables.depthAsString() + "start index        = ${'$'}{'${'$'}'}start")
            println(preprocessor.base.globalVariables.depthAsString() + "end index          = ${'$'}{'${'$'}'}end")
            println(preprocessor.base.globalVariables.depthAsString() + "current index       = ${'$'}{'${'$'}'}index")
            println(preprocessor.base.globalVariables.depthAsString() + "unmatched brackets = ${'$'}{'${'$'}'}countLeft")
            println(preprocessor.base.globalVariables.depthAsString() + "splitter count     = ${'$'}{'${'$'}'}splitterCount")
            println(preprocessor.base.globalVariables.depthAsString() + "splitter location  = ${'$'}{'${'$'}'}splitterLocation")
        }
    }
}
\"\"\",
                            "expand.kt", "linuxMain/kotlin/preprocessor/utils/extra/expand.kt", \"\"\"package preprocessor.utils.extra

import preprocessor.base.globalVariables
import preprocessor.core.*
import preprocessor.utils.core.abort
import preprocessor.utils.`class`.extensions.collapse
import preprocessor.utils.`class`.extensions.toByteArray
import preprocessor.utils.core.algorithms.Stack
import kotlin.collections.MutableList

fun expecting(what: String, closeValue: String): String {
    return "expecting close value of ${'$'}{'${'$'}'}closeValue for ${'$'}{'${'$'}'}what >"
}

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
    c: Boolean = false,
    newlineFunction :(
        (String) -> String
    )?
): String? {
    val dm = 15
    if (depth > dm) abort("depth exceeded ${'$'}{'${'$'}'}dm")
    preprocessor.base.globalVariables.depth = depth
    var stringize = s
    var concat = c
    if (preprocessor.base.globalVariables.flags.debug) {
        println(preprocessor.base.globalVariables.depthAsString() + "PARAMETERS AT FUNCTION CALL START")
        println(
            preprocessor.base.globalVariables.depthAsString() + "expanding '${'$'}{'${'$'}'}{
            lex.currentLine?.replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())}'"
        )
        println(
            preprocessor.base.globalVariables.depthAsString() + "tokenSequence = ${'$'}{'${'$'}'}{tokenSequence.toStringAsArray()
                .replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())}"
        )
        println(
            preprocessor.base.globalVariables.depthAsString() + "ARG = ${'$'}{'${'$'}'}{ARG.toString()
                .replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())}"
        )
        println(preprocessor.base.globalVariables.depthAsString() + "blacklist = ${'$'}{'${'$'}'}blacklist")
        println(preprocessor.base.globalVariables.depthAsString() + "expanding    : ${'$'}{'${'$'}'}expanding")
        println(preprocessor.base.globalVariables.depthAsString() + "expandingType: ${'$'}{'${'$'}'}expandingType")
        println(preprocessor.base.globalVariables.depthAsString() + "originalExpanding: ${'$'}{'${'$'}'}originalExpanding")
        println(preprocessor.base.globalVariables.depthAsString() + "originalExpandingType: ${'$'}{'${'$'}'}originalExpandingType")
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
            if (preprocessor.base.globalVariables.flags.debug) println(
                preprocessor.base.globalVariables.depthAsString() + "clearing comment token '${'$'}{'${'$'}'}{tokenSequence
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
                    if (lex.currentLine == null) {
                        if (newlineFunction == null) abort(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "no more lines when expecting more lines, unterminated block comment"
                        )
                        else lex.currentLine = newlineFunction(expecting("block comment", "*/"))
                    }
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
            if (preprocessor.base.globalVariables.flags.debug) println(
                preprocessor.base.globalVariables.depthAsString() +
                        "popping double string token '${'$'}{'${'$'}'}doubleString'"
            )
            expansion.append(doubleString.toString())
            doubleString.pop() // pop the first "
            var iterations = 0
            val maxIterations = 1000
            while (iterations <= maxIterations) {
                if (tokenSequence.peek() == null) {
                    lex.lex()
                    // Line is longer than allowed by code style (> 120 columns)
                    if (lex.currentLine == null) {
                        if (newlineFunction == null) abort(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "no more lines when expecting more lines, unterminated double string"
                        )
                        else lex.currentLine = newlineFunction(expecting("double string", "\""))
                    }
                    tokenSequence.tokenList = parserPrep(lex.currentLine as String)
                }
                if (newline.peek()) {
                    if (preprocessor.base.globalVariables.flags.debug) println(
                        preprocessor.base.globalVariables.depthAsString() + "popping newline token '${'$'}{'${'$'}'}{
                        newline
                            .toString()
                            .replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())
                        }'"
                    )
                    newline.pop()
                } else if (doubleString.peek()) {
                    if (preprocessor.base.globalVariables.flags.debug) println(
                        preprocessor.base.globalVariables.depthAsString() +
                                "popping double string token '${'$'}{'${'$'}'}doubleString'"
                    )
                    expansion.append(doubleString.toString())
                    doubleString.pop()
                    break
                } else if (backslash.peek()) {
                    if (preprocessor.base.globalVariables.flags.debug) println(
                        preprocessor.base.globalVariables.depthAsString() +
                                "popping backslash token '${'$'}{'${'$'}'}backslash'"
                    )
                    expansion.append(backslash.toString())
                    backslash.pop()
                    if (doubleString.peek()) {
                        if (preprocessor.base.globalVariables.flags.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "popping double string token '${'$'}{'${'$'}'}doubleString'"
                        )
                        expansion.append(doubleString.toString())
                        doubleString.pop()
                    } else {
                        if (preprocessor.base.globalVariables.flags.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "unknown backslash sequence '${'$'}{'${'$'}'}{tokenSequence.peek()}'"
                        )
                        expansion.append(tokenSequence.pop()!!)
                    }
                } else {
                    val popped = tokenSequence.pop()
                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "popping normal token '${'$'}{'${'$'}'}popped'")
                    expansion.append(popped)
                }
                iterations++
            }
            if (iterations > maxIterations) abort(
                preprocessor.base.globalVariables.depthAsString() + "iterations expired"
            )
        } else if (singleString.peek()) {
            if (preprocessor.base.globalVariables.flags.debug) println(
                preprocessor.base.globalVariables.depthAsString() +
                        "popping single string token '${'$'}{'${'$'}'}singleString'"
            )
            expansion.append(singleString.toString())
            singleString.pop() // pop the first '
            var iterations = 0
            val maxIterations = 1000
            while (iterations <= maxIterations) {
                if (tokenSequence.peek() == null) {
                    lex.lex()
                    // Line is longer than allowed by code style (> 120 columns)
                    if (lex.currentLine == null) {
                        if (newlineFunction == null) abort(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "no more lines when expecting more lines, unterminated single string"
                        )
                        else lex.currentLine = newlineFunction(expecting("single string", "'"))
                    }
                    tokenSequence.tokenList = parserPrep(lex.currentLine as String)
                }
                if (newline.peek()) {
                    if (preprocessor.base.globalVariables.flags.debug) println(
                        preprocessor.base.globalVariables.depthAsString() + "popping newline token '${'$'}{'${'$'}'}{
                        newline
                            .toString()
                            .replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())
                        }'"
                    )
                    newline.pop()
                } else if (singleString.peek()) {
                    if (preprocessor.base.globalVariables.flags.debug) println(
                        preprocessor.base.globalVariables.depthAsString() +
                                "popping single string token '${'$'}{'${'$'}'}singleString'"
                    )
                    expansion.append(singleString.toString())
                    singleString.pop()
                    break
                } else if (backslash.peek()) {
                    if (preprocessor.base.globalVariables.flags.debug) println(
                        preprocessor.base.globalVariables.depthAsString() +
                                "popping backslash token '${'$'}{'${'$'}'}backslash'"
                    )
                    expansion.append(backslash.toString())
                    backslash.pop()
                    if (singleString.peek()) {
                        if (preprocessor.base.globalVariables.flags.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "popping single string token '${'$'}{'${'$'}'}singleString'"
                        )
                        expansion.append(singleString.toString())
                        singleString.pop()
                    } else {
                        if (preprocessor.base.globalVariables.flags.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "unknown backslash sequence '${'$'}{'${'$'}'}{tokenSequence.peek()}'"
                        )
                        expansion.append(tokenSequence.pop()!!)
                    }
                } else {
                    val popped = tokenSequence.pop()
                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "popping normal token '${'$'}{'${'$'}'}popped'")
                    expansion.append(popped)
                }
                iterations++
            }
            if (iterations > maxIterations) abort(
                preprocessor.base.globalVariables.depthAsString() + "iterations expired"
            )
        } else if (emptyParenthesis.peek()) {
            if (preprocessor.base.globalVariables.flags.debug) println(
                preprocessor.base.globalVariables.depthAsString() +
                        "popping empty parenthesis token '${'$'}{'${'$'}'}emptyParenthesis'"
            )
            expansion.append(emptyParenthesis.toString())
            emptyParenthesis.pop()
        } else if (newline.peek()) {
            if (preprocessor.base.globalVariables.flags.debug) println(
                preprocessor.base.globalVariables.depthAsString() + "popping newline token '${'$'}{'${'$'}'}{
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
                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "popping space token '${'$'}{'${'$'}'}space'")
                space.pop()
                expansion.append(" ")
            }
            if (directive.peek()) {
                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "popping directive token '${'$'}{'${'$'}'}directive'")
                directive.pop()

                if (stringize) {
                    stringize = false
                    concat = true
                } else stringize = true

                if (space.peek()) {
                    stringize = false
                    concat = false
                    // case 1, space at start of file followed by define
                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "popping space token '${'$'}{'${'$'}'}space'")
                    space.pop()
                }
                if (abortDef.peek()) abort("#abort found")
                if (ignoreDef.peek()) return null
                if (defineDef.peek()) {
                    stringize = false
                    concat = false
                    // case 2, define at start of line
                    if (preprocessor.base.globalVariables.flags.debug) println(
                        preprocessor.base.globalVariables.depthAsString() +
                                "popping ${'$'}{'${'$'}'}{Macro().Directives().Define().value} statement '${'$'}{'${'$'}'}{tokenSequence
                                    .toString().replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())
                                }'"
                    )
                    processDefine("${'$'}{'${'$'}'}{Macro().Directives().value}${'$'}{'${'$'}'}tokenSequence", macro)
                    tokenSequence.clear()
                }
            }
        } else if (space.peek()) {
            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "popping space token '${'$'}{'${'$'}'}space'")
            space.pop()
            expansion.append(" ")
        } else {
            val index = macro.size - 1
            val ss = tokenSequence.peek()
            val name: String
            if (ss == null) abort(preprocessor.base.globalVariables.depthAsString() + "something is wrong")
            name = ss
            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "popping normal token '${'$'}{'${'$'}'}name'")
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
                if (globalVariables.status.currentMacroExists) {
                    macroFunctionExists = true
                }
                macroObjectIndex = macroExists(
                    name,
                    Macro().Directives().Define().Types().Object,
                    index,
                    macro
                )
                if (globalVariables.status.currentMacroExists) {
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

                if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "looking ahead")
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
                        if (globalVariables.status.currentMacroExists) skip = true
                    }
                    if (macroFunctionExists && !skip) {
                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "'${'$'}{'${'$'}'}name' is a function")
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
                            index = index,
                            newlineFunction = newlineFunction
                        )
                        if (preprocessor.base.globalVariables.flags.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "eFS1R.argv = ${'$'}{'${'$'}'}{eFS1R.argv}"
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
                            replacementList = macro[index].macros[eFS1R.macroTypeDependantIndex].replacementList,
                            newlineFunction = newlineFunction
                        )
                        // 3) rescan, if the original macro appears again in the output, it isn't expanded any further
                        if (preprocessor.base.globalVariables.flags.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "rescanning, ARG = ${'$'}{'${'$'}'}ARG"
                        )
                        if (preprocessor.base.globalVariables.flags.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "eFS1R.argv = ${'$'}{'${'$'}'}{eFS1R.argv}"
                        )
                        val eX = if (originalExpanding != null) originalExpanding
                        else if (eFS1R.argv.isNotEmpty()) name
                        else null
                        val eXT = if (originalExpandingType != null) originalExpandingType
                        else if (eFS1R.argv.isNotEmpty()) Macro().Directives().Define().Types().Function
                        else null
                        if (preprocessor.base.globalVariables.flags.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "eX = ${'$'}{'${'$'}'}eX"
                        )
                        if (preprocessor.base.globalVariables.flags.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "eXT = ${'$'}{'${'$'}'}eXT"
                        )
                        if (preprocessor.base.globalVariables.flags.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "blacklisting ${'$'}{'${'$'}'}name"
                        )
                        if (macroUnexpanded != null) {
                            if (preprocessor.base.globalVariables.flags.debug) println(
                                preprocessor.base.globalVariables.depthAsString() +
                                        "macroUnexpanded " +
                                        "${'$'}{'${'$'}'}{macroUnexpanded[index].macros[eFS1R.macroTypeDependantIndex].identifier} of type " +
                                        "${'$'}{'${'$'}'}{macroUnexpanded[index].macros[eFS1R.macroTypeDependantIndex].type} has value " +
                                        "${'$'}{'${'$'}'}{macroUnexpanded[index].macros[eFS1R.macroTypeDependantIndex].replacementList
                                            ?.replace(
                                                "\n",
                                                "\n" + preprocessor.base.globalVariables.depthAsString()
                                            )}"
                            )
                            abort("1")
                        }
                        if (preprocessor.base.globalVariables.flags.debug) {
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
                            newlineFunction = newlineFunction,
                            string = e1!!
                        )

                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() +
                                "expansion = '${'$'}{'${'$'}'}{expansion.toString()}'")
                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() +
                                "e2 = '${'$'}{'${'$'}'}e2'")

                        // 4) put the result of substitution back into the source code
                        if (e2 != null) expansion.append(e2)
                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() +
                                "expansion = '${'$'}{'${'$'}'}{expansion.toString()}'")
                    } else {
                        if (preprocessor.base.globalVariables.flags.debug) println(
                            preprocessor.base.globalVariables.depthAsString() +
                                    "'${'$'}{'${'$'}'}name' is a function but no associated function macro exists"
                        )
                        expansion.append(name)
                    }
                } else {
                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "'${'$'}{'${'$'}'}name' is an object")
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
                            if (preprocessor.base.globalVariables.flags.debug) println(
                                preprocessor.base.globalVariables.depthAsString() +
                                        "but it is currently being expanded"
                            )
                            expansion.append(name)
                        } else {
                            if (preprocessor.base.globalVariables.flags.debug) println(
                                preprocessor.base.globalVariables.depthAsString() +
                                        "and is not currently being expanded"
                            )

                            val macroTypeDependantIndex = macroObjectIndex
                            // Line is longer than allowed by code style (> 120 columns)
                            if (preprocessor.base.globalVariables.flags.debug) println(
                                preprocessor.base.globalVariables.depthAsString() +
                                        "${'$'}{'${'$'}'}{macro[index].macros[macroTypeDependantIndex].identifier} of type " +
                                        "${'$'}{'${'$'}'}{macro[index].macros[macroTypeDependantIndex].type} has value " +
                                        "${'$'}{'${'$'}'}{macro[index].macros[macroTypeDependantIndex].replacementList
                                            ?.replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())}"
                            )
                            if (stringize) {
                                if (macroUnexpanded != null) {
                                    if (preprocessor.base.globalVariables.flags.debug) println(
                                        preprocessor.base.globalVariables.depthAsString() +
                                                "macroUnexpanded " +
                                                "${'$'}{'${'$'}'}{macroUnexpanded[index].macros[macroTypeDependantIndex].identifier} of type " +
                                                "${'$'}{'${'$'}'}{macroUnexpanded[index].macros[macroTypeDependantIndex].type} has value " +
                                                "${'$'}{'${'$'}'}{macroUnexpanded[index].macros[macroTypeDependantIndex].replacementList
                                                    ?.replace(
                                                        "\n",
                                                        "\n" + preprocessor.base.globalVariables.depthAsString()
                                                    )}"
                                    )
                                    expansion.append(
                                        "\"${'$'}{'${'$'}'}{macroUnexpanded[index].macros[macroTypeDependantIndex].replacementList
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
                                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "ARG = ${'$'}{'${'$'}'}ARG")
                                        if (!ARG.contains(name)) {
                                            if (preprocessor.base.globalVariables.flags.debug) println(
                                                preprocessor.base.globalVariables.depthAsString() +
                                                        "blacklisting ${'$'}{'${'$'}'}name"
                                            )
                                            blacklist.add(name)
                                        } else {
                                            if (preprocessor.base.globalVariables.flags.debug) println(
                                                preprocessor.base.globalVariables.depthAsString() +
                                                        "${'$'}{'${'$'}'}name is an argument"
                                            )
                                        }
                                    } else {
                                        if (preprocessor.base.globalVariables.flags.debug) println(
                                            preprocessor.base.globalVariables.depthAsString() +
                                                    "warning: ARG is null"
                                        )
                                        if (preprocessor.base.globalVariables.flags.debug) println(
                                            preprocessor.base.globalVariables.depthAsString() +
                                                    "blacklisting ${'$'}{'${'$'}'}name"
                                        )
                                        blacklist.add(name)
                                    }
                                    val parser =
                                        Parser(lex.currentLine as String) { parserPrep(it) }
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
                                        c = concat,
                                        newlineFunction = newlineFunction
                                    )!!
                                    preprocessor.base.globalVariables.depth = depth
                                    if (preprocessor.base.globalVariables.flags.debug) println(
                                        preprocessor.base.globalVariables.depthAsString() +
                                                "macro Object expansion ${'$'}{'${'$'}'}name returned ${'$'}{'${'$'}'}e"
                                    )
                                    if (stringize) stringize = false
                                    if (concat) concat = false
                                    expansion.append(e)
                                }
                            }
                        }
                    } else {
                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "but does not exist as a macro")
                        expansion.append(name)
                    }
                }
            } else expansion.append(tokenSequence.pop()!!)
        }
        iterations++
    }
    if (iterations > maxIterations) if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "iterations expired")
    if (preprocessor.base.globalVariables.flags.debug) {
        println(preprocessor.base.globalVariables.depthAsString() + "expansion = ${'$'}{'${'$'}'}expansion")
        println(preprocessor.base.globalVariables.depthAsString() + "PARAMETERS AT FUNCTION CALL END")
        println(preprocessor.base.globalVariables.depthAsString() + "expanding '${'$'}{'${'$'}'}{lex.currentLine?.replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())}'")
        println(preprocessor.base.globalVariables.depthAsString() + "tokenSequence = ${'$'}{'${'$'}'}{tokenSequence.toStringAsArray()}")
        println(preprocessor.base.globalVariables.depthAsString() + "ARG = ${'$'}{'${'$'}'}ARG")
        println(preprocessor.base.globalVariables.depthAsString() + "blacklist = ${'$'}{'${'$'}'}blacklist")
        println(preprocessor.base.globalVariables.depthAsString() + "expanding    : ${'$'}{'${'$'}'}expanding")
        println(preprocessor.base.globalVariables.depthAsString() + "expandingType: ${'$'}{'${'$'}'}expandingType")
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
    index: Int,
    newlineFunction :(
        (String) -> String
    )?
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
        if (if (newline.peek()) {
                newline.pop()
                true
            } else tokenSequence.peek() == null
        ) {
            if (tokenSequence.peek() == null) {
                if (preprocessor.base.globalVariables.flags.debug) println(
                    preprocessor.base.globalVariables.depthAsString() +
                            "ran out of tokens, grabbing more tokens from the next line"
                )
                lex.lex()
                if (lex.currentLine == null) {
                    if (newlineFunction == null) abort(
                        preprocessor.base.globalVariables.depthAsString() +
                                "no more lines when expecting more lines, unterminated parenthesis"
                    )
                    else lex.currentLine = newlineFunction(expecting("parenthesis", ")"))
                }
                tokenSequence.tokenList = parserPrep(lex.currentLine as String)
            }
        }
        if (preprocessor.base.globalVariables.flags.debug) println(
            preprocessor.base.globalVariables.depthAsString() +
                    "popping '${'$'}{'${'$'}'}{tokenSequence.peek()}'"
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
    if (iterations > maxIterations) if (preprocessor.base.globalVariables.flags.debug) println(
        preprocessor.base.globalVariables.depthAsString() + "iterations expired"
    )
    argc++
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "argc = ${'$'}{'${'$'}'}argc")
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "argv = ${'$'}{'${'$'}'}argv")
    val macroTypeDependantIndex = macroFunctionIndex
    // Line is longer than allowed by code style (> 120 columns)
    if (preprocessor.base.globalVariables.flags.debug) println(
        preprocessor.base.globalVariables.depthAsString() +
                "${'$'}{'${'$'}'}{macro[index].macros[macroTypeDependantIndex].identifier} of type " +
                "${'$'}{'${'$'}'}{macro[index].macros[macroTypeDependantIndex].type} has value " +
                "${'$'}{'${'$'}'}{macro[index].macros[macroTypeDependantIndex].replacementList
                    ?.replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())}"
    )
    if (preprocessor.base.globalVariables.flags.debug) println(
        preprocessor.base.globalVariables.depthAsString() +
                "macro  args = ${'$'}{'${'$'}'}{macro[index].macros[macroTypeDependantIndex].arguments}"
    )
    val macroUnexpanded: MutableList<String> = mutableListOf()
    var i = 0
    if (preprocessor.base.globalVariables.flags.debug) println(
        preprocessor.base.globalVariables.depthAsString() +
                "expanding arguments: ${'$'}{'${'$'}'}argc arguments to expand"
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
                Parser(lex.currentLine as String) { parserPrep(it) }
            val e = expand(
                depth = depth + 1,
                lex = lex,
                tokenSequence = parser,
                macro = macro,
                expanding = argv[i].substringBefore('('),
                expandingType = if (argv[i].contains('('))
                    Macro().Directives().Define().Types().Function
                else
                    Macro().Directives().Define().Types().Object,
                newlineFunction = newlineFunction
            )
            preprocessor.base.globalVariables.depth = depth
            if (preprocessor.base.globalVariables.flags.debug) println(
                preprocessor.base.globalVariables.depthAsString() +
                        "macro expansion '${'$'}{'${'$'}'}{argv[i]}' returned ${'$'}{'${'$'}'}e"
            )
            argv[i] = e!!.trimStart().trimEnd()
        }
        i++
    }
    if (preprocessor.base.globalVariables.flags.debug) println(
        preprocessor.base.globalVariables.depthAsString() +
                "expanded arguments: ${'$'}{'${'$'}'}argc arguments expanded"
    )
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "target args = ${'$'}{'${'$'}'}argv")
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "unexpanded target args = ${'$'}{'${'$'}'}macroUnexpanded")
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
    replacementList: String?,
    newlineFunction :(
        (String) -> String
    )?
): String? = if (macro[index].macros[macroTypeDependantIndex].replacementList == null) null
else {
    val lex = Lexer(
        replacementList!!.toByteArray(),
        globalVariables.tokensNewLine
    )
    lex.lex()
    if (lex.currentLine != null) {
        val parser = Parser(lex.currentLine as String) { parserPrep(it) }
        val associatedArguments: MutableList<Macro>? = toMacro(arguments, argv)
        val associatedArgumentsUnexpanded: MutableList<Macro>? = toMacro(
            definition = arguments,
            replacementList = macroUnexpanded
        )
        if (associatedArguments == null || associatedArgumentsUnexpanded == null) abort("arguments are null")
        // Line is longer than allowed by code style (> 120 columns)
        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "arguments" + arguments)
        val e = expand(
            depth = depth + 1,
            lex = lex,
            tokenSequence = parser,
            macro = associatedArguments,
            macroUnexpanded = associatedArgumentsUnexpanded,
            ARG = arguments,
            expanding = name,
            expandingType = Macro().Directives().Define().Types().Function,
            newlineFunction = newlineFunction
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
    newlineFunction :(
        (String) -> String
    )?,
    string: String
): String? {
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() +
            "string = '${'$'}{'${'$'}'}string'")
    val lex2 = Lexer(string.toByteArray(), globalVariables.tokensNewLine)
    lex2.lex()
    if (lex2.currentLine != null) {
        val parser = Parser(lex2.currentLine as String) { parserPrep(it) }
        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() +
                "lex.currentLine = '${'$'}{'${'$'}'}{lex.currentLine}'")
        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() +
                "lex2.currentLine = '${'$'}{'${'$'}'}{lex2.currentLine}'")
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
            c = c,
            newlineFunction = newlineFunction
        )
        preprocessor.base.globalVariables.depth = depth
        return e
    } else return null
}\"\"\",
                            "extractArguments.kt", "linuxMain/kotlin/preprocessor/utils/extra/extractArguments.kt", \"\"\"package preprocessor.utils.extra

import preprocessor.utils.core.abort

/**
 * extracts the arguments of a function and puts them into an array
 *
 * @returns an array of parameters
 */
fun extractArguments(arg: String): MutableList<String>? {
    fun filterSplit(arg: String, ex: Balanced, b: Balanced.BalanceList): MutableList<String> {
        val arguments: MutableList<String> = mutableListOf()
        if (ex.containsL(arg, b)) {
            if (ex.isBalancedSplit(arg, b, ',')) {
                ex.info()
                if (ex.splitterCount == 0) {
                    arguments.add(arg)
                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + arguments[0])
                } else {
                    var s: String = arg.substring(0, ex.splitterLocation[0]).trimStart()
                    arguments.add(s)
                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + arguments[0])
                    var i = 0
                    while (i < ex.splitterLocation.lastIndex) {
                        s = arg.substring(ex.splitterLocation[i] + 1, ex.splitterLocation[i + 1]).trimStart()
                        arguments.add(s)
                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + arguments[i])
                        i++
                    }
                    s = arg.substring(ex.splitterLocation[i] + 1, ex.index).trimStart()
                    arguments.add(s)
                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + arguments[i])
                }
            } else {
                ex.info()
                abort(preprocessor.base.globalVariables.depthAsString() + "unBalanced code")
            }
        } else if (ex.containsR(arg, b)) {
            // unBalanced
            abort(preprocessor.base.globalVariables.depthAsString() + "unBalanced code")
        } else {
            val a: MutableList<String> = arg.split(',').toMutableList()
            // next, remove whitespaces from the start and end of each index string
            var i = 0
            while (i <= a.lastIndex) {
                val s: String = a[i].trimStart().trimEnd()
                arguments.add(s)
                i++
            }
        }
        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "arguments List = ${'$'}{'${'$'}'}arguments")
        return arguments
    }
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "extracting arguments for ${'$'}{'${'$'}'}arg")
    // first, determine the positions of all tokens
    val balance = Balanced.BalanceList()
    balance.addPair('(', ')')
    balance.addPair('{', '}')
    balance.addPair('[', ']')
    val ex = Balanced()
    return filterSplit(arg, ex, balance)
}
\"\"\",
                            "parse.kt", "linuxMain/kotlin/preprocessor/utils/extra/parse.kt", \"\"\"package preprocessor.utils.extra

import preprocessor.base.globalVariables
import preprocessor.core.Macro
import preprocessor.core.parserPrep
import preprocessor.core.Lexer
import preprocessor.core.Parser
import preprocessor.utils.`class`.extensions.toByteArray
import preprocessor.utils.core.algorithms.Stack

/**
 * parses a line
 * @param lex the current [Lexer]
 * @param macro the [Macro] list
 */
fun parse(lex: Lexer, macro: MutableList<Macro>): String? = parse(lex, macro, null)

/**
 * parses a line
 * @param lex the current [Lexer]
 * @param macro the [Macro] list
 * @param newlineFunction the function to try if [lex].lex() fails
 */
fun parse(lex: Lexer, macro: MutableList<Macro>, newlineFunction :((String) -> String)?): String? {
    preprocessor.base.globalVariables.depth = 0
    return expand(
        lex = lex,
        tokenSequence = Parser(tokens = lex.currentLine as String, stackMethod = { parserPrep(it) }),
        macro = macro,
        newlineFunction = newlineFunction
    )
}

/**
 * parses a line
 * @param string the string to parse
 * @param macro the [Macro] list
 */
fun parse(
    string: String,
    macro: MutableList<Macro>
): String? = parse(string, macro, null)

/**
 * parses a line
 * @param string the string to parse
 * @param macro the [Macro] list
 * @param newlineFunction the function to try if [lex].lex() fails
 */
fun parse(
    string: String,
    macro: MutableList<Macro>,
    newlineFunction :((String) -> String)?
): String? {
    if (string.isEmpty()) return string
    val lex = Lexer(
        string.toByteArray(),
        globalVariables.tokensNewLine
    )
    lex.lex()
    if (lex.currentLine == null) return null
    val str = StringBuilder()
    while (lex.currentLine != null) {
        val p = parse(lex, macro, newlineFunction)
        if (p == null) return null
        str.append(p)
        lex.lex()
    }
    return str.toString()
}
\"\"\"
                        )
                    )
                ),
                listOf(
                    "sample",
                    "Commands.kt", "linuxMain/kotlin/sample/Commands.kt", \"\"\"package sample

import preprocessor.utils.`class`.extensions.*
import preprocessor.utils.core.algorithms.LinkedList

@Suppress("unused")
class Commands(repl: REPL) {
    inner class cmd {
        var command: String? = null
        var description: String? = null
        var alias: String? = null
        var function: (() -> Unit)? = null
    }

    var command = LinkedList<cmd>()
    val repl = repl
    val defaultCommandHeader = "command"
    val defaultDescriptionHeader = "description"

    fun add(command: String): Commands = this.also {
        it.command.append(
            cmd()
                .also {
                    it.command = command
                }
                .also {
                    it.description = "No description provided"
                }
        )
    }
    fun add(command: String, description: String): Commands = this.also {
        it.command.append(
            cmd()
                .also {
                    it.command = command
                }.also {
                    it.description = description
                }
        )
    }
    fun add(command: String, description: String, function: () -> Unit): Commands = this.also {
        it.command.append(
            cmd()
                .also {
                    it.command = command
                }.also {
                    it.function = function
                }.also {
                    it.description = description
                }
        )
    }
    fun add(command: String, function: () -> Unit): Commands = this.also {
        it.command.append(
            cmd()
                .also {
                    it.command = command
                }.also {
                    it.function = function
                }
                .also {
                    it.description = "No description provided"
                }
        )
    }
    fun alias(command: String, value: String): Commands = this.also {
        it.command.append(
            cmd()
                .also {
                    it.command = value
                }
                .also {
                    it.alias = command
                }
                .also {
                    it.description = "alias for ${'$'}{'${'$'}'}value"
                }
        )
    }
    fun get(command: String) : (() -> Unit)? {
        if (repl.debug) println("repl.debug = ${'$'}{'${'$'}'}{repl.debug}")
        this.command.forEach {
            when {
                it != null -> when {
                    it.command != null || it.alias != null -> when {
                        ifUnconditionalReturn(
                            ifTrueReturn(
                                it.alias.equals(command)
                            ) {
                                if (repl.debug) println("alias equals command")
                            }.ifFalseReturn {
                                if (repl.debug) println("alias does not equal command")
                            } && ifFalseReturn(
                                !it.command.equals(command)
                            ) {
                                if (repl.debug) println("command equals command, this is a looping alias")
                            }.ifTrueReturn {
                                if (repl.debug) println("command does not equal command, this is not a looping alias")
                            } && ifTrueReturn(
                                it.function == null
                            ) {
                                if (repl.debug) println("function is null")
                            }.ifFalseReturn {
                                if (repl.debug) println("function is not null")
                            }
                        )
                            {
                                if (repl.debug) println("alias found")
                            }.ifTrueReturn {
                            if (repl.debug) println("and it matches ${'$'}{'${'$'}'}command")
                            }.ifFalseReturn {
                            if (repl.debug) println("and it does not match ${'$'}{'${'$'}'}command")
                            } -> {
                                val t = get(it.command!!)
                                return t
                            }
                        ifUnconditionalReturn(it.command.equals(command) && it.alias == null) {
                            if (repl.debug) println("command found")
                        }.ifTrueReturn {
                            if (repl.debug) println("and it matches ${'$'}{'${'$'}'}command")
                        }.ifFalseReturn {
                            if (repl.debug) println("and it does not match ${'$'}{'${'$'}'}command")
                        } -> return it.function
                    }
                }
            }
        }
        return null
    }
    fun longestCommand(): Int {
        var currentLength = 0
        this.command.forEach {
            if (it != null) {
                if (it.command != null) {
                    var thisLength = it.command!!.length
                    if (thisLength isGreaterThan currentLength ) currentLength = thisLength
                    thisLength = defaultCommandHeader.length
                    if (thisLength isGreaterThan currentLength) currentLength = thisLength
                }
            }
        }
        return currentLength
    }
    fun longestAlias(): Int {
        var currentLength = 0
        this.command.forEach {
            if (it != null) {
                if (it.alias != null) {
                    var thisLength = it.alias!!.length
                    if (thisLength isGreaterThan currentLength ) currentLength = thisLength
                    thisLength = defaultCommandHeader.length
                    if (thisLength isGreaterThan currentLength) currentLength = thisLength
                }
            }
        }
        return currentLength
    }
    fun longestDescription(): Int {
        var currentLength = 0
        this.command.forEach {
            if (it != null) {
                if (it.description != null) {
                    var thisLineCurrentLength = 0
                    it.description!!.lines().forEach {
                        var thisLineLength = it.length
                        if (thisLineLength isGreaterThan thisLineCurrentLength) thisLineCurrentLength = thisLineLength
                        thisLineLength = defaultDescriptionHeader.length
                        if (thisLineLength isGreaterThan thisLineCurrentLength) thisLineCurrentLength = thisLineLength
                    }
                    val thisLength = thisLineCurrentLength
                    if (thisLength isGreaterThan currentLength) currentLength = thisLength
                }
            }
        }
        return currentLength
    }
    inner class Format {
        val seperationLength = 2
        val spacing = 2
        inner class PrettyPrint {
            inner class Frame {
                inner class Corner {
                    var topRight: String = "┓"
                    var bottomRight: String = "┛"
                    var bottomLeft: String = "┗"
                    var topLeft: String = "┏"
                }
                inner class Wall {
                    var top: String = "━"
                    var right: String = "┃"
                    var bottom: String = "━"
                    var left: String = "┃"
                }
                inner class Intersection {
                    var top: String = "┳"
                    var right: String = "┣"
                    var bottom: String = "┻"
                    var left: String = "┫"
                    var all: String = "╋"
                }
            }
            fun single(command: String, description: String) {
                val lengthAlias = longestAlias()
                val lengthCommand = longestCommand()
                val length = if (lengthAlias isGreaterThan lengthCommand) lengthAlias else lengthCommand
                if(length isLessThanOrEqualTo 0) return
                println(
                    // top
                    Frame().Corner().topLeft
                            + "".padExtendEnd(seperationLength, Frame().Wall().top)
                            + "".padExtendEnd(length, Frame().Wall().top)
                            + "".padExtendEnd(seperationLength, Frame().Wall().top)
                            + Frame().Intersection().top
                            + "".padExtendEnd(seperationLength, Frame().Wall().top)
                            + "".padExtendEnd(longestDescription(), Frame().Wall().top)
                            + "".padExtendEnd(seperationLength, Frame().Wall().top)
                            + Frame().Corner().topRight
                            + "\n"
                            // middle top
                            + Frame().Wall().left
                            + "".padExtendEnd(seperationLength, ' ')
                            + defaultCommandHeader.padExtendEnd(length, ' ')
                            + "".padExtendEnd(seperationLength, ' ')
                            + "┃"
                            + "".padExtendEnd(seperationLength, ' ')
                            + defaultDescriptionHeader.padExtendEnd(longestDescription(), ' ')
                            + "".padExtendEnd(seperationLength, ' ')
                            + Frame().Wall().right
                            + "\n"
                            // seperator
                            + Frame().Intersection().right
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + "".padExtendEnd(length, Frame().Wall().bottom)
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + Frame().Intersection().all
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + "".padExtendEnd(longestDescription(), Frame().Wall().bottom)
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + Frame().Intersection().left
                            + "\n"
                            // middle
                            + Frame().Wall().left
                            + "".padExtendEnd(seperationLength, ' ')
                            + command.padExtendEnd(length, ' ')
                            + "".padExtendEnd(seperationLength, ' ')
                            + "┃"
                            + "".padExtendEnd(seperationLength, ' ')
                            + description.padExtendEnd(longestDescription(), ' ')
                            + "".padExtendEnd(seperationLength, ' ')
                            + Frame().Wall().right
                            + "\n"
                            // bottom
                            + Frame().Corner().bottomLeft
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + "".padExtendEnd(length, Frame().Wall().bottom)
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + Frame().Intersection().bottom
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + "".padExtendEnd(longestDescription(), Frame().Wall().bottom)
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + Frame().Corner().bottomRight
                )
            }
            fun top(command: String, description: String) {
                val lengthAlias = longestAlias()
                val lengthCommand = longestCommand()
                val length = if (lengthAlias isGreaterThan lengthCommand) lengthAlias else lengthCommand
                if(length isLessThanOrEqualTo 0) return
                println(
                    Frame().Corner().topLeft
                            + "".padExtendEnd(seperationLength, Frame().Wall().top)
                            + "".padExtendEnd(length, Frame().Wall().top)
                            + "".padExtendEnd(seperationLength, Frame().Wall().top)
                            + Frame().Intersection().top
                            + "".padExtendEnd(seperationLength, Frame().Wall().top)
                            + "".padExtendEnd(longestDescription(), Frame().Wall().top)
                            + "".padExtendEnd(seperationLength, Frame().Wall().top)
                            + Frame().Corner().topRight
                )
                middleTop(defaultCommandHeader, defaultDescriptionHeader)
                middleTop(command, description)
            }
            fun middleTop(command: String, description: String) {
                val lengthAlias = longestAlias()
                val lengthCommand = longestCommand()
                val length = if (lengthAlias isGreaterThan lengthCommand) lengthAlias else lengthCommand
                if(length isLessThanOrEqualTo 0) return
                println(
                    Frame().Wall().left
                            + "".padExtendEnd(seperationLength, ' ')
                            + command.padExtendEnd(length, ' ')
                            + "".padExtendEnd(seperationLength, ' ')
                            + "┃"
                            + "".padExtendEnd(seperationLength, ' ')
                            + description.padExtendEnd(longestDescription(), ' ')
                            + "".padExtendEnd(seperationLength, ' ')
                            + Frame().Wall().right
                )
                middleSeperator()
            }
            fun middleBottom(command: String, description: String) {
                Debugger().breakPoint()
                val lengthAlias = longestAlias()
                val lengthCommand = longestCommand()
                val length = if (lengthAlias isGreaterThan lengthCommand) lengthAlias else lengthCommand
                if(length isLessThanOrEqualTo 0) return
                println(
                    Frame().Wall().left
                            + "".padExtendEnd(seperationLength, ' ')
                            + command.padExtendEnd(length, ' ')
                            + "".padExtendEnd(seperationLength, ' ')
                            + "┃"
                            + "".padExtendEnd(seperationLength, ' ')
                            + description.padExtendEnd(longestDescription(), ' ')
                            + "".padExtendEnd(seperationLength, ' ')
                            + Frame().Wall().right
                )
            }
            fun middleSeperator() {
                val lengthAlias = longestAlias()
                val lengthCommand = longestCommand()
                val length = if (lengthAlias isGreaterThan lengthCommand) lengthAlias else lengthCommand
                if(length isLessThanOrEqualTo 0) return
                println(
                    Frame().Intersection().right
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + "".padExtendEnd(length, Frame().Wall().bottom)
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + Frame().Intersection().all
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + "".padExtendEnd(longestDescription(), Frame().Wall().bottom)
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + Frame().Intersection().left
                )
            }
            fun bottom(command: String, description: String) {
                val lengthAlias = longestAlias()
                val lengthCommand = longestCommand()
                val length = if (lengthAlias isGreaterThan lengthCommand) lengthAlias else lengthCommand
                if(length isLessThanOrEqualTo 0) return
                middleBottom(command, description)
                println(
                    Frame().Corner().bottomLeft
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + "".padExtendEnd(length, Frame().Wall().bottom)
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + Frame().Intersection().bottom
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + "".padExtendEnd(longestDescription(), Frame().Wall().bottom)
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + Frame().Corner().bottomRight
                )
            }
        }
        fun normal(command: String, description: String) {
            val lengthAlias = longestAlias()
            val lengthCommand = longestCommand()
            val length = if (lengthAlias isGreaterThan lengthCommand) lengthAlias else lengthCommand
            if(length isLessThanOrEqualTo 0) return
            println(command.padExtendEnd(length+seperationLength, ' ') + description)
        }
    }
    fun listCommands() {
        for ((index, it) in this.command.withIndex()) {
            when {
                it != null -> when {
                    (it.command != null && it.description != null) -> when {
                        this.command.count() == 1 -> Format().PrettyPrint().single(
                            when {
                                it.alias != null -> it.alias
                                else -> it.command
                            }!!, it.description!!)
                        else -> when(index) {
                            0 -> Format().PrettyPrint().top(
                                when {
                                    it.alias != null -> it.alias
                                    else -> it.command
                                }!!, it.description!!
                            )
                            this.command.count() - 1, this.command.count() -> Format().PrettyPrint().bottom(
                                when {
                                    it.alias != null -> it.alias
                                    else -> it.command
                                }!!, it.description!!
                            )
                            else -> Format().PrettyPrint().middleTop(
                                when {
                                    it.alias != null -> it.alias
                                    else -> it.command
                                }!!, it.description!!
                            )
                        }
                    }
                }
            }
        }
    }
}\"\"\",
                    listOf(
                        "kotlin",
                        "Grammar.kt", "linuxMain/kotlin/sample/kotlin/Grammar.kt", \"\"\"package sample.kotlin

import preprocessor.base.globalVariables
import preprocessor.core.Lexer
import preprocessor.utils.`class`.extensions.toByteArray

class Grammar {
    fun lexer(contents: String) {
        val lex = Lexer(
            contents.toByteArray(),
            globalVariables.tokensNewLine
        )
        lex.lex()
        if (lex.currentLine == null) return
        while (lex.currentLine != null) {
            lex
            lex.lex()
        }
    }
}\"\"\",
                        "Kotlin.kt", "linuxMain/kotlin/sample/kotlin/Kotlin.kt", \"\"\"@file:Suppress("unused")

package sample

import preprocessor.base.globalVariables
import preprocessor.core.Lexer
import preprocessor.core.Parser
import preprocessor.test.init
import preprocessor.utils.`class`.extensions.toByteArray
import preprocessor.utils.`class`.extensions.toStack
import preprocessor.utils.core.algorithms.Stack
import preprocessor.utils.extra.parse

class Kotlin(contents: String) {
    val parseStream = Parser(contents) { it.toStack() }
    inner class Lexer {
        val A = parseStream.IsSequenceOnce("A")
        val B = parseStream.IsSequenceOnce("B")
        val AB = A and B
        init {
            println(parseStream.toStringAsArray())
            println(A.peek())
            println("A.value = " + A.value)
            println("A.left?.value = " + A.left?.value)
            println("A.right?.value = " + A.right?.value)
            println("B.value = " + B.value)
            println("B.left?.value = " + B.left?.value)
            println("B.right?.value = " + B.right?.value)
            println("AB.value = " + AB.value)
            println("AB.left?.value = " + AB.left?.value)
            println("AB.right?.value = " + AB.right?.value)
        }
    }

    inner class Parser {
        fun parse() {
        }
    }
}

private infix fun Parser.IsSequenceOnce.or(and: Parser.IsSequenceOnce): Parser.IsSequenceOnce {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

fun Parser.isSequenceOnceArray(): Parser {
    return clone()
}\"\"\"
                    )""",
                    "SampleLinux.kt", "linuxMain/kotlin/sample/SampleLinux.kt", """package sample

import Hierarchy
import preprocessor.test.Tests
import preprocessor.test.init
import preprocessor.utils.core.abort
import preprocessor.utils.extra.parse

// https://medium.com/@elye.project/mastering-kotlin-standard-functions-run-with-let-also-and-apply-9cd334b0ef84

fun printUsage() {
    println("Usage:")
    println("cat FILE | thisProgram")
    println("echo hai | thisProgram")
    println("echo \"hai\" | thisProgram")
    println("bash\${'$'} thisProgram < FILE")
    println("bash\${'$'} thisProgram <<EOF\nlines\nEOF")
    println("bash\${'$'} thisProgram <<<\"lines\nof\ntext\"")
    println("thisProgram line1 line2 ...")
    println("thisProgram \"line 1\" \"line 2\" ...")
    println("thisProgram hai --test hai --debug hai --test hai ...")
    println("and so on for every way you can send input to thisProgram")
}

fun printHelp() {
    println("Help:")
    println("Flags that affect how arguments are processed")
    println("-h,  --help                      print help")
    println("-u,  --usage                     print usage")
    println("-v,  --version                   print version information")
    println("-r,  --repl, -R, --REPL          start a REPL session")
    println("-t, --test                       test the macro preprocessor using its internal testing suite")
    println("-, --stdin                       read input from stdin\n" +
            "                                 default if no 'Flags that affect how arguments are processed' are given)"
    )
    println()
    println("Flags that do not affect how arguments are processed")
    println("-d,  --debug,   --debugon        enable debugging")
    println("-dd, --nodebug, --debugoff       disable debugging")
    abort()
}

val m = init()

fun processSTDIN() {
    var line = readLine()
    while (line != null) {
        println(parse(line, m, newlineFunction = {
            val x = readLine()
            if (x != null) x
            else abort("failed to grab a new line")
        }))
        line = readLine()
    }
}



fun printVersion() {
    println("Kotlin Pre Processor Version ${'$'}{preprocessor.base.globalVariables.version}")
    println("https://github.com/mgood7123/kpp-Native")
    println("Developer: Matthew James Good")
    println("with huge help by: https://cpplang.slack.com/team/UAG0Z05BQ - chill")
}


fun main(a: Array<String>) {
    val l = Kotlin("ABCDEF").Lexer()
    return
    Hierarchy().listFiles()
    if (a.isEmpty()) processSTDIN()
    else a.forEach {
        when(it) {
            "",
            "-h", "--help" -> printHelp()
            "-u", "--usage" -> printUsage()
            "-",  "--stdin" -> processSTDIN()
            "-v", "--version" -> printVersion()
            "-r", "--repl", "-R", "--REPL" -> REPL().REPL()
            "-d", "--debug", "--debugon" -> {
                preprocessor.base.globalVariables.flags.debug = true
                if (a.size == 1) processSTDIN()
            }
            "-dd", "--nodebug", "--debugoff" -> {
                preprocessor.base.globalVariables.flags.debug = false
                if (a.size == 1) processSTDIN()
            }
            "-t", "--test" -> Tests().doAll()
            else -> println(parse(it, m))
        }
    }
}
""",
                    "REPL.kt", "linuxMain/kotlin/sample/REPL.kt", """package sample

import preprocessor.test.init
import preprocessor.utils.`class`.extensions.ifTrueReturn
import preprocessor.utils.core.abort
import preprocessor.utils.extra.parse
import sample.Commands.Format.PrettyPrint

@Suppress("unused")
class REPL {
    var debug = false
    val env = init()
        .also {
            parse("#define VERSION ${'$'}{preprocessor.base.globalVariables.version}", it)
        }
        .also {
            parse("#define PS1 ${'$'}", it)
        }

    val commands = Commands(this)

    init {
        commands
            .alias("help", "list")
            .add("list", "lists all available commands") { commands.listCommands() }
            .add("set x", "enable build-in debugger") { this.debug = true }
            .add("set -x", "disable build-in debugger") { this.debug = false }
    }

    fun promt() {
        print(parse("PS1", env) + " ")
    }

    fun promt(PS1: String) {
        print("${'$'}PS1 ")
    }

    fun REPL() {
        // TODO make a proper REPL
        println(parse("Kotlin Pre Processor Version VERSION REPL BETA", env))
        promt()
        var line = readLine()
        while (line != null) {
            if (debug) println("line = ${'$'}line")
            if (line.ifTrueReturn(line.startsWith('/')) {
                    line = it.drop(1)
                }
            ) {
                val command = line as String
                if (command.isEmpty()) commands.get("help")?.invoke()
                else {
                    println("command = " + command)
                    commands.get(command)?.invoke()
//                    val str = StringBuilder()
//                    val parser = Parser(command.toStack())
//                    val space = parser.IsSequenceOneOrMany(" ")
//                    while (parser.peek() != null) {
//                        if (t.peek()) {
//                            t.pop()
//                            str.append(replaceWith)
//                        } else str.append(parser.pop()!!)
//                    }
                }
            }
            else println(parse(line!!, m, newlineFunction = {
                promt(it)
                val x = readLine()
                if (x != null) x
                else abort("failed to grab a new line")
            }))
            promt()
            line = readLine()
        }
    }
}""",
                    "Debugger.kt", "linuxMain/kotlin/sample/Debugger.kt", """package sample

import preprocessor.utils.core.abort

@Suppress("unused")
class Debugger {
    fun breakPoint() {
        val stack = Exception().getStackTrace().iterator()
        println("stack trace:")
        while (stack.hasNext()) {
            val e = stack.next()
            val o = e.substringAfter('(').substringBefore(')')
            val a: List<String>? = if (o.contains(';') || o.isNotBlank()) o.split(';') else null
            val m = e.substringBefore('(')
            if (m.isBlank()) break
            if (!m.contains(':'))  break
            val f = m.substringAfter(':')
            if (f.isBlank()) break
            if (f == "kotlin.Exception.<init>") {
                stack.next()
                continue
            }
            println("next stack:")
            println("    function: ${'$'}f")
            print("    function argument count: ")
            if (a == null) println("0")
            else {
                println(a.size)
                if (a.isNotEmpty()) {
                    var paramNumber = 0
                    a.forEach {
                        println("        parameter ${'$'}paramNumber is of type: ${'$'}it")
                        paramNumber++
                    }
                }
            }
            println("    entire function: ${'$'}e")
        }
    }
}
"""
                )
            )
        ),
        listOf(
            "linuxTest",
            listOf(
                "kotlin",
                listOf(
                    "sample",
                    "SampleTests.kt", "linuxTest/kotlin/sample/SampleTests.kt", """package sample

import kotlin.test.Test
import kotlin.test.assertTrue

fun hello(): String = "Hello, Kotlin/Native!"

class SampleTests {
    @Test
    fun testHello() {
        assertTrue("Kotlin/Native" in hello())
    }
}"""
                )
            )
        )
    )
}

