import org.gradle.kotlin.dsl.support.zipTo
import java.io.File

val hierarchy = file("$projectDir/src/linuxMain/kotlin/sample/Hierarchy.kt")
val dir = file("$projectDir/src")
var hierarchyFirstWrite = true
val indentation = 4

tasks.register("pack") {
    hierarchy.createNewFile()
    write(
"""
class Hierarchy {
    private val indentation = 4

    private fun indent(depth: Int) = " ".repeat(indentation).repeat(depth)
    fun printHierarchy() = printHierarchy(0, rootFileSystem)
    private fun printHierarchy(depth: Int = 0, rootFileSystem: List<Any>) {
        var indexI = 0
        for (any in rootFileSystem) {
            when(any) {
                is List<*> -> printHierarchy(depth+1, any as List<Any>)
                else ->  when(indexI) {
                    0 -> println("${'$'}{indent(if (depth == 0) 0 else depth)}directory = ${'$'}any").also {indexI++}
                    1 -> println("${'$'}{indent(depth + 1)}file: ${'$'}any").also {indexI++}
                    2 -> println("${'$'}{indent(depth + 1)}full file path: ${'$'}any").also {indexI++}
                    3 -> {
                        println("${'$'}{indent(depth + 1)}content: ${'$'}any")
                        indexI = 1
                    }
                }
            }
        }
    }
    fun listFiles() = listFiles(rootFileSystem)
    private fun listFiles(rootFileSystem: List<Any>) {
        var indexI = 0
        for (any in rootFileSystem) {
            when(any) {
                is List<*> -> listFiles(any as List<Any>)
                else ->  when(indexI) {
                    0 -> println("directory = ${'$'}any").also {indexI++}
                    1 -> println("file: ${'$'}any").also {indexI++}
                    2 -> println("full file path: ${'$'}any").also {indexI++}
                    3 -> {
                       indexI = 1 // skip contents
                    }
                }
            }
        }
    }
    fun find(file: String) = find(file, rootFileSystem)
    private fun find(file: String, rootFileSystem: List<Any>) {
        var indexI = 0
        for (any in rootFileSystem) {
            when(any) {
                is List<*> -> find(file, any as List<Any>)
                else ->  when(indexI) {
                    0 -> println("directory = ${'$'}any").also {indexI++}
                    1 -> println("file: ${'$'}any").also {indexI++}
                    2 -> println("full file path: ${'$'}any").also {indexI++}
                    3 -> {
                       indexI = 1 // skip contents
                    }
                }
            }
        }
    }

    private val rootFileSystem = listOf(
        "Root"""")
    recDir(dir, 2)
    writeln("""
    )
}
""")
}

fun recDir(current: File, depth: Int) {
    var first = false
    for (currentFile in current.listFiles()) {
        val name = currentFile.name
        if (currentFile.isDirectory) {
            if (!empty(currentFile)) {
                println("found $currentFile/ of type directory")
                write("${if (!first) ",\n" else ""}${indent(depth)}listOf(\n${indent(depth+1)}\"$name\"")
                recDir(currentFile, depth + 1)
                write("\n${indent(depth)})")
            }
        }
        if (currentFile.isFile) {
            println("found $currentFile of type file")
            write(
                "${if (!first) ",\n" else ""}${indent(depth)}\"$name\", \"${currentFile.toString().removePrefix(dir.toString() + "/")}\", \"\"\"${currentFile.readText()
                    .replace("\$", "\${'$'}")
                    .replace("\"\"\"", "\\\"\\\"\\\"")
                }\"\"\""
            )
            if (first) first = false
        }
    }
}

fun write(line: String) {
    when {
        hierarchyFirstWrite -> {
            hierarchy.writeText(line)
            hierarchyFirstWrite = false
        }
        else -> hierarchy.appendText(line)
    }
}

fun writeln(line: String) {
    write(line + "\n")
}

fun indent(depth: Int) = " ".repeat(indentation).repeat(depth)

fun empty(src: File): Boolean {
    val files: Array<File>? = src.listFiles()
    if (files == null) return true
    if (files.isEmpty()) return true
    val e: MutableList<Boolean> = mutableListOf()
    files.forEach {
        if (it.isDirectory) e.add(empty(it))
        else if (it.isFile) e.add(false)
        e.add(true)
    }
    return e.all { it == true }
}