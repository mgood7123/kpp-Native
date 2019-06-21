package sample

import preprocessor.utils.core.abort

@Suppress("unused")
class Debugger {
    fun breakPoint() {
        val stack = Exception().getStackTrace().iterator()
        println("stack trace:")
        while (stack.hasNext()) {
            val e = stack.next()
            val o = e.substringAfter('(').substringBefore(')')
            val a: List<String>? = if (o.contains(';') || o.isNotBlank()) o.split(';') else null
            val m = e.substringBefore('(')
            if (m.isBlank()) break
            if (!m.contains(':'))  break
            val f = m.substringAfter(':')
            if (f.isBlank()) break
            if (f == "kotlin.Exception.<init>") {
                stack.next()
                continue
            }
            println("next stack:")
            println("    function: $f")
            print("    function argument count: ")
            if (a == null) println("0")
            else {
                println(a.size)
                if (a.isNotEmpty()) {
                    var paramNumber = 0
                    a.forEach {
                        println("        parameter $paramNumber is of type: $it")
                        paramNumber++
                    }
                }
            }
            println("    entire function: $e")
        }
    }
}
