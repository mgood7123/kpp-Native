package sample

import preprocessor.test.Tests
import preprocessor.test.init
import preprocessor.utils.extra.parse

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
    println("-t, --test                       test the macro preprocessor using its internal testing suite")
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
    var line = readLine()
    while (line != null) {
        println(parse(line, m))
        line = readLine()
    }
}

val version = 1.0

fun printVersion() {
    println("Kotlin Pre Processor Version $version")
    println("https://github.com/mgood7123/kpp-Native")
    println("Developer: Matthew James Good")
    println("with huge help by: https://cpplang.slack.com/team/UAG0Z05BQ - chill")
}

fun REPL() {
    // TODO make a proper REPL
    println("Kotlin Pre Processor Version $version")
    var line = readLine()
    while (line != null) {
        println(parse(line, m))
        line = readLine()
    }
}

fun main(a: Array<String>) {
    if (a.isEmpty()) processSTDIN()
    else a.forEach {
        when(it) {
            "",
            "-h", "--help" -> printHelp()
            "-u", "--usage" -> printUsage()
            "-",  "--stdin" -> processSTDIN()
            "-v", "--version" -> printVersion()
            "-d", "--debug", "--debugon" -> {
                preprocessor.base.globalVariables.flags.debug = true
                if (a.size == 1) processSTDIN()
            }
            "-dd", "--nodebug", "--debugoff" -> {
                preprocessor.base.globalVariables.flags.debug = false
                if (a.size == 1) processSTDIN()
            }
            "-t", "--test" -> Tests().doAll()
            else -> println(parse(it, m))
        }
    }
}