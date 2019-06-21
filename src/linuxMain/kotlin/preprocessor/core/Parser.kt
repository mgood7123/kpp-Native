package preprocessor.core

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
            // attempt to check for corruption
            if (try {
                    tokenList.pop()
                } catch (e: NoSuchElementException) {
                    null
                } != null) abort(preprocessor.base.globalVariables.depthAsString() + "token list is corrupted, or the desired exception 'java.util.NoSuchElementException' was not caught")
            else return null
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

    inner class Types {
        var once: IsSequenceOnce? = null
        var oneOrMany: IsSequenceOneOrMany? = null
        var zeroOrMany: IsSequenceZeroOrMany? = null
        fun get(): Any? = when {
            once != null -> once
            oneOrMany != null -> oneOrMany
            zeroOrMany != null -> zeroOrMany
            else -> null
        }
        fun add(p: Any) = when(p) {
            is IsSequenceOnce -> once = p
            is IsSequenceOneOrMany -> oneOrMany = p
            is IsSequenceZeroOrMany -> zeroOrMany = p
            else -> println("unsupported type")
        }
    }

    private fun cloneList(list: MutableList<Types>): MutableList<Types> {
        val tmp: MutableList<Types> = mutableListOf()
        var f = true
        var par: Parser? = null
        list.forEach {
            val type = when(it.get()) {
                is IsSequenceOnce -> 1
                is IsSequenceOneOrMany -> 2
                is IsSequenceZeroOrMany -> 3
                else -> {
                    println("UNKNOWN TYPE")
                    4
                }
            }
            if (f) {
                par = when(type) {
                    1 -> it.once!!.parent.clone()
                    2 -> it.oneOrMany!!.parent.clone()
                    else -> it.zeroOrMany!!.parent.clone()
                }
                f = false
            }
            val v = when(type) {
                1 -> it.once!!.value
                2 -> it.oneOrMany!!.value
                else -> it.zeroOrMany!!.value
            }
            tmp.add(
                Types().also {
                    it.add(
                        when(type) {
                            1 -> par!!.IsSequenceOnce(v)
                            2 -> par!!.IsSequenceOneOrMany(v)
                            else -> par!!.IsSequenceZeroOrMany(v)
                        }
                    )
                }
            )
        }
        return tmp
    }

    inner class Multi(str: String) {
        private val parent = this@Parser
        var list: MutableList<Types> = mutableListOf()
        val value: String = str

        fun printList() = list.forEach {
            val x = it.get()
            println("it.${when(x) {
                is IsSequenceOnce -> "once.value = " + x.value + " as IsSequenceOnce"
                is IsSequenceOneOrMany -> "oneOrMany.value = " + x.value + " as IsSequenceOneOrMany"
                else -> "zeroOrMany.value = " + (x as IsSequenceZeroOrMany).value + " as IsSequenceZeroOrMany"
            }}")
        }

        override fun toString(): String {
            val x = cloneList(list).iterator()
            val result = StringBuilder()
            while (x.hasNext()) {
                val y = x.next().get()
                if (
                    when(y) {
                        is IsSequenceOnce -> y.peek()
                        is IsSequenceOneOrMany -> y.peek()
                        else -> (y as IsSequenceZeroOrMany).peek() 
                    }
                ) {
                    val z = when(y) {
                        is IsSequenceOnce -> y.toString()
                        is IsSequenceOneOrMany -> y.toString()
                        else -> (y as IsSequenceZeroOrMany).toString()
                    }
                    if (
                        when(y) {
                            is IsSequenceOnce -> y.pop()
                            is IsSequenceOneOrMany -> y.pop()
                            else -> (y as IsSequenceZeroOrMany).pop()
                        }
                    ) result.append(z)
                    else break
                } else break
            }
            return result.toString()
        }

        fun peek(): Boolean {
            val x = cloneList(list).iterator()
            var m = 0
            val max = list.size
            while (x.hasNext()) {
                val y = x.next().get()
                if (
                    when(y) {
                        is IsSequenceOnce -> y.peek()
                        is IsSequenceOneOrMany -> y.peek()
                        else -> (y as IsSequenceZeroOrMany).peek()
                    }
                ) {
                    if (
                        when(y) {
                            is IsSequenceOnce -> y.pop()
                            is IsSequenceOneOrMany -> y.pop()
                            else -> (y as IsSequenceZeroOrMany).pop()
                        }
                    ) m++
                    else break
                } else break
            }
            if (m == max) return true
            return false
        }

        fun pop(): Boolean {
            val x = list.iterator()
            var m = 0
            val max = list.size
            while (x.hasNext()) {
                val y = x.next().get()
                if (
                    when(y) {
                        is IsSequenceOnce -> y.peek()
                        is IsSequenceOneOrMany -> y.peek()
                        else -> (y as IsSequenceZeroOrMany).peek()
                    }
                ) {
                    if (
                        when(y) {
                            is IsSequenceOnce -> y.pop()
                            is IsSequenceOneOrMany -> y.pop()
                            else -> (y as IsSequenceZeroOrMany).pop()
                        }
                    ) m++
                    else break
                } else break
            }
            if (m == max) return true
            return false
        }

        infix fun and(right: Multi): Multi {
            val x = parent.clone().Multi(this.value)
            x.list.addAll(this.list)
            x.list.addAll(right.list)
            return x
        }

        infix fun and(right: IsSequenceZeroOrMany): Multi {
            val x = parent.clone().Multi(this.value)
            x.list.addAll(this.list)
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right) })
            return x
        }

        infix fun and(right: IsSequenceOneOrMany): Multi {
            val x = parent.clone().Multi(this.value)
            x.list.addAll(this.list)
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right) })
            return x
        }

        infix fun and(right: IsSequenceOnce): Multi {
            val x = parent.clone().Multi(this.value)
            x.list.addAll(this.list)
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right) })
            return x
        }
    }

    /**
     * matches a sequence of **str** zero or more times
     * @see IsSequenceOneOrMany
     * @see IsSequenceOnce
     */
    inner class IsSequenceZeroOrMany(str: String) {
        val parent = this@Parser
        var list: MutableList<Types> = mutableListOf()
        val value: String = str
        private var seq: IsSequenceOneOrMany = IsSequenceOneOrMany(value)

        fun printList() = list.forEach {
            val x = it.get()
            println("it.${when(x) {
                is IsSequenceOnce -> "once.value = " + x.value + " as IsSequenceOnce"
                is IsSequenceOneOrMany -> "oneOrMany.value = " + x.value + " as IsSequenceOneOrMany"
                else -> "zeroOrMany.value = " + (x as IsSequenceZeroOrMany).value + " as IsSequenceZeroOrMany"
            }}")
        }

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
        override fun toString(): String = if (list.size != 0) toStringMultiple() else toStringSingle()

        private fun toStringSingle(): String {
            return seq.toString()
        }

        private fun toStringMultiple(): String {
            val x = cloneList(list).iterator()
            var m = 0
            val max = list.size
            val result = StringBuilder()
            while (x.hasNext()) {
                val y = x.next().zeroOrMany!!
                if (y.peek()) {
                    val z = y.toString()
                    if (y.pop()) result.append(z)
                    else break
                } else break
            }
            return result.toString()
        }

        /**
         * this function always returns true
         * @see toString
         * @see pop
         */
        fun peek(): Boolean = if (list.size != 0) peekMultiple() else peekSingle()

        private fun peekSingle(): Boolean {
            return true
        }

        private fun peekMultiple(): Boolean {
            val x = cloneList(list).iterator()
            var m = 0
            val max = list.size
            while (x.hasNext()) {
                val y = x.next().zeroOrMany!!
                if (y.peek()) {
                    if (y.pop()) m++
                    else break
                } else break
            }
            if (m == max) return true
            return false
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
        fun pop(): Boolean = if (list.size != 0) popMultiple() else popSingle()

        private fun popSingle(): Boolean {
            while (seq.peek()) seq.pop()
            return true
        }

        private fun popMultiple(): Boolean {
            val x = list.iterator()
            var m = 0
            val max = list.size
            while (x.hasNext()) {
                val y = x.next().zeroOrMany!!
                if (y.peek()) {
                    if (y.pop()) m++
                    else break
                } else break
            }
            if (m == max) return true
            return false
        }

        infix fun and(right: IsSequenceZeroOrMany): IsSequenceZeroOrMany {
            val x = parent.clone().IsSequenceZeroOrMany(this.value)
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this) })
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right) })
            return x
        }

        infix fun and(right: IsSequenceOneOrMany): Multi {
            val x = parent.clone().Multi(this.value)
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this) })
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right) })
            return x
        }

        infix fun and(right: IsSequenceOnce): Multi {
            val x = parent.clone().Multi(this.value)
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this) })
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right) })
            return x
        }
    }
    /**
     * matches a sequence of **str** one or more times
     * @see IsSequenceZeroOrMany
     * @see IsSequenceOnce
     */
    inner class IsSequenceOneOrMany(str: String) {
        val parent = this@Parser
        var list: MutableList<Types> = mutableListOf()
        val value: String = str

        fun printList() = list.forEach {
            val x = it.get()
            println("it.${when(x) {
                is IsSequenceOnce -> "once.value = " + x.value + " as IsSequenceOnce"
                is IsSequenceOneOrMany -> "oneOrMany.value = " + x.value + " as IsSequenceOneOrMany"
                else -> "zeroOrMany.value = " + (x as IsSequenceZeroOrMany).value + " as IsSequenceZeroOrMany"
            }}")
        }

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
        override fun toString(): String = if (list.size != 0) toStringMultiple() else toStringSingle()

        private fun toStringSingle(): String {
            val o = clone().IsSequenceOnce(value)
            val result = StringBuilder()
            while (o.peek()) {
                result.append(o.toString())
                o.pop()
            }
            return result.toString()
        }

        private fun toStringMultiple(): String {
            val x = cloneList(list).iterator()
            val result = StringBuilder()
            while (x.hasNext()) {
                val y = x.next().oneOrMany!!
                if (y.peek()) {
                    val z = y.toString()
                    if (y.pop()) result.append(z)
                    else break
                } else break
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
        fun peek(): Boolean = if (list.size != 0) peekMultiple() else peekSingle()

        private fun peekSingle(): Boolean {
            val o = clone().IsSequenceOnce(value)
            var matches = 0
            while (o.peek()) {
                matches++
                o.pop()
            }
            if (matches != 0) return true
            return false
        }

        private fun peekMultiple(): Boolean {
            val x = cloneList(list).iterator()
            var m = 0
            val max = list.size
            while (x.hasNext()) {
                val y = x.next().oneOrMany!!
                if (y.peek()) {
                    if (y.pop()) m++
                    else break
                } else break
            }
            if (m == max) return true
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
        fun pop(): Boolean = if (list.size != 0) popMultiple() else popSingle()

        private fun popSingle(): Boolean {
            val o = IsSequenceOnce(value)
            var matches = 0
            while (o.peek()) {
                matches++
                o.pop()
            }
            if (matches != 0) return true
            return false
        }

        private fun popMultiple(): Boolean {
            val x = list.iterator()
            var m = 0
            val max = list.size
            while (x.hasNext()) {
                val y = x.next().oneOrMany!!
                if (y.peek()) {
                    if (y.pop()) m++
                    else break
                } else break
            }
            if (m == max) return true
            return false
        }

        infix fun and(right: IsSequenceZeroOrMany): Multi {
            val x = parent.clone().Multi(this.value)
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this) })
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right) })
            return x
        }

        infix fun and(right: IsSequenceOneOrMany): IsSequenceOneOrMany {
            val x = parent.clone().IsSequenceOneOrMany(this.value)
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this) })
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right) })
            return x
        }

        infix fun and(right: IsSequenceOnce): Multi {
            val x = parent.clone().Multi(this.value)
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this) })
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right) })
            return x
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
        val parent = this@Parser
        var list: MutableList<Types> = mutableListOf()

        fun printList() = list.forEach {
            val x = it.get()
            println("it.${when(x) {
                is IsSequenceOnce -> "once.value = " + x.value + " as IsSequenceOnce"
                is IsSequenceOneOrMany -> "oneOrMany.value = " + x.value + " as IsSequenceOneOrMany"
                else -> "zeroOrMany.value = " + (x as IsSequenceZeroOrMany).value + " as IsSequenceZeroOrMany"
            }}")
        }

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
        override fun toString(): String = if (list.size != 0) toStringMultiple() else toStringSingle()

        private fun toStringSingle(): String {
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

        private fun toStringMultiple(): String {
            val x = cloneList(list).iterator()
            val result = StringBuilder()
            while (x.hasNext()) {
                val y = x.next().once!!
                if (y.peek()) {
                    result.append(y.toString())
                    y.pop()
                } else break
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
        fun peek(): Boolean = if (list.size != 0) peekMultiple() else peekSingle()

        private fun peekSingle(): Boolean {
            val tmp = tokenList.clone()
            val s = stackMethodFunction(value)
            if (s.peek() == null || tmp.peek() == null) return false
            val expected = s.size
            var matches = 0
            while (tmp.peek() != null && s.peek() != null) if (tmp.pop() == s.pop()) matches++
            if (matches == expected) return true
            return false
        }

        private fun peekMultiple(): Boolean {
            val x = cloneList(list).iterator()
            var m = 0
            val max = list.size
            while (x.hasNext()) {
                val y = x.next().once!!
                if (y.peek()) {
                    m++
                    y.pop()
                } else break
            }
            if (m == max) return true
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
        fun pop(): Boolean = if (list.size != 0) popMultiple() else popSingle()

        private fun popSingle(): Boolean {
            val s = stackMethodFunction(value)
            if (s.peek() == null) return false
            val expected = s.size
            var matches = 0
            while (this@Parser.peek() != null && s.peek() != null) if (this@Parser.pop().equals(s.pop())) matches++
            if (matches == expected) return true
            return false
        }

        private fun popMultiple(): Boolean {
            val x = list.iterator()
            var m = 0
            val max = list.size
            while (x.hasNext()) {
                val y = x.next().once!!
                if (y.peek()) {
                    m++
                    y.pop()
                } else break
            }
            if (m == max) return true
            return false
        }

        infix fun and(right: IsSequenceZeroOrMany): Multi {
            val x = parent.clone().Multi(this.value)
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this) })
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right) })
            return x
        }

        infix fun and(right: IsSequenceOneOrMany): Multi {
            val x = parent.clone().Multi(this.value)
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this) })
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right) })
            return x
        }

        infix fun and(right: IsSequenceOnce): IsSequenceOnce {
            val x = parent.clone().IsSequenceOnce(this.value)
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this) })
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right) })
            return x
        }
    }
}