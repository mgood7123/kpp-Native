@file:Suppress("unused")

package preprocessor.core

import preprocessor.base.globalVariables
import preprocessor.utils.core.abort
import preprocessor.utils.core.realloc

/**
 *
 * This class is used to house all macro definitions
 *
 * @sample Tests.generalUsage
 *
 * @see Directives
 * @see Directives.Define
 *
 */
class Macro {
    /**
     * this class is used to obtain predefined values such as directive names and types
     *
     * all preprocessor directives start with a [#][Macro.Directives.value]
     *
     * this CAN be changed if desired to run multiple variants of this preprocessor on the same file
     * @see Define
     */
    @Suppress("MemberVisibilityCanBePrivate")
    inner class Directives {
        /**
         * the preprocessor [directive][Directives] identification token
         *
         * this defaults to **#**
         */
        var value: String = "#"

        /**
         * contains predefined [values][Define.value] and [types][Define.Types] for the
         * [#][Macro.Directives.value][define][Define.value] directive
         * @see Directives
         */
        inner class Define {
            /**
             * the ***[#][Macro.Directives.value]define*** directive associates an identifier to a replacement list
             * the Default value of this is **define**
             * @see Types
             */
            val value: String = "define"

            /**
             * the valid types of a definition directive macro
             * @see Object
             * @see Function
             */
            inner class Types {
                /**
                 * the Object type denotes the standard macro definition, in which all text is matched with
                 *
                 * making **Object** lowercase conflicts with the top level declaration **object**
                 *
                 * @see Types
                 * @see Function
                 * @sample objectSample
                 * @sample objectUsage
                 */
                @Suppress("PropertyName")
                val Object: String = "object"
                /**
                 * the Function type denotes the Function macro definition, in which all text that is followed by
                 * parentheses is matched with
                 *
                 * making **Function** lowercase must mean [Object] must also be lowercase
                 * to maintain naming pairs (as **Object, function**, and **object, Function**
                 * just looks weird)
                 *
                 * unfortunately this is impossible as it would conflict with the top level
                 * declaration **object**
                 *
                 * @see Types
                 * @see Object
                 * @sample functionSample
                 * @sample functionUsage
                 */
                @Suppress("PropertyName")
                val Function: String = "function"

                private fun objectSample() {
                    /* ignore this block comment

                    #define object value
                    object
                    object(object).object[object]

                    ignore this block comment */
                }

                private fun objectUsage() {
                    val c = mutableListOf(Macro())
                    c[0].fileName = "test"
                    c[0].macros[0].fullMacro = "A B00"
                    c[0].macros[0].identifier = "A"
                    c[0].macros[0].replacementList = "B00"
                    c[0].macros[0].type =
                        Macro().Directives().Define().Types().Object
                    if (preprocessor.base.globalVariables.debug) println(c[0].macros[0].type)
                }

                private fun functionSample() {
                    /* ignore this block comment

                    #define object value
                    #define function value
                    object
                    function(object).object[function(function()]

                    ignore this block comment */
                }

                private fun functionUsage() {
                    val c = mutableListOf(Macro())
                    c[0].fileName = "test"
                    c[0].macros[0].fullMacro = "A() B00"
                    c[0].macros[0].identifier = "A"
                    c[0].macros[0].replacementList = "B00"
                    c[0].macros[0].type =
                        Macro().Directives().Define().Types().Function
                    if (preprocessor.base.globalVariables.debug) println(c[0].macros[0].type)
                }
            }
        }
    }

    /**
     * the internals of the Macro class
     *
     * this is where all macros are kept, this is managed via [realloc]
     * @sample Tests.generalUsage
     */
    inner class MacroInternal {
        /**
         * the current size of the [macro][MacroInternal] list
         *
         * can be used to obtain the last added macro
         *
         * @sample Tests.sizem
         */
        var size: Int = 0
        /**
         * the full macro definition
         *
         * contains the full line at which the definition appears
         */
        var fullMacro: String? = null
        /**
         * contains the definition **identifier**
         */
        var identifier: String? = null
        /**
         * contains the definition **type**
         */
        var type: String? = null
        /**
         * contains the definition **arguments**,
         * valid only for
         * [Function][Macro.Directives.Define.Types.Function]
         * type definitions
         *
         * this defaults to **null**
         *
         */
        var arguments: MutableList<String>? = null
        /**
         * this contains the definition replacement list
         */
        var replacementList: String? = null
    }

    /**
     * the current size of the [macro][Macro] list
     *
     * can be used to obtain the last added [macro group][MacroInternal]
     *
     * @sample Tests.size
     */
    var size: Int = 0
    /**
     * the name of the file containing this [macro][Macro] list
     */
    var fileName: String? = null
    /**
     * the [macro][MacroInternal] list
     */
    var macros: MutableList<MacroInternal>

