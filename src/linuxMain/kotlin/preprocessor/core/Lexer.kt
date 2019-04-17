package preprocessor.core

import preprocessor.utils.core.algorithms.Stack
import preprocessor.utils.core.algorithms.toStack

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
