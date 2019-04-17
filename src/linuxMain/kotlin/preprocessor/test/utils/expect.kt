package preprocessor.test.utils

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
            println("expect(\"$input\", \"$output\", <macro>) passed")
            true
        }
        else -> {
            println("expect(\"$input\", \"$output\", <macro>) failed")
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
