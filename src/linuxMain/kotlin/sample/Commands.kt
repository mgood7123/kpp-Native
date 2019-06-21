package sample

import preprocessor.utils.`class`.extensions.*
import preprocessor.utils.core.algorithms.LinkedList

@Suppress("unused")
class Commands(repl: REPL) {
    inner class cmd {
        var command: String? = null
        var description: String? = null
        var alias: String? = null
        var function: (() -> Unit)? = null
    }

    var command = LinkedList<cmd>()
    val repl = repl
    val defaultCommandHeader = "command"
    val defaultDescriptionHeader = "description"

    fun add(command: String): Commands = this.also {
        it.command.append(
            cmd()
                .also {
                    it.command = command
                }
                .also {
                    it.description = "No description provided"
                }
        )
    }
    fun add(command: String, description: String): Commands = this.also {
        it.command.append(
            cmd()
                .also {
                    it.command = command
                }.also {
                    it.description = description
                }
        )
    }
    fun add(command: String, description: String, function: () -> Unit): Commands = this.also {
        it.command.append(
            cmd()
                .also {
                    it.command = command
                }.also {
                    it.function = function
                }.also {
                    it.description = description
                }
        )
    }
    fun add(command: String, function: () -> Unit): Commands = this.also {
        it.command.append(
            cmd()
                .also {
                    it.command = command
                }.also {
                    it.function = function
                }
                .also {
                    it.description = "No description provided"
                }
        )
    }
    fun alias(command: String, value: String): Commands = this.also {
        it.command.append(
            cmd()
                .also {
                    it.command = value
                }
                .also {
                    it.alias = command
                }
                .also {
                    it.description = "alias for $value"
                }
        )
    }
    fun get(command: String) : (() -> Unit)? {
        if (repl.debug) println("repl.debug = ${repl.debug}")
        this.command.forEach {
            when {
                it != null -> when {
                    it.command != null || it.alias != null -> when {
                        ifUnconditionalReturn(
                            ifTrueReturn(
                                it.alias.equals(command)
                            ) {
                                if (repl.debug) println("alias equals command")
                            }.ifFalseReturn {
                                if (repl.debug) println("alias does not equal command")
                            } && ifFalseReturn(
                                !it.command.equals(command)
                            ) {
                                if (repl.debug) println("command equals command, this is a looping alias")
                            }.ifTrueReturn {
                                if (repl.debug) println("command does not equal command, this is not a looping alias")
                            } && ifTrueReturn(
                                it.function == null
                            ) {
                                if (repl.debug) println("function is null")
                            }.ifFalseReturn {
                                if (repl.debug) println("function is not null")
                            }
                        )
                            {
                                if (repl.debug) println("alias found")
                            }.ifTrueReturn {
                            if (repl.debug) println("and it matches $command")
                            }.ifFalseReturn {
                            if (repl.debug) println("and it does not match $command")
                            } -> {
                                val t = get(it.command!!)
                                return t
                            }
                        ifUnconditionalReturn(it.command.equals(command) && it.alias == null) {
                            if (repl.debug) println("command found")
                        }.ifTrueReturn {
                            if (repl.debug) println("and it matches $command")
                        }.ifFalseReturn {
                            if (repl.debug) println("and it does not match $command")
                        } -> return it.function
                    }
                }
            }
        }
        return null
    }
    fun longestCommand(): Int {
        var currentLength = 0
        this.command.forEach {
            if (it != null) {
                if (it.command != null) {
                    var thisLength = it.command!!.length
                    if (thisLength isGreaterThan currentLength ) currentLength = thisLength
                    thisLength = defaultCommandHeader.length
                    if (thisLength isGreaterThan currentLength) currentLength = thisLength
                }
            }
        }
        return currentLength
    }
    fun longestAlias(): Int {
        var currentLength = 0
        this.command.forEach {
            if (it != null) {
                if (it.alias != null) {
                    var thisLength = it.alias!!.length
                    if (thisLength isGreaterThan currentLength ) currentLength = thisLength
                    thisLength = defaultCommandHeader.length
                    if (thisLength isGreaterThan currentLength) currentLength = thisLength
                }
            }
        }
        return currentLength
    }
    fun longestDescription(): Int {
        var currentLength = 0
        this.command.forEach {
            if (it != null) {
                if (it.description != null) {
                    var thisLineCurrentLength = 0
                    it.description!!.lines().forEach {
                        var thisLineLength = it.length
                        if (thisLineLength isGreaterThan thisLineCurrentLength) thisLineCurrentLength = thisLineLength
                        thisLineLength = defaultDescriptionHeader.length
                        if (thisLineLength isGreaterThan thisLineCurrentLength) thisLineCurrentLength = thisLineLength
                    }
                    val thisLength = thisLineCurrentLength
                    if (thisLength isGreaterThan currentLength) currentLength = thisLength
                }
            }
        }
        return currentLength
    }
    inner class Format {
        val seperationLength = 2
        val spacing = 2
        inner class PrettyPrint {
            inner class Frame {
                inner class Corner {
                    var topRight: String = "┓"
                    var bottomRight: String = "┛"
                    var bottomLeft: String = "┗"
                    var topLeft: String = "┏"
                }
                inner class Wall {
                    var top: String = "━"
                    var right: String = "┃"
                    var bottom: String = "━"
                    var left: String = "┃"
                }
                inner class Intersection {
                    var top: String = "┳"
                    var right: String = "┣"
                    var bottom: String = "┻"
                    var left: String = "┫"
                    var all: String = "╋"
                }
            }
            fun single(command: String, description: String) {
                val lengthAlias = longestAlias()
                val lengthCommand = longestCommand()
                val length = if (lengthAlias isGreaterThan lengthCommand) lengthAlias else lengthCommand
                if(length isLessThanOrEqualTo 0) return
                println(
                    // top
                    Frame().Corner().topLeft
                            + "".padExtendEnd(seperationLength, Frame().Wall().top)
                            + "".padExtendEnd(length, Frame().Wall().top)
                            + "".padExtendEnd(seperationLength, Frame().Wall().top)
                            + Frame().Intersection().top
                            + "".padExtendEnd(seperationLength, Frame().Wall().top)
                            + "".padExtendEnd(longestDescription(), Frame().Wall().top)
                            + "".padExtendEnd(seperationLength, Frame().Wall().top)
                            + Frame().Corner().topRight
                            + "\n"
                            // middle top
                            + Frame().Wall().left
                            + "".padExtendEnd(seperationLength, ' ')
                            + defaultCommandHeader.padExtendEnd(length, ' ')
                            + "".padExtendEnd(seperationLength, ' ')
                            + "┃"
                            + "".padExtendEnd(seperationLength, ' ')
                            + defaultDescriptionHeader.padExtendEnd(longestDescription(), ' ')
                            + "".padExtendEnd(seperationLength, ' ')
                            + Frame().Wall().right
                            + "\n"
                            // seperator
                            + Frame().Intersection().right
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + "".padExtendEnd(length, Frame().Wall().bottom)
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + Frame().Intersection().all
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + "".padExtendEnd(longestDescription(), Frame().Wall().bottom)
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + Frame().Intersection().left
                            + "\n"
                            // middle
                            + Frame().Wall().left
                            + "".padExtendEnd(seperationLength, ' ')
                            + command.padExtendEnd(length, ' ')
                            + "".padExtendEnd(seperationLength, ' ')
                            + "┃"
                            + "".padExtendEnd(seperationLength, ' ')
                            + description.padExtendEnd(longestDescription(), ' ')
                            + "".padExtendEnd(seperationLength, ' ')
                            + Frame().Wall().right
                            + "\n"
                            // bottom
                            + Frame().Corner().bottomLeft
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + "".padExtendEnd(length, Frame().Wall().bottom)
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + Frame().Intersection().bottom
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + "".padExtendEnd(longestDescription(), Frame().Wall().bottom)
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + Frame().Corner().bottomRight
                )
            }
            fun top(command: String, description: String) {
                val lengthAlias = longestAlias()
                val lengthCommand = longestCommand()
                val length = if (lengthAlias isGreaterThan lengthCommand) lengthAlias else lengthCommand
                if(length isLessThanOrEqualTo 0) return
                println(
                    Frame().Corner().topLeft
                            + "".padExtendEnd(seperationLength, Frame().Wall().top)
                            + "".padExtendEnd(length, Frame().Wall().top)
                            + "".padExtendEnd(seperationLength, Frame().Wall().top)
                            + Frame().Intersection().top
                            + "".padExtendEnd(seperationLength, Frame().Wall().top)
                            + "".padExtendEnd(longestDescription(), Frame().Wall().top)
                            + "".padExtendEnd(seperationLength, Frame().Wall().top)
                            + Frame().Corner().topRight
                )
                middleTop(defaultCommandHeader, defaultDescriptionHeader)
                middleTop(command, description)
            }
            fun middleTop(command: String, description: String) {
                val lengthAlias = longestAlias()
                val lengthCommand = longestCommand()
                val length = if (lengthAlias isGreaterThan lengthCommand) lengthAlias else lengthCommand
                if(length isLessThanOrEqualTo 0) return
                println(
                    Frame().Wall().left
                            + "".padExtendEnd(seperationLength, ' ')
                            + command.padExtendEnd(length, ' ')
                            + "".padExtendEnd(seperationLength, ' ')
                            + "┃"
                            + "".padExtendEnd(seperationLength, ' ')
                            + description.padExtendEnd(longestDescription(), ' ')
                            + "".padExtendEnd(seperationLength, ' ')
                            + Frame().Wall().right
                )
                middleSeperator()
            }
            fun middleBottom(command: String, description: String) {
                Debugger().breakPoint()
                val lengthAlias = longestAlias()
                val lengthCommand = longestCommand()
                val length = if (lengthAlias isGreaterThan lengthCommand) lengthAlias else lengthCommand
                if(length isLessThanOrEqualTo 0) return
                println(
                    Frame().Wall().left
                            + "".padExtendEnd(seperationLength, ' ')
                            + command.padExtendEnd(length, ' ')
                            + "".padExtendEnd(seperationLength, ' ')
                            + "┃"
                            + "".padExtendEnd(seperationLength, ' ')
                            + description.padExtendEnd(longestDescription(), ' ')
                            + "".padExtendEnd(seperationLength, ' ')
                            + Frame().Wall().right
                )
            }
            fun middleSeperator() {
                val lengthAlias = longestAlias()
                val lengthCommand = longestCommand()
                val length = if (lengthAlias isGreaterThan lengthCommand) lengthAlias else lengthCommand
                if(length isLessThanOrEqualTo 0) return
                println(
                    Frame().Intersection().right
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + "".padExtendEnd(length, Frame().Wall().bottom)
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + Frame().Intersection().all
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + "".padExtendEnd(longestDescription(), Frame().Wall().bottom)
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + Frame().Intersection().left
                )
            }
            fun bottom(command: String, description: String) {
                val lengthAlias = longestAlias()
                val lengthCommand = longestCommand()
                val length = if (lengthAlias isGreaterThan lengthCommand) lengthAlias else lengthCommand
                if(length isLessThanOrEqualTo 0) return
                middleBottom(command, description)
                println(
                    Frame().Corner().bottomLeft
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + "".padExtendEnd(length, Frame().Wall().bottom)
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + Frame().Intersection().bottom
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + "".padExtendEnd(longestDescription(), Frame().Wall().bottom)
                            + "".padExtendEnd(seperationLength, Frame().Wall().bottom)
                            + Frame().Corner().bottomRight
                )
            }
        }
        fun normal(command: String, description: String) {
            val lengthAlias = longestAlias()
            val lengthCommand = longestCommand()
            val length = if (lengthAlias isGreaterThan lengthCommand) lengthAlias else lengthCommand
            if(length isLessThanOrEqualTo 0) return
            println(command.padExtendEnd(length+seperationLength, ' ') + description)
        }
    }
    fun listCommands() {
        for ((index, it) in this.command.withIndex()) {
            when {
                it != null -> when {
                    (it.command != null && it.description != null) -> when {
                        this.command.count() == 1 -> Format().PrettyPrint().single(
                            when {
                                it.alias != null -> it.alias
                                else -> it.command
                            }!!, it.description!!)
                        else -> when(index) {
                            0 -> Format().PrettyPrint().top(
                                when {
                                    it.alias != null -> it.alias
                                    else -> it.command
                                }!!, it.description!!
                            )
                            this.command.count() - 1, this.command.count() -> Format().PrettyPrint().bottom(
                                when {
                                    it.alias != null -> it.alias
                                    else -> it.command
                                }!!, it.description!!
                            )
                            else -> Format().PrettyPrint().middleTop(
                                when {
                                    it.alias != null -> it.alias
                                    else -> it.command
                                }!!, it.description!!
                            )
                        }
                    }
                }
            }
        }
    }
}