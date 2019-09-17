package preprocessor.core

import preprocessor.base.globalVariables
import preprocessor.utils.`class`.extensions.lastIndex
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
                } != null
            ) abort(preprocessor.base.globalVariables.depthAsString() + "token list is corrupted, or the desired exception 'java.util.NoSuchElementException' was not caught")
            else return null
        }
        val returnValue =
            tokenList.pop() ?: abort(preprocessor.base.globalVariables.depthAsString() + "token list is corrupted")
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
        var AND = 0
        var OR = 1
        fun typeToString(type: Int?): String {
            return when (type) {
                AND -> "And"
                OR -> "Or"
                null -> "No associated type"
                else -> "Unknown associated type"
            }
        }

        fun typeToString(): String = typeToString(type)

        var once: IsSequenceOnce? = null
        var oneOrMany: IsSequenceOneOrMany? = null
        var zeroOrMany: IsSequenceZeroOrMany? = null
        var type: Int? = null
        fun get(): Any? = when {
            once != null -> once
            oneOrMany != null -> oneOrMany
            zeroOrMany != null -> zeroOrMany
            else -> null
        }

        fun add(p: IsSequenceZeroOrMany, type: Int) {
            this.type = type
            zeroOrMany = p
        }

        fun add(p: IsSequenceOneOrMany, type: Int) {
            this.type = type
            oneOrMany = p
        }

        fun add(p: IsSequenceOnce, type: Int) {
            this.type = type
            once = p
        }
    }

    private fun cloneList(list: MutableList<Types>): MutableList<Types> {
        val tmp: MutableList<Types> = mutableListOf()
        var f = true
        var par: Parser? = null
        list.forEach {
            val type = when (it.get()) {
                is IsSequenceOnce -> 1
                is IsSequenceOneOrMany -> 2
                is IsSequenceZeroOrMany -> 3
                else -> {
                    println("UNKNOWN TYPE")
                    4
                }
            }
            if (f) {
                par = when (type) {
                    1 -> it.once!!.parent.clone()
                    2 -> it.oneOrMany!!.parent.clone()
                    else -> it.zeroOrMany!!.parent.clone()
                }
                f = false
            }
            val v = when (type) {
                1 -> it.once!!.value
                2 -> it.oneOrMany!!.value
                else -> it.zeroOrMany!!.value
            }
            tmp.add(
                Types().also { t ->
                    when (type) {
                        1 -> t.add(par!!.IsSequenceOnce(v), it.type!!)
                        2 -> t.add(par!!.IsSequenceOneOrMany(v), it.type!!)
                        else -> t.add(par!!.IsSequenceZeroOrMany(v), it.type!!)
                    }
                }
            )
        }
        return tmp
    }

    inner class Multi(str: String) {
        private val parent = this@Parser
        var list: MutableList<Types> = mutableListOf()
        val value: String = str
        var type: Int? = null

        fun printList() = list.forEach {
            val x = it.get()
            println(
                "it.${when (x) {
                    is IsSequenceOnce -> "once.value = " + x.value + " as IsSequenceOnce " + it.typeToString()
                    is IsSequenceOneOrMany -> "oneOrMany.value = " + x.value + " as IsSequenceOneOrMany " + it.typeToString()
                    else -> "zeroOrMany.value = " + (x as IsSequenceZeroOrMany).value + " as IsSequenceZeroOrMany " + it.typeToString()
                }}"
            )
        }

        override fun toString(): String {
            val x = cloneList(list).iterator()
            val result = StringBuilder()
            while (x.hasNext()) {
                val y = x.next().get()
                if (
                    when (y) {
                        is IsSequenceOnce -> y.peek()
                        is IsSequenceOneOrMany -> y.peek()
                        else -> (y as IsSequenceZeroOrMany).peek()
                    }
                ) {
                    val z = when (y) {
                        is IsSequenceOnce -> y.toString()
                        is IsSequenceOneOrMany -> y.toString()
                        else -> (y as IsSequenceZeroOrMany).toString()
                    }
                    if (
                        when (y) {
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
                    when (y) {
                        is IsSequenceOnce -> y.peek()
                        is IsSequenceOneOrMany -> y.peek()
                        else -> (y as IsSequenceZeroOrMany).peek()
                    }
                ) {
                    if (
                        when (y) {
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
                    when (y) {
                        is IsSequenceOnce -> y.peek()
                        is IsSequenceOneOrMany -> y.peek()
                        else -> (y as IsSequenceZeroOrMany).peek()
                    }
                ) {
                    if (
                        when (y) {
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

        infix fun and(right: GroupCombination): GroupCombination {
            // promote to combination
            val y = Combination()
            y.add(this, Types().AND)
            return y and right
        }

        infix fun and(right: Group): GroupCombination {
            // promote to combination
            val y = Combination()
            y.add(this, Types().AND)
            return y and right
        }

        infix fun and(right: Combination): Combination {
            // promote to combination
            val y = Combination()
            y.add(this, Types().AND)
            return y and right
        }

        infix fun and(right: Multi): Multi {
            val x = parent.clone().Multi(this.value)
            x.list.addAll(this.list)
            x.list.addAll(right.list)
            return x
        }

        infix fun and(right: IsSequenceZeroOrMany): Multi {
            val x = parent.clone().Multi(this.value)
            if (this.type == null || this.type == Types().AND) x.type = Types().AND
            if (right.type == null || right.type == Types().AND) x.type = Types().AND
            x.list.addAll(this.list)
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.AND) })
            return x
        }

        infix fun and(right: IsSequenceOneOrMany): Multi {
            val x = parent.clone().Multi(this.value)
            if (this.type == null || this.type == Types().AND) x.type = Types().AND
            if (right.type == null || right.type == Types().AND) x.type = Types().AND
            x.list.addAll(this.list)
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.AND) })
            return x
        }

        infix fun and(right: IsSequenceOnce): Multi {
            val x = parent.clone().Multi(this.value)
            if (this.type == null || this.type == Types().AND) x.type = Types().AND
            if (right.type == null || right.type == Types().AND) x.type = Types().AND
            x.list.addAll(this.list)
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.AND) })
            return x
        }

        infix fun or(right: GroupCombination): GroupCombination {
            // promote to combination
            val y = Combination()
            y.add(this, Types().OR)
            return y or right
        }

        infix fun or(right: Group): GroupCombination {
            // promote to combination
            val y = Combination()
            y.add(this, Types().OR)
            return y or right
        }

        infix fun or(right: Combination): Combination {
            // promote to combination
            val y = Combination()
            y.add(this, Types().OR)
            return y or right
        }

        infix fun or(right: Multi): Multi {
            val x = parent.clone().Multi(this.value)
            x.list.addAll(this.list)
            x.list.addAll(right.list)
            return x
        }

        infix fun or(right: IsSequenceZeroOrMany): Multi {
            val x = parent.clone().Multi(this.value)
            if (this.type == null || this.type == Types().OR) x.type = Types().OR
            if (right.type == null || right.type == Types().OR) x.type = Types().OR
            x.list.addAll(this.list)
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.OR) })
            return x
        }

        infix fun or(right: IsSequenceOneOrMany): Multi {
            val x = parent.clone().Multi(this.value)
            if (this.type == null || this.type == Types().OR) x.type = Types().OR
            if (right.type == null || right.type == Types().OR) x.type = Types().OR
            x.list.addAll(this.list)
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.OR) })
            return x
        }

        infix fun or(right: IsSequenceOnce): Multi {
            val x = parent.clone().Multi(this.value)
            if (this.type == null || this.type == Types().OR) x.type = Types().OR
            if (right.type == null || right.type == Types().OR) x.type = Types().OR
            x.list.addAll(this.list)
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.OR) })
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
        var type: Int? = null
        private var seq: IsSequenceOneOrMany = IsSequenceOneOrMany(value)

        fun printList() = list.forEach {
            val x = it.get()
            println(
                "it.${when (x) {
                    is IsSequenceOnce -> "once.value = " + x.value + " as IsSequenceOnce " + it.typeToString()
                    is IsSequenceOneOrMany -> "oneOrMany.value = " + x.value + " as IsSequenceOneOrMany " + it.typeToString()
                    else -> "zeroOrMany.value = " + (x as IsSequenceZeroOrMany).value + " as IsSequenceZeroOrMany " + it.typeToString()
                }}"
            )
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

        infix fun and(right: GroupCombination): GroupCombination = right and this

        infix fun and(right: Group): GroupCombination = right and this

        infix fun and(right: Combination): Combination = right and this

        infix fun and(right: Multi): Multi = right and this

        infix fun and(right: IsSequenceZeroOrMany): Combination {
            val y = Combination()
            val x = parent.clone().Multi(this.value)
            if (this.type == null || this.type == Types().AND) x.type = Types().AND
            if (right.type == null || right.type == Types().AND) x.type = Types().AND
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this, it.AND) })
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.AND) })
            y.add(x, Types().AND)
            return y
        }

        infix fun and(right: IsSequenceOneOrMany): Combination {
            val y = Combination()
            val x = parent.clone().Multi(this.value)
            if (this.type == null || this.type == Types().AND) x.type = Types().AND
            if (right.type == null || right.type == Types().AND) x.type = Types().AND
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this, it.AND) })
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.AND) })
            y.add(x, Types().AND)
            return y
        }

        infix fun and(right: IsSequenceOnce): Combination {
            val y = Combination()
            val x = parent.clone().Multi(this.value)
            if (this.type == null || this.type == Types().AND) x.type = Types().AND
            if (right.type == null || right.type == Types().AND) x.type = Types().AND
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this, it.AND) })
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.AND) })
            y.add(x, Types().AND)
            return y
        }

        infix fun or(right: GroupCombination): GroupCombination = right or  this

        infix fun or(right: Group): GroupCombination = right or this

        infix fun or(right: Combination): Combination = right or this

        infix fun or(right: Multi): Multi = right or this

        infix fun or(right: IsSequenceZeroOrMany): Combination {
            val y = Combination()
            val x = parent.clone().Multi(this.value)
            if (this.type == null || this.type == Types().OR) x.type = Types().OR
            if (right.type == null || right.type == Types().OR) x.type = Types().OR
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this, it.OR) })
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.OR) })
            y.add(x, Types().OR)
            return y
        }

        infix fun or(right: IsSequenceOneOrMany): Combination {
            val y = Combination()
            val x = parent.clone().Multi(this.value)
            if (this.type == null || this.type == Types().OR) x.type = Types().OR
            if (right.type == null || right.type == Types().OR) x.type = Types().OR
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this, it.OR) })
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.OR) })
            y.add(x, Types().OR)
            return y
        }

        infix fun or(right: IsSequenceOnce): Combination {
            val y = Combination()
            val x = parent.clone().Multi(this.value)
            if (this.type == null || this.type == Types().OR) x.type = Types().OR
            if (right.type == null || right.type == Types().OR) x.type = Types().OR
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this, it.OR) })
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.OR) })
            y.add(x, Types().OR)
            return y
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
        var type: Int? = null

        fun printList() = list.forEach {
            val x = it.get()
            println(
                "it.${when (x) {
                    is IsSequenceOnce -> "once.value = " + x.value + " as IsSequenceOnce " + it.typeToString()
                    is IsSequenceOneOrMany -> "oneOrMany.value = " + x.value + " as IsSequenceOneOrMany " + it.typeToString()
                    else -> "zeroOrMany.value = " + (x as IsSequenceZeroOrMany).value + " as IsSequenceZeroOrMany " + it.typeToString()
                }}"
            )
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

        infix fun and(right: GroupCombination): GroupCombination = right and this

        infix fun and(right: Group): GroupCombination = right and this

        infix fun and(right: Combination): Combination = right and this

        infix fun and(right: Multi): Multi = right and this

        infix fun and(right: IsSequenceZeroOrMany): Combination {
            val y = Combination()
            val x = parent.clone().Multi(this.value)
            if (this.type == null || this.type == Types().AND) x.type = Types().AND
            if (right.type == null || right.type == Types().AND) x.type = Types().AND
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this, it.AND) })
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.AND) })
            y.add(x, Types().AND)
            return y
        }

        infix fun and(right: IsSequenceOneOrMany): Combination {
            val y = Combination()
            val x = parent.clone().Multi(this.value)
            if (this.type == null || this.type == Types().AND) x.type = Types().AND
            if (right.type == null || right.type == Types().AND) x.type = Types().AND
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this, it.AND) })
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.AND) })
            y.add(x, Types().AND)
            return y
        }

        infix fun and(right: IsSequenceOnce): Combination {
            val y = Combination()
            val x = parent.clone().Multi(this.value)
            if (this.type == null || this.type == Types().AND) x.type = Types().AND
            if (right.type == null || right.type == Types().AND) x.type = Types().AND
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this, it.AND) })
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.AND) })
            y.add(x, Types().AND)
            return y
        }

        infix fun or(right: GroupCombination): GroupCombination = right or  this

        infix fun or(right: Group): GroupCombination = right or this

        infix fun or(right: Combination): Combination = right or this

        infix fun or(right: Multi): Multi = right or this

        infix fun or(right: IsSequenceZeroOrMany): Combination {
            val y = Combination()
            val x = parent.clone().Multi(this.value)
            if (this.type == null || this.type == Types().OR) x.type = Types().OR
            if (right.type == null || right.type == Types().OR) x.type = Types().OR
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this, it.OR) })
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.OR) })
            y.add(x, Types().OR)
            return y
        }

        infix fun or(right: IsSequenceOneOrMany): Combination {
            val y = Combination()
            val x = parent.clone().Multi(this.value)
            if (this.type == null || this.type == Types().OR) x.type = Types().OR
            if (right.type == null || right.type == Types().OR) x.type = Types().OR
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this, it.OR) })
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.OR) })
            y.add(x, Types().OR)
            return y
        }

        infix fun or(right: IsSequenceOnce): Combination {
            val y = Combination()
            val x = parent.clone().Multi(this.value)
            if (this.type == null || this.type == Types().OR) x.type = Types().OR
            if (right.type == null || right.type == Types().OR) x.type = Types().OR
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this, it.OR) })
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.OR) })
            y.add(x, Types().OR)
            return y
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
        var type: Int? = null

        fun printList() = list.forEach {
            val x = it.get()
            println(
                "it.${when (x) {
                    is IsSequenceOnce -> "once.value = " + x.value + " as IsSequenceOnce " + it.typeToString()
                    is IsSequenceOneOrMany -> "oneOrMany.value = " + x.value + " as IsSequenceOneOrMany " + it.typeToString()
                    else -> "zeroOrMany.value = " + (x as IsSequenceZeroOrMany).value + " as IsSequenceZeroOrManytype " + it.typeToString()
                }}"
            )
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
        fun peek(): Boolean = if (list.size != 0) {
            if (type == Types().AND) peekMultipleAnd()
            else peekMultipleOr()
        } else peekSingle()

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

        private fun peekMultipleAnd(): Boolean {
            val x = cloneList(list).iterator()
            var m = 0
            val max = list.size
            while (x.hasNext()) {
                val p = x.next().once!!
                if (p.peek()) {
                    m++
                    p.pop()
                } else break
            }
            if (m == max) return true
            return false
        }

        private fun peekMultipleOr(): Boolean {
            val x = cloneList(list).iterator()
            while (x.hasNext()) {
                val p = x.next().once!!
                if (p.peek()) {
                    p.pop()
                    return true
                }
            }
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
        fun pop(): Boolean = if (list.size != 0) {
            if (type == Types().AND) popMultipleAnd()
            else popMultipleOr()
        } else popSingle()

        private fun popSingle(): Boolean {
            val s = stackMethodFunction(value)
            if (s.peek() == null) return false
            val expected = s.size
            var matches = 0
            while (this@Parser.peek() != null && s.peek() != null) if (this@Parser.pop().equals(s.pop())) matches++
            if (matches == expected) return true
            return false
        }

        private fun popMultipleAnd(): Boolean {
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

        private fun popMultipleOr(): Boolean {
            val x = list.iterator()
            while (x.hasNext()) {
                val y = x.next().once!!
                if (y.peek()) {
                    y.pop()
                    return true
                } else break
            }
            return false
        }

        infix fun and(right: GroupCombination): GroupCombination {
            // promote to Multi
            val x = parent.clone().Multi(this.value)
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this, it.AND) })
            return x and right
        }

        infix fun and(right: Group): GroupCombination {
            // promote to Multi
            val x = parent.clone().Multi(this.value)
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this, it.AND) })
            return x and right
        }

        infix fun and(right: Combination): Combination {
            // promote to Multi
            val x = parent.clone().Multi(this.value)
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this, it.AND) })
            return x and right
        }

        infix fun and(right: Multi): Multi {
            // promote to Multi
            val x = parent.clone().Multi(this.value)
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this, it.AND) })
            return x and right
        }

        infix fun and(right: IsSequenceZeroOrMany): Combination {
            val y = Combination()
            val x = parent.clone().Multi(this.value)
            if (this.type == null || this.type == Types().AND) x.type = Types().AND
            if (right.type == null || right.type == Types().AND) x.type = Types().AND
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this, it.AND) })
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.AND) })
            y.add(x, Types().AND)
            return y
        }

        infix fun and(right: IsSequenceOneOrMany): Combination {
            val y = Combination()
            val x = parent.clone().Multi(this.value)
            if (this.type == null || this.type == Types().AND) x.type = Types().AND
            if (right.type == null || right.type == Types().AND) x.type = Types().AND
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this, it.AND) })
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.AND) })
            y.add(x, Types().AND)
            return y
        }

        infix fun and(right: IsSequenceOnce): Combination {
            val y = Combination()
            val x = parent.clone().Multi(this.value)
            if (this.type == null || this.type == Types().AND) x.type = Types().AND
            if (right.type == null || right.type == Types().AND) x.type = Types().AND
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this, it.AND) })
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.AND) })
            y.add(x, Types().AND)
            return y
        }

        infix fun or(right: GroupCombination): GroupCombination {
            // promote to Multi
            val x = parent.clone().Multi(this.value)
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this, it.OR) })
            return x or right
        }

        infix fun or(right: Group): GroupCombination {
            // promote to Multi
            val x = parent.clone().Multi(this.value)
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this, it.OR) })
            return x or right
        }

        infix fun or(right: Combination): Combination {
            // promote to Multi
            val x = parent.clone().Multi(this.value)
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this, it.OR) })
            return x or right
        }

        infix fun or(right: Multi): Multi {
            // promote to Multi
            val x = parent.clone().Multi(this.value)
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this, it.OR) })
            return x or right
        }

        infix fun or(right: IsSequenceZeroOrMany): Combination {
            val y = Combination()
            val x = parent.clone().Multi(this.value)
            if (this.type == null || this.type == Types().OR) x.type = Types().OR
            if (right.type == null || right.type == Types().OR) x.type = Types().OR
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this, it.OR) })
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.OR) })
            y.add(x, Types().OR)
            return y
        }

        infix fun or(right: IsSequenceOneOrMany): Combination {
            val y = Combination()
            val x = parent.clone().Multi(this.value)
            if (this.type == null || this.type == Types().OR) x.type = Types().OR
            if (right.type == null || right.type == Types().OR) x.type = Types().OR
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this, it.OR) })
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.OR) })
            y.add(x, Types().OR)
            return y
        }

        infix fun or(right: IsSequenceOnce): Combination {
            val y = Combination()
            val x = parent.clone().Multi(this.value)
            if (this.type == null || this.type == Types().OR) x.type = Types().OR
            if (right.type == null || right.type == Types().OR) x.type = Types().OR
            if (this.list.size != 0) x.list.addAll(this.list) else x.list.add(Types().also { it.add(this, it.OR) })
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.OR) })
            y.add(x, Types().OR)
            return y
        }
    }

    inner class Combination() {
        inner class LIST {
            var type: Int? = null
            var list: Multi? = null
        }

        val parent = this@Parser
        var list: MutableList<LIST> = mutableListOf()
        fun add(List: Multi, type: Int) {
            // append list in order
            if (list.size != 0) {
                val t = list.lastIndex()
                if (t.type == Types().AND) {
                    if (type == Types().AND) {
                        t.list = t.list!! and List
                    } else {
                        val x = LIST()
                        x.type = type
                        x.list = List
                        list.add(x)
                    }
                } else {
                    if (type == Types().AND) {
                        val x = LIST()
                        x.type = type
                        x.list = List
                        list.add(x)
                    } else {
                        t.list = t.list!! and List
                    }
                }
            } else {
                val x = LIST()
                x.type = type
                x.list = List
                list.add(x)
            }
        }

        fun printList() = list.forEach {
            it.list!!.printList()
        }

        infix fun and(right: GroupCombination): GroupCombination {
            // promote to Group
            val g = Group(this)
            g.depth -= 2
            return g and right
        }

        infix fun and(right: Group): GroupCombination {
            // promote to Group
            val g = Group(this)
            right.depth--
            g.depth -= 2
            return g and right
        }

        infix fun and(right: Combination): Combination {
            // append list in order
            if (list.size != 0) {
                val t = this.list.lastIndex()
                if (t.type == Types().AND) {
                    right.list.forEach {
                        t.list = t.list!! and it.list!!
                    }
                } else {
                    val x = LIST()
                    x.type = Types().AND
                    right.list.forEach {
                        x.list = x.list!! and it.list!!
                    }
                    list.add(x)
                }
            } else {
                val x = LIST()
                x.type = Types().AND
                right.list.forEach {
                    x.list = x.list!! and it.list!!
                }
                list.add(x)
            }
            return this
        }

        infix fun and(right: Multi): Combination {
            // append list in order
            if (this.list.size != 0) {
                val t = this.list.lastIndex()
                if (t.type == Types().AND) {
                    t.list = t.list!! and right
                } else {
                    val x = LIST()
                    x.type = Types().AND
                    x.list = Multi(right.value).also { it.list.addAll(right.list) }
                    list.add(x)
                }
            } else {
                val x = LIST()
                x.type = Types().AND
                x.list = Multi(right.value).also { it.list.addAll(right.list) }
                list.add(x)
            }
            return this
        }

        infix fun and(right: IsSequenceZeroOrMany): Combination {
            // append list in order
            if (this.list.size != 0) {
                val t = this.list.lastIndex()
                if (t.type == Types().AND) {
                    t.list = t.list!! and right
                } else {
                    val x = LIST()
                    x.type = Types().AND
                    x.list = Multi(right.value).also { it.list.add(Types().also { it.add(right, it.AND) }) }
                    list.add(x)
                }
            } else {
                val x = LIST()
                x.type = Types().AND
                x.list = Multi(right.value).also { it.list.add(Types().also { it.add(right, it.AND) }) }
                list.add(x)
            }
            return this
        }

        infix fun and(right: IsSequenceOneOrMany): Combination {
            // append list in order
            if (this.list.size != 0) {
                val t = this.list.lastIndex()
                if (t.type == Types().AND) {
                    t.list = t.list!! and right
                } else {
                    val x = LIST()
                    x.type = Types().AND
                    x.list = Multi(right.value).also { it.list.add(Types().also { it.add(right, it.AND) }) }
                    list.add(x)
                }
            } else {
                val x = LIST()
                x.type = Types().AND
                x.list = Multi(right.value).also { it.list.add(Types().also { it.add(right, it.AND) }) }
                list.add(x)
            }
            return this
        }

        infix fun and(right: IsSequenceOnce): Combination {
            // append list in order
            if (this.list.size != 0) {
                val t = this.list.lastIndex()
                if (t.type == Types().AND) {
                    t.list = t.list!! and right
                } else {
                    val x = LIST()
                    x.type = Types().AND
                    x.list = Multi(right.value).also { it.list.add(Types().also { it.add(right, it.AND) }) }
                    list.add(x)
                }
            } else {
                val x = LIST()
                x.type = Types().AND
                x.list = Multi(right.value).also { it.list.add(Types().also { it.add(right, it.AND) }) }
                list.add(x)
            }
            return this
        }

        infix fun or(right: GroupCombination): GroupCombination {
            // promote to Group
            val g = Group(this)
            g.depth -= 2
            return g or right
        }

        infix fun or(right: Group): GroupCombination {
            // promote to Group
            val g = Group(this)
            right.depth--
            g.depth -= 2
            return g or right
        }

        infix fun or(right: Combination): Combination {
            // append list in order
            if (list.size != 0) {
                val t = this.list.lastIndex()
                if (t.type == Types().OR) {
                    right.list.forEach {
                        t.list = t.list!! or it.list!!
                    }
                } else {
                    val x = LIST()
                    x.type = Types().OR
                    right.list.forEach {
                        x.list = x.list!! and it.list!!
                    }
                    list.add(x)
                }
            } else {
                val x = LIST()
                x.type = Types().OR
                right.list.forEach {
                    x.list = x.list!! and it.list!!
                }
                list.add(x)
            }
            return this
        }

        infix fun or(right: Multi): Combination {
            // append list in order
            if (this.list.size != 0) {
                val t = this.list.lastIndex()
                if (t.type == Types().OR) {
                    t.list = t.list!! or right
                } else {
                    val x = LIST()
                    x.type = Types().OR
                    x.list = Multi(right.value).also { it.list.addAll(right.list) }
                    list.add(x)
                }
            } else {
                val x = LIST()
                x.type = Types().OR
                x.list = Multi(right.value).also { it.list.addAll(right.list) }
                list.add(x)
            }
            return this
        }

        infix fun or(right: IsSequenceZeroOrMany): Combination {
            // append list in order
            if (this.list.size != 0) {
                val t = this.list.lastIndex()
                if (t.type == Types().OR) {
                    t.list = t.list!! or right
                } else {
                    val x = LIST()
                    x.type = Types().OR
                    x.list = Multi(right.value).also { it.list.add(Types().also { it.add(right, it.OR) }) }
                    list.add(x)
                }
            } else {
                val x = LIST()
                x.type = Types().OR
                x.list = Multi(right.value).also { it.list.add(Types().also { it.add(right, it.OR) }) }
                list.add(x)
            }
            return this
        }

        infix fun or(right: IsSequenceOneOrMany): Combination {
            // append list in order
            if (this.list.size != 0) {
                val t = this.list.lastIndex()
                if (t.type == Types().OR) {
                    t.list = t.list!! or right
                } else {
                    val x = LIST()
                    x.type = Types().OR
                    x.list = Multi(right.value).also { it.list.add(Types().also { it.add(right, it.OR) }) }
                    list.add(x)
                }
            } else {
                val x = LIST()
                x.type = Types().OR
                x.list = Multi(right.value).also { it.list.add(Types().also { it.add(right, it.OR) }) }
                list.add(x)
            }
            return this
        }

        infix fun or(right: IsSequenceOnce): Combination {
            // append list in order
            if (this.list.size != 0) {
                val t = this.list.lastIndex()
                if (t.type == Types().OR) {
                    t.list = t.list!! or right
                } else {
                    val x = LIST()
                    x.type = Types().OR
                    x.list = Multi(right.value).also { it.list.add(Types().also { it.add(right, it.OR) }) }
                    list.add(x)
                }
            } else {
                val x = LIST()
                x.type = Types().OR
                x.list = Multi(right.value).also { it.list.add(Types().also { it.add(right, it.OR) }) }
                list.add(x)
            }
            return this
        }
    }

    fun Group(parsers: GroupCombination): GroupCombination {
        parsers.list.forEach {
            it.list!!.depth += 1
        }
        return parsers
    }

    fun Group(parsers: Group): GroupCombination {
        // promote to GroupCombination
        val x = GroupCombination()
        parsers.depth += 1
        x.add(parsers, parsers.list[0].type!!)
        return x
    }

    fun Group(parsers: Combination): Group {
        val x = Group()
        x.add(1, parsers)
        return x
    }

    fun Group(parsers: Multi): Group {
        val z = Group()
        z.add(1, parsers, Types().AND)
        return z
    }

    fun Group(parsers: IsSequenceZeroOrMany): Group {
        val z = Group()
        z.add(1, Multi(parsers.value).also { it.list.add(Types().also { it.add(parsers, it.AND) }) }, Types().AND)
        return z
    }

    fun Group(parsers: IsSequenceOneOrMany): Group {
        val z = Group()
        z.add(1, Multi(parsers.value).also { it.list.add(Types().also { it.add(parsers, it.AND) }) }, Types().AND)
        return z
    }

    fun Group(parsers: IsSequenceOnce): Group {
        val z = Group()
        z.add(1, Multi(parsers.value).also { it.list.add(Types().also { it.add(parsers, it.AND) }) }, Types().AND)
        return z
    }

    inner class Group() {

        inner class LIST {
            var type: Int? = null
            var list: Combination? = null
        }

        val parent = this@Parser
        var list: MutableList<LIST> = mutableListOf()
        var depth: Int = 0

        fun add(depth: Int, parsers: Combination) {
            this.depth = depth
            val x = LIST()
            x.type = parsers.list[0].type!!
            x.list = parsers
            list.add(x)
        }

        fun add(depth: Int, List: Multi, type: Int) {
            this.depth = depth
            // append list in order
            if (list.size != 0) {
                val t = list.lastIndex()
                if (t.type == Types().AND) {
                    if (type == Types().AND) {
                        t.list = t.list!! and List
                    } else {
                        val x = LIST()
                        x.type = type
                        // promote to combination
                        val y = Combination()
                        y.add(List, Types().AND)
                        x.list = y
                        list.add(x)
                    }
                } else {
                    if (type == Types().AND) {
                        val x = LIST()
                        x.type = type
                        // promote to combination
                        val y = Combination()
                        y.add(List, Types().AND)
                        x.list = y
                        list.add(x)
                    } else {
                        t.list = t.list!! and List
                    }
                }
            } else {
                val x = LIST()
                x.type = type
                // promote to combination
                val y = Combination()
                y.add(List, Types().AND)
                x.list = y
                list.add(x)
            }
        }

        fun printList() = list.forEach {
            println("group ${list.indexOf(it)} (depth $depth): ${Types().typeToString(it.type)}")
            it.list!!.printList()
        }

        infix fun and(right: GroupCombination): GroupCombination {
            // promote to GroupCombination
            val c = GroupCombination()
            this.depth--
            c.add(this, Types().AND)
            return c and right
        }

        infix fun and(right: Group): GroupCombination {
            // promote to GroupCombination
            val c = GroupCombination()
            // append existing group
            this.depth--
            right.depth--
            c.add(this, Types().AND)
            c.add(right, Types().AND)
            return c
        }

        infix fun and(right: Combination): GroupCombination {
            // promote to Group
            val g = Group(right)
            // promote to GroupCombination
            val c = GroupCombination()
            // append existing group
            c.add(this, Types().AND)
            c.add(g, Types().AND)
            return c
        }

        infix fun and(right: Multi): GroupCombination {
            // promote to combination
            val y = Combination()
            y.add(right, Types().AND)
            // promote to Group
            val g = Group(y)
            depth--
            g.depth -= 2
            // promote to GroupCombination
            val c = GroupCombination()
            // append existing group
            c.add(this, Types().AND)
            c.add(g, Types().AND)
            return c
        }

        infix fun and(right: IsSequenceZeroOrMany): GroupCombination {
            // promote to Multi
            val x = parent.clone().Multi(right.value)
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.AND) })
            // promote to combination
            val y = Combination()
            y.add(x, Types().AND)
            // promote to Group
            val g = Group(y)
            depth--
            g.depth -= 2
            // promote to GroupCombination
            val c = GroupCombination()
            // append existing group
            c.add(this, Types().AND)
            c.add(g, Types().AND)
            return c
        }

        infix fun and(right: IsSequenceOneOrMany): GroupCombination {
            // promote to Multi
            val x = parent.clone().Multi(right.value)
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.AND) })
            // promote to combination
            val y = Combination()
            y.add(x, Types().AND)
            // promote to Group
            val g = Group(y)
            depth--
            g.depth -= 2
            // promote to GroupCombination
            val c = GroupCombination()
            // append existing group
            c.add(this, Types().AND)
            c.add(g, Types().AND)
            return c
        }

        infix fun and(right: IsSequenceOnce): GroupCombination {
            // promote to Multi
            val x = parent.clone().Multi(right.value)
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.AND) })
            // promote to combination
            val y = Combination()
            y.add(x, Types().AND)
            // promote to Group
            val g = Group(y)
            depth--
            g.depth -= 2
            // promote to GroupCombination
            val c = GroupCombination()
            // append existing group
            c.add(this, Types().AND)
            c.add(g, Types().AND)
            return c
        }

        infix fun or(right: GroupCombination): GroupCombination {
            // promote to GroupCombination
            val c = GroupCombination()
            c.add(this, Types().OR)
            return c or right
        }

        infix fun or(right: Group): GroupCombination {
            // promote to GroupCombination
            val c = GroupCombination()
            // append existing group
            c.add(this, Types().OR)
            c.add(right, Types().OR)
            return c
        }

        infix fun or(right: Combination): GroupCombination {
            // promote to Group
            val g = Group(right)
            // promote to GroupCombination
            val c = GroupCombination()
            // append existing group
            c.add(this, Types().OR)
            c.add(g, Types().OR)
            return c
        }

        infix fun or(right: Multi): GroupCombination {
            // promote to combination
            val y = Combination()
            y.add(right, Types().OR)
            // promote to Group
            val g = Group(y)
            depth--
            g.depth -= 2
            // promote to GroupCombination
            val c = GroupCombination()
            // append existing group
            c.add(this, Types().OR)
            c.add(g, Types().OR)
            return c
        }

        infix fun or(right: IsSequenceZeroOrMany): GroupCombination {
            // promote to Multi
            val x = parent.clone().Multi(right.value)
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.OR) })
            // promote to combination
            val y = Combination()
            y.add(x, Types().OR)
            // promote to Group
            val g = Group(y)
            depth--
            g.depth -= 2
            // promote to GroupCombination
            val c = GroupCombination()
            // append existing group
            c.add(this, Types().OR)
            c.add(g, Types().OR)
            return c
        }

        infix fun or(right: IsSequenceOneOrMany): GroupCombination {
            // promote to Multi
            val x = parent.clone().Multi(right.value)
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.OR) })
            // promote to combination
            val y = Combination()
            y.add(x, Types().OR)
            // promote to Group
            val g = Group(y)
            depth--
            g.depth -= 2
            // promote to GroupCombination
            val c = GroupCombination()
            // append existing group
            c.add(this, Types().OR)
            c.add(g, Types().OR)
            return c
        }

        infix fun or(right: IsSequenceOnce): GroupCombination {
            // promote to Multi
            val x = parent.clone().Multi(right.value)
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.OR) })
            // promote to combination
            val y = Combination()
            y.add(x, Types().OR)
            // promote to Group
            val g = Group(y)
            depth--
            g.depth -= 2
            // promote to GroupCombination
            val c = GroupCombination()
            // append existing group
            c.add(this, Types().OR)
            c.add(g, Types().OR)
            return c
        }
    }

    inner class GroupCombination() {
        inner class LIST {
            var type: Int? = null
            var list: Group? = null
        }

        val parent = this@Parser
        var list: MutableList<LIST> = mutableListOf()
        fun add(List: Group, type: Int) {
            val x = LIST()
            x.type = type
            x.list = List
            List.depth += 1
            list.add(x)
        }

        fun add(List: GroupCombination, type: Int) = List.list.forEach {
            it.type = type
            list.add(it)
        }

        fun printList() {
            list.forEach {
                println("groupCombination ${list.indexOf(it)}: ${Types().typeToString(it.type)}")
                it.list!!.printList()
            }
        }

        infix fun and(right: GroupCombination): GroupCombination {
            val g = GroupCombination()
            // append existing groupCombination
            g.add(this, Types().AND)
            g.add(right, Types().AND)
            return g
        }

        infix fun and(right: Group): GroupCombination {
            // promote to GroupCombination
            val c = GroupCombination()
            // append existing groupCombination
            c.add(this, Types().AND)
            c.add(right, Types().AND)
            return c
        }

        infix fun and(right: Combination): GroupCombination {
            // promote to Group
            val g = Group(right)
            // promote to GroupCombination
            val c = GroupCombination()
            // append existing groupCombination
            c.add(this, Types().AND)
            c.add(g, Types().AND)
            return c
        }

        infix fun and(right: Multi): GroupCombination {
            // promote to combination
            val y = Combination()
            y.add(right, Types().AND)
            // promote to Group
            val g = Group(y)
            g.depth -= 2
            // promote to GroupCombination
            val c = GroupCombination()
            // append existing groupCombination
            c.add(this, Types().AND)
            c.add(g, Types().AND)
            return c
        }

        infix fun and(right: IsSequenceZeroOrMany): GroupCombination {
            // promote to Multi
            val x = parent.clone().Multi(right.value)
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.AND) })
            // promote to combination
            val y = Combination()
            y.add(x, Types().AND)
            // promote to Group
            val g = Group(y)
            g.depth -= 2
            // promote to GroupCombination
            val c = GroupCombination()
            // append existing groupCombination
            c.add(this, Types().AND)
            c.add(g, Types().AND)
            return c
        }

        infix fun and(right: IsSequenceOneOrMany): GroupCombination {
            // promote to Multi
            val x = parent.clone().Multi(right.value)
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.AND) })
            // promote to combination
            val y = Combination()
            y.add(x, Types().AND)
            // promote to Group
            val g = Group(y)
            g.depth -= 2
            // promote to GroupCombination
            val c = GroupCombination()
            // append existing groupCombination
            c.add(this, Types().AND)
            c.add(g, Types().AND)
            return c
        }

        infix fun and(right: IsSequenceOnce): GroupCombination {
            // promote to Multi
            val x = parent.clone().Multi(right.value)
            x.list.add(Types().also { it.add(right, it.AND) })
            // promote to combination
            val y = Combination()
            y.add(x, Types().AND)
            // promote to Group
            val g = Group(y)
            g.depth -= 2
            val c = GroupCombination()
            // append existing groupCombination
            c.add(this, Types().AND)
            // promote to GroupCombination
            c.add(g, Types().AND)
            return c
        }

        infix fun or(right: GroupCombination): GroupCombination {
            val c = GroupCombination()
            // append existing groupCombination
            c.add(this, Types().OR)
            c.add(right, Types().OR)
            return c
        }

        infix fun or(right: Group): GroupCombination {
            // promote to GroupCombination
            val c = GroupCombination()
            // append existing groupCombination
            c.add(this, Types().OR)
            c.add(right, Types().OR)
            return c
        }

        infix fun or(right: Combination): GroupCombination {
            // promote to Group
            val g = Group(right)
            // promote to GroupCombination
            val c = GroupCombination()
            // append existing groupCombination
            c.add(this, Types().OR)
            c.add(g, Types().OR)
            return c
        }

        infix fun or(right: Multi): GroupCombination {
            // promote to combination
            val y = Combination()
            y.add(right, Types().OR)
            // promote to Group
            val g = Group(y)
            g.depth -= 2
            // promote to GroupCombination
            val c = GroupCombination()
            // append existing groupCombination
            c.add(this, Types().OR)
            c.add(g, Types().OR)
            return c
        }

        infix fun or(right: IsSequenceZeroOrMany): GroupCombination {
            // promote to Multi
            val x = parent.clone().Multi(right.value)
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.OR) })
            // promote to combination
            val y = Combination()
            y.add(x, Types().OR)
            // promote to Group
            val g = Group(y)
            g.depth -= 2
            // promote to GroupCombination
            val c = GroupCombination()
            // append existing groupCombination
            c.add(this, Types().OR)
            c.add(g, Types().OR)
            return c
        }

        infix fun or(right: IsSequenceOneOrMany): GroupCombination {
            // promote to Multi
            val x = parent.clone().Multi(right.value)
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.OR) })
            // promote to combination
            val y = Combination()
            y.add(x, Types().OR)
            // promote to Group
            val g = Group(y)
            g.depth -= 2
            // promote to GroupCombination
            val c = GroupCombination()
            // append existing groupCombination
            c.add(this, Types().OR)
            c.add(g, Types().OR)
            return c
        }

        infix fun or(right: IsSequenceOnce): GroupCombination {
            // promote to Multi
            val x = parent.clone().Multi(right.value)
            if (right.list.size != 0) x.list.addAll(right.list) else x.list.add(Types().also { it.add(right, it.OR) })
            // promote to combination
            val y = Combination()
            y.add(x, Types().OR)
            // promote to Group
            val g = Group(y)
            g.depth -= 2
            // promote to GroupCombination
            val c = GroupCombination()
            // append existing groupCombination
            c.add(this, Types().OR)
            c.add(g, Types().OR)
            return c
        }
    }
}
