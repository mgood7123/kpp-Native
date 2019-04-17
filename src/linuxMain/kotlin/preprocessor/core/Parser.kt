package preprocessor.core

import preprocessor.base.globalVariables
import preprocessor.utils.core.abort
import preprocessor.utils.core.algorithms.Stack
import preprocessor.utils.core.algorithms.tokenize

/**
 * prepares the input **line** for consumption by the Parser
 * @return an [Stack] which is [split][tokenize] by the global variable **tokens**
 *
 * this [Stack] preserves the tokens in which it is split by
 * @see Parser
 */
fun parserPrep(line: String): Stack<String> = Stack<String>().also { it.addLast(line.tokenize(globalVariables.tokens, true)) }

/**
 * a minimal Parser implementation
 * @param tokens a list of tokens to split by
 * @see preprocessor.globals.Globals.tokens
 * @see Lexer
 */
@Suppress("unused")
class Parser(tokens: Stack<String>) {
    /**
     * a line represented as an [Stack] as returned by [parserPrep]
     * @see preprocessor.globals.Globals.tokens
     */
    var tokenList: Stack<String> = tokens

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
        val result = StringBuilder()
        tokenList.forEach {
            result.append(it!!)
        }
        return Parser(parserPrep(result.toString()))
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
        private val sg: String = str
        private val seq: IsSequenceOneOrMany = IsSequenceOneOrMany(sg)

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
        @Suppress("MemberVisibilityCanBePrivate")
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
        @Suppress("MemberVisibilityCanBePrivate")
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
        private val sg: String = str

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
            val o = clone().IsSequenceOnce(sg)
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
         * @return true if [sg] matches at least once
         * @see IsSequenceOnce
         * @see toString
         * @see pop
         */
        @Suppress("MemberVisibilityCanBePrivate")
        fun peek(): Boolean {
            val o = clone().IsSequenceOnce(sg)
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
         * @return true if [sg] matches at least once
         * @see IsSequenceOnce
         * @see IsSequenceOnce.pop
         * @see toString
         * @see peek
         */
        @Suppress("MemberVisibilityCanBePrivate")
        fun pop(): Boolean {
            val o = IsSequenceOnce(sg)
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
        /**
         * the string to match
         */
        private val sg = str

        /**
         * returns the accumulative value of [Parser.peek] and [Parser.pop]
         *
         * the original [tokenList] is [cloned][Parser.clone] to prevent modifications to the original [list][tokenList]
         *
         * @return "" if either [tokenList] or [str][sg] is empty
         *
         * otherwise the result of [Parser.peek], which can never be an empty string
         * @see peek
         * @see pop
         */
        override fun toString(): String {
            val tmp = tokenList.clone()
            val s = parserPrep(sg)
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
         * @return false if [str][sg] does not match
         *
         * otherwise returns true
         * @see toString
         * @see pop
         */
        @Suppress("MemberVisibilityCanBePrivate")
        fun peek(): Boolean {
            val tmp = tokenList.clone()
            val s = parserPrep(sg)
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
         * @return false if [str][sg] does not match
         *
         * otherwise returns true
         * @see toString
         * @see peek
         */
        @Suppress("MemberVisibilityCanBePrivate")
        fun pop(): Boolean {
            val s = parserPrep(sg)
            if (s.peek() == null) return false
            val expected = s.size
            var matches = 0
            while (this@Parser.peek() != null && s.peek() != null) if (this@Parser.pop().equals(s.pop())) matches++
            if (matches == expected) return true
            return false
        }
    }
}