    init {
        this.size = 1
        macros = mutableListOf(MacroInternal())
    }

    private class Tests {
        fun generalUsage() {
            val c = mutableListOf(Macro())
            c[0].fileName = "test"
            c[0].macros[0].fullMacro = "A B"
            c[0].macros[0].identifier = "A"
            c[0].macros[0].replacementList = "B00"
            if (preprocessor.base.globalVariables.debug) println(c[0].macros[0].replacementList)
            realloc(c, c.size + 1)
            c[1].fileName = "test"
            c[1].macros[0].fullMacro = "A B"
            c[1].macros[0].identifier = "A"
            c[1].macros[0].replacementList = "B10"
            if (preprocessor.base.globalVariables.debug) println(c[1].macros[0].replacementList)
            realloc(c[1].macros, c[1].macros.size + 1)
            c[1].fileName = "test"
            c[1].macros[1].fullMacro = "A B"
            c[1].macros[1].identifier = "A"
            c[1].macros[1].replacementList = "B11"
            if (preprocessor.base.globalVariables.debug) println(c[1].macros[1].replacementList)
            realloc(c[1].macros, c[1].macros.size + 1)
            c[1].fileName = "test"
            c[1].macros[2].fullMacro = "A B"
            c[1].macros[2].identifier = "A"
            c[1].macros[2].replacementList = "B12"
            if (preprocessor.base.globalVariables.debug) println(c[1].macros[2].replacementList)
        }

        fun reallocUsage() {
            val c = mutableListOf(Macro())
            // allocate a new index
            realloc(c, c.size + 1)
            // assign some values
            c[0].fileName = "test"
            c[1].fileName = "test"
        }

        fun reallocUsageInternal() {
            val c = mutableListOf(Macro())
            // allocate a new index
            realloc(c[0].macros, c[0].macros.size + 1)
            // assign some values
            c[0].macros[0].fullMacro = "A A"
            c[0].macros[1].fullMacro = "A B"
        }

        fun size() {
            val c = mutableListOf(Macro())
            // allocate a new macro
            realloc(c[0].macros, c[0].macros.size + 1)
            c[0].macros[1].fullMacro = "A B"
            if (preprocessor.base.globalVariables.debug) println(c[0].macros[1].replacementList)
            // obtain base index
            val index = c.size - 1
            // obtain last macro index
            val macroIndex = c[0].macros.size - 1
            if (c[index].macros[macroIndex].fullMacro.equals(c[0].macros[1].fullMacro))
                if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() + "index matches")
        }

        fun sizem() {
            val c = mutableListOf(Macro())
            c[0].fileName = "test1"
            realloc(c, c.size + 1)
            c[1].fileName = "test2"
            val index = c.size - 1
            if (c[index].fileName.equals(c[1].fileName))
                if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() + "index matches")
        }
    }
}

/**
 * checks if the desired macro exists in the [Macro] list
 */
fun macroExists(token: String, type: String, index: Int, macro: MutableList<Macro>): Int {
    globalVariables.currentMacroExists = false
    // if empty return 0 and do not set globalVariables.currentMacroExists
    if (macro[index].macros[0].fullMacro == null) return 0
    var i = 0
    while (i <= macro[index].macros.lastIndex) {
        if (macro[index].macros[i].identifier.equals(token) && macro[index].macros[i].type.equals(type)) {
            // Line is longer than allowed by code style (> 120 columns)
            if (preprocessor.base.globalVariables.debug) println(
                preprocessor.base.globalVariables.depthAsString() +
                        "token and type matches existing definition ${macro[index].macros[i].identifier} type " +
                        "${macro[index].macros[i].type}"
            )
            globalVariables.currentMacroExists = true
            if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() + "returning $i")
            return i
        }
        // Line is longer than allowed by code style (> 120 columns)
        else if (preprocessor.base.globalVariables.debug) println(
            preprocessor.base.globalVariables.depthAsString() +
                    "token $token or type $type does not match current definition token " +
                    "${macro[index].macros[i].identifier} type ${macro[index].macros[i].type}"
        )
        i++
    }
    return i
}

/**
 * lists the current macros in a [Macro] list
 */
fun macroList(index: Int = 0, macro: MutableList<Macro>) {
    if (macro[index].macros[0].fullMacro == null) return
    if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() + "LISTING macros")
    var i = 0
    while (i <= macro[index].macros.lastIndex) {
        // Line is longer than allowed by code style (> 120 columns)
        if (preprocessor.base.globalVariables.debug) println(
            preprocessor.base.globalVariables.depthAsString() +
                    "[$i].fullMacro       = ${macro[index].macros[i].fullMacro}"
        )
        if (preprocessor.base.globalVariables.debug) println(
            preprocessor.base.globalVariables.depthAsString() +
                    "[$i].type            = ${macro[index].macros[i].type}"
        )
        if (preprocessor.base.globalVariables.debug) println(
            preprocessor.base.globalVariables.depthAsString() +
                    "[$i].identifier      = " +
                    "${macro[index].macros[i].identifier}"
        )
        if (macro[index].macros[i].arguments != null)
        // Line is longer than allowed by code style (> 120 columns)
            if (preprocessor.base.globalVariables.debug) println(
                preprocessor.base.globalVariables.depthAsString() +
                        "[$i].arguments       = ${macro[index].macros[i].arguments}"
            )
        // Line is longer than allowed by code style (> 120 columns)
        if (preprocessor.base.globalVariables.debug) println(
            preprocessor.base.globalVariables.depthAsString() +
                    "[$i].replacementList = ${macro[index].macros[i].replacementList
                        ?.replace("\n", "\n" + preprocessor.base.globalVariables.depthAsString())}"
        )
        i++
    }
    if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() + "LISTED macros")
}

/**
 * lists the current macros in a [Macro] list
 *
 * this version lists ALL [Macro]s in the current [Macro] list in all available file index's
 */
fun macroList(macro: MutableList<Macro>) {
    if (macro.size == 0) {
        if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() + "macro list is empty")
        return
    }
    var i = 0
    while (i < macro.size) {
        if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() + "LISTING macros for file ${macro[i].fileName}")
        macroList(i, macro)
        if (preprocessor.base.globalVariables.debug) println(preprocessor.base.globalVariables.depthAsString() + "LISTED macros for file ${macro[i].fileName}")
        i++
    }
}

/**
 * converts a [List] and a [List] into a [Macro] array
 */
fun toMacro(definition: List<String>?, replacementList: List<String>?): MutableList<Macro>? {
    if (definition == null) return null
    if (replacementList == null) return toMacro(definition)
    // Line is longer than allowed by code style (> 120 columns)
    if (preprocessor.base.globalVariables.debug) println(
        preprocessor.base.globalVariables.depthAsString() +
                "${definition.size} == ${replacementList.size} is " +
                "${definition.size == replacementList.size}"
    )
    if (definition.size != replacementList.size) {
        // Line is longer than allowed by code style (> 120 columns)
        abort(
            preprocessor.base.globalVariables.depthAsString() +
                    "size mismatch: expected ${definition.size}, got ${replacementList.size}"
        )
    }
    val associatedArguments = mutableListOf(Macro())
    var i = 0
    associatedArguments[0].macros[i].fullMacro =
        "${Macro().Directives().Define().value} ${definition[i]} ${replacementList[i]}"
    associatedArguments[0].macros[i].type = Macro().Directives().Define().Types().Object
    associatedArguments[0].macros[i].identifier = definition[i]
    associatedArguments[0].macros[i].replacementList = replacementList[i]
    i++
    while (i <= definition.lastIndex) {
        // Line is longer than allowed by code style (> 120 columns)
        realloc(
            associatedArguments[0].macros,
            associatedArguments[0].macros.size + 1
        )
        associatedArguments[0].macros[i].fullMacro =
            "${Macro().Directives().Define().value} ${definition[i]} ${replacementList[i]}"
        associatedArguments[0].macros[i].type = Macro().Directives().Define().Types().Object
        associatedArguments[0].macros[i].identifier = definition[i]
        associatedArguments[0].macros[i].replacementList = replacementList[i]
        i++
    }
    macroList(macro = associatedArguments)
    return associatedArguments
}

/**
 * converts an [MutableList] and a [List] into a [Macro] array
 */
fun toMacro(definition: MutableList<String>?, replacementList: List<String>?): MutableList<Macro>? =
    toMacro(definition?.toList(), replacementList)

/**
 * converts an [List] and a [MutableList] into a [Macro] array
 */
fun toMacro(definition: List<String>?, replacementList: MutableList<String>?): MutableList<Macro>? =
    toMacro(definition, replacementList?.toList())

/**
 * converts an [MutableList] and a [MutableList] into a [Macro] array
 */
fun toMacro(definition: MutableList<String>?, replacementList: MutableList<String>?): MutableList<Macro>? =
    toMacro(definition?.toList(), replacementList?.toList())

/**
 * converts an [MutableList] into a [Macro] array, value is null
 */
fun toMacro(definition: MutableList<String>?): MutableList<Macro>? =
    toMacro(definition?.toList())

/**
 * converts an [List] into a [Macro] array, value is null
 */
fun toMacro(definition: List<String>?): MutableList<Macro>? {
    return when {
        definition == null -> null
        definition.isNotEmpty() -> mutableListOf(Macro()).also { var0 ->
            definition.forEach {
                processDefine("#define $it", var0)
            }
        }.also {
            macroList(macro = it)
        }
        else -> null
    }
}