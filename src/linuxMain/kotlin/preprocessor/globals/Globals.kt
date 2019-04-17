package preprocessor.globals

import preprocessor.core.Macro
import preprocessor.utils.core.basename
//import java.io.File

/**
 * the globals class contains all global variables used by this library
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class Globals {

    /**
     * prints debug output if this value is true
     */
    var debug: Boolean = false
//    /**
//     * the current project directory that this task has been called from
//     * @see projectDirectoryBaseName
//     * @see rootDirectory
//     */
//    var projectDirectory: File? = null
//    /**
//     * the basename of [projectDirectory]
//     * @see rootDirectoryBaseName
//     */
//    var projectDirectoryBaseName: String? = null
//    /**
//     * the root project directory
//     * @see rootDirectoryBaseName
//     * @see projectDirectory
//     */
//    var rootDirectory: File? = null
//    /**
//     * the basename of [rootDirectory]
//     * @see projectDirectoryBaseName
//     */
//    var rootDirectoryBaseName: String? = null
//
//    /**
//     * the Default [macro][Macro] list
//     */
//    var kppMacroList: MutableList<Macro> = mutableListOf(Macro())
//
//    /**
//     * the directory that **kpp** is contained in
//     */
//    var kppDir: String? = null
//    /**
//     * the directory that **kpp** is contained in
//     */
//    var kppDirAsFile: File? = null
//    /**
//     * the suffix to give files that have been processed by kpp
//     */
//    var preprocessedExtension: String = ".preprocessed"
//
//    /**
//     * initializes the global variables
//     *
//     * it is recommended to call this function as `Globals().initGlobals(rootDir, projectDir)`
//     *
//     * replace `Globals()` with your instance of the `Globals` class
//     * @sample globalsSample
//     */
//    fun initGlobals(rootDir: File, projectDir: File) {
//        projectDirectory = projectDir
//        projectDirectoryBaseName = basename(projectDirectory)
//        rootDirectory = rootDir
//        rootDirectoryBaseName = basename(rootDirectory)
//        kppDir = rootDirectory.toString() + "/kpp"
//        kppDirAsFile = File(kppDir)
//    }
//
//    /**
//     * initializes the global variables
//     *
//     * it is recommended to call this function as `Globals().initGlobals(rootDir, projectDir)`
//     *
//     * replace `Globals()` with your instance of the `Globals` class
//     * @sample globalsSample
//     */
//    fun initGlobals(rootDir: String, projectDir: String) {
//        initGlobals(File(rootDir), File(projectDir))
//    }

    /**
     * this is used by [testFile][preprocessor.utils.Sync.testFile]
     */
    var currentFileContainsPreprocessor: Boolean = false
    /**
     *
     *//*
    TODO: implement file cache
    var currentFileIsCashed: Boolean = false
    var cachedFileContainsPreprocessor: Boolean = false
     */
    var firstLine: Boolean = true
    /**
     *
     */
    var currentMacroExists: Boolean = false
    /**
     *
     */
    var abortOnComplete: Boolean = true

    /**
     * `<space> or <tab>`
     * @see tokens
     */
    val tokensSpace: String = " \t"
    val tokensNewLine: String = "\n"
    val tokensExtra: String = "\\\"'/*${Macro().Directives().value}().,->{}[]"
    val tokensMath: String = "+-*/"
    val tokens: String = tokensSpace + tokensNewLine + tokensExtra + tokensMath

    /**
     * the current depth
     */
    var depth: Int = 0
    /**
     * returns a depth string
     */
    fun depthAsString(): String = "    ".repeat(depth) + "depth:$depth > "
}

//private fun globalsSample(rootDir: File, projectDir: File) {
//    val globals = Globals()
//    globals.initGlobals(rootDir, projectDir)
//    //rootDir is usually provided within the task itself
//    //projectDir is usually provided within the task itself
//}