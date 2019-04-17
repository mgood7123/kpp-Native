package sample

import preprocessor.core.macroList
import preprocessor.test.init
import preprocessor.utils.core.abort
import preprocessor.utils.extra.parse

@Suppress("unused")
class REPL {
    val ENV = init()
        .also {
            parse("#define VERSION ${preprocessor.base.globalVariables.version}", it)
        }
        .also {
            parse("#define PS1 $", it)
        }

    fun promt() {
        print(parse("PS1", ENV) + " ")
    }

    fun promt(PS1: String) {
        print("$PS1 ")
    }

    fun REPL() {
        // TODO make a proper REPL
        println(parse("Kotlin Pre Processor Version VERSION REPL BETA", ENV))
        promt()
        var line = readLine()
        while (line != null) {
            println(parse(line, m, newlineFunction = {
                promt(it)
                val x = readLine()
                if (x != null) x
                else abort("failed to grab a new line")
            }))
            promt()
            line = readLine()
        }
    }
}