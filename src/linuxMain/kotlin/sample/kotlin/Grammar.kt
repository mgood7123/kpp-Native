package sample.kotlin

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
}