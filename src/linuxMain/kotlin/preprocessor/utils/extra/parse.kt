package preprocessor.utils.extra

import preprocessor.base.globalVariables
import preprocessor.core.Macro
import preprocessor.core.Lexer
import preprocessor.core.Parser
import preprocessor.core.parserPrep
import preprocessor.utils.core.algorithms.toByteArray

/**
 * parses a line
 * @param lex the current [Lexer]
 * @param macro the [Macro] list
 */
fun parse(lex: Lexer, macro: MutableList<Macro>): String? {
    preprocessor.base.globalVariables.depth = 0
    return expand(
        lex = lex,
        tokenSequence = Parser(tokens = parserPrep(lex.currentLine as String)),
        macro = macro
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
): String? {
    val lex = Lexer(
        string.toByteArray(),
        globalVariables.tokensNewLine
    )
    lex.lex()
    if (lex.currentLine == null) return null
    val str = StringBuilder()
    while (lex.currentLine != null) {
        val p = parse(lex, macro)
        if (p == null) return null
        str.append(p)
        lex.lex()
    }
    return str.toString()
}