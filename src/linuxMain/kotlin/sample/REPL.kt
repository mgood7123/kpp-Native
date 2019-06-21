package sample

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
            parse("#define VERSION ${preprocessor.base.globalVariables.version}", it)
        }
        .also {
            parse("#define PS1 $", it)
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
        print("$PS1 ")
    }

    fun REPL() {
        // TODO make a proper REPL
        println(parse("Kotlin Pre Processor Version VERSION REPL BETA", env))
        promt()
        var line = readLine()
        while (line != null) {
            if (debug) println("line = $line")
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
}