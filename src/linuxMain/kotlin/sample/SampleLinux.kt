package sample

import Hierarchy
import preprocessor.test.Tests
import preprocessor.test.init
import preprocessor.utils.core.abort
import preprocessor.utils.extra.parse
import sample.kotlin.Kotlin

// https://medium.com/@elye.project/mastering-kotlin-standard-functions-run-with-let-also-and-apply-9cd334b0ef84

fun printUsage() {
    println("Usage:")
    println("cat FILE | thisProgram")
    println("echo hai | thisProgram")
    println("echo \"hai\" | thisProgram")
    println("bash\$ thisProgram < FILE")
    println("bash\$ thisProgram <<EOF\nlines\nEOF")
    println("bash\$ thisProgram <<<\"lines\nof\ntext\"")
    println("thisProgram line1 line2 ...")
    println("thisProgram \"line 1\" \"line 2\" ...")
    println("thisProgram hai --test hai --debug hai --test hai ...")
    println("and so on for every way you can send input to thisProgram")
}

fun printHelp() {
    println("Help:")
    println("Flags that affect how arguments are processed")
    println("-h,  --help                      print help")
    println("-u,  --usage                     print usage")
    println("-v,  --version                   print version information")
    println("-r,  --repl, -R, --REPL          start a REPL session")
    println("-t, --test                       test the macro preprocessor using its internal testing suite")
    println("-k,  --kotlin                    print usage")
    println("-, --stdin                       read input from stdin\n" +
            "                                 default if no 'Flags that affect how arguments are processed' are given)"
    )
    println()
    println("Flags that do not affect how arguments are processed")
    println("-d,  --debug,   --debugon        enable debugging")
    println("-dd, --nodebug, --debugoff       disable debugging")
}

val m = init()

fun processSTDIN() {
    var line: String? = readLine()
    while (line != null) {
        val r = parse(line, m, newlineFunction = { readLine() }) ?: return
        println(r)
        line = readLine()
    }
}



fun printVersion() {
    println("Kotlin Pre Processor Version ${preprocessor.base.globalVariables.version}")
    println("https://github.com/mgood7123/kpp-Native")
    println("Developer: Matthew James Good")
    println("with huge help by: https://cpplang.slack.com/team/UAG0Z05BQ - chill")
}


fun main(a: Array<String>) {
//    Hierarchy().listFiles()
    if (a.isEmpty()) processSTDIN()
    else a.forEach {
        when(it) {
            "",
            "-h", "--help" -> printHelp()
            "-u", "--usage" -> printUsage()
            "-",  "--stdin" -> processSTDIN()
            "-v", "--version" -> printVersion()
            "-r", "--repl", "-R", "--REPL" -> REPL().REPL()
            "-t", "--test" -> Tests().doAll()
            "-k", "--kotlin" -> Kotlin("ABCDEF").Lexer()
            // flags
            "-d", "--debug", "--debugon" -> {
                preprocessor.base.globalVariables.flags.debug = true
                if (a.size == 1) processSTDIN()
            }
            "-dd", "--nodebug", "--debugoff" -> {
                preprocessor.base.globalVariables.flags.debug = false
                if (a.size == 1) processSTDIN()
            }
            else -> println(parse(it, m))
        }
    }
}
