package preprocessor.core

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
// * the result is saved in "$src${globalVariables.preprocessedExtension}$extensions"
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
//    val destinationPreProcessed = File("$src${globalVariables.preprocessedExtension}.$extension")
//    var index = macro.size - 1
//    if (macro[index].fileName != null) {
//        index++
//        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "reallocating to $index")
//        realloc(macro, index + 1)
//    }
//    macro[index].fileName = src.substringAfterLast('/')
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "registered macro definition for ${macro[index].fileName} at index $index")
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "processing ${macro[index].fileName} -> ${destinationPreProcessed.name}")
//    destinationPreProcessed.createNewFile()
//    val lex = Lexer(fileToByteArray(File(src)), globalVariables.tokensNewLine)
//    lex.lex()
//    if (preprocessor.base.globalVariables.flags.debug) println(
//        preprocessor.base.globalVariables.depthAsString() + "lex.currentLine is ${lex.currentLine
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
//            preprocessor.base.globalVariables.depthAsString() + "\ninput = $input"
//                .replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())
//        )
//        if (preprocessor.base.globalVariables.flags.debug) println(
//            preprocessor.base.globalVariables.depthAsString() + "output = $out\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
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
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "saving macro in to index $index")
    var macroIndex = macro[index].macros.size - 1
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "saving macro in to macro index $macroIndex")
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
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "type       = ${macro[index].macros[macroIndex].type}")
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "token      = ${macro[index].macros[macroIndex].identifier}")
    if (macro[index].macros[macroIndex].arguments != null)
        if (preprocessor.base.globalVariables.flags.debug) println(
            preprocessor.base.globalVariables.depthAsString() +
                    "arguments  = ${macro[index].macros[macroIndex].arguments}"
        )
    if (preprocessor.base.globalVariables.flags.debug) println(
        preprocessor.base.globalVariables.depthAsString() +
                "replacementList      = ${macro[index].macros[macroIndex].replacementList
                    ?.replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())
                }"
    )
    macroList(index, macro)
    // definition names do not expand
    // definition values do expand
}
