package preprocessor.core.parserCombinator

import Lamdatrampoline
import preprocessor.utils.`class`.extensions.toStack
import preprocessor.utils.core.algorithms.LinkedList
import preprocessor.utils.core.algorithms.Stack

/*
[23:32] <cbreak> https://en.wikipedia.org/wiki/Chomsky_hierarchy
[23:32] <cbreak> macroprep: you were the one that mentioned FiniteStateMachine
[23:32] <cbreak> a finite state machine can parse regular languages
[23:40] <macroprep> im not sure
[23:41] <macroprep> id like to support the ability for left recursion if possible
[23:42] <macroprep> (tho im not sure if it is required for the language i want to parse)
[23:46] <macroprep> would it be easier to start at type 3 and work my way up?
[00:01] <macroprep> but yes, i want to parse a regular language to start with
[00:01] <macroprep> cbreak:
[00:03] <cbreak> there are parser generators for many language types
[00:03] <cbreak> for regular languages, for example regular expression parsers
[00:05] <macroprep> i know but i want to make my own
[00:08] <quarterback> It seems like long work, you have to deal with grammars first of that language.
[00:09] <quarterback> after you write grammar, then you can do lexing. You will have to learn lot of theory of parsing and lexing before which I think is about 2000 pages of text.
[00:09] <quarterback> You also can't use stdlib for it as stdlb slows down low level code which is done with char strings.
[00:10] <quarterback> You will also have to learn a lot of automata, regex, FiniteStateMachine's
[00:11] <macroprep> ok, well i have the grammar for the syntax of the language i am targeting
[00:13] <quarterback> its called EBNF
...
[15:26] <_W_> macroprep: these are computational theory machines, never actually implemented (except as curiosities)
[15:26] <_W_> it's a hypothetical thing thought up for the sake of mathematics
[15:30] <macroprep> _W_: oh...
[15:31] <macroprep> so all these https://en.wikipedia.org/wiki/Finite-state_machine#Classification (transducers, acceptors, classifiers and sequencers )   dont actually exist nor have an implementation?
[15:31] <macroprep> _W_:
[15:33] <macroprep> (and their respecive articles: https://en.wikipedia.org/wiki/Finite-state_transducer https://en.wikipedia.org/wiki/Moore_machine https://en.wikipedia.org/wiki/Mealy_machine )
[15:36] <_W_> As a general rule, yes. Some people /do/ of course implement (limited versions of) them, as jokes or personal exercises
[15:36] <_W_> but their purpose, as specifications, is to enable mathematics and classifications of computation
[15:38] <_W_> state machines, in general, are of course used in practical programming
[15:38] <_W_> but "finite" state machines specifically is a classification to enable mathematics
[15:39] <_W_> (all actual programming on real computers is finite state machines)
[15:39] <pavonia> atralheaven: If you are working on an algorithm that may return an integer or a special fail value, you don't need to use -1 or some other oddity for representing the error but you can use a new value :myerror instead
[15:40] <pavonia> or :novalue or whatever
[15:40] <macroprep> _W_: ok, so if i have a Finite state automaton, how could i convert it into, it implement using it, a Non-deterministic pushdown automaton
[15:40] <macroprep> or implement using it, *
[15:41] <_W_> macroprep: you never would. It is mathematically proven that you /can/ and that's all that computational theory cares aboutr
[15:41] <_W_> you would just reference the proof and leave the actual conversion hypothetical
[15:42] <atralheaven> pavonia: that's great, I've seen :ok, I thought atoms are something more than just that, it's kinda like an anonymous function, but for variables
[15:43] <macroprep> _W_: as according to https://en.wikipedia.org/wiki/Chomsky_hierarchy, i currently have a type 3, and i want to obtain a type 2, then a type 1, and then a type 0
[15:44] <_W_> macroprep: grammars are something slightly different
[15:44] <_W_> macroprep: are you designing a programming language syntax?
[15:44] <pavonia> For an deterministic PDA you would add a stack to it
[15:44] <macroprep> _W_: im designing a parser
[15:53] <_W_> well, that you can use it to simulate any other turing machine, technically speaking
[15:54] <_W_> there's a few heuristics you can use to recognize them, that I don't recall at the top of my head. Loops, recursion, that kind of thing
[15:56] <_W_> yeah, like I said, could not recall the specifics
[15:57] <_W_> generally speaking when someone wishes to establish that something is a turing machine, what they do is use it to implement something else that is already known to be a turing machine
[15:59] <macroprep> _W_: ok
[15:59] <_W_> e.g. the game of life
[16:01] <nitrix> (What _W_ described though is Turing Completeness, not to be confused with the Turing Machine computational model)
[16:03] <nitrix> And yeah, one of the formal method to claim turing equivalenet is by proving language A can implement language B and vice-versa.
[16:03] <nitrix> *turing equivalence
*/
/*
https://en.wikipedia.org/wiki/Chomsky_hierarchy

   https://en.wikipedia.org/wiki/Combinational_logic // useful in circuitry only
  https://en.wikipedia.org/wiki/Finite-state_machine
 https://en.wikipedia.org/wiki/Pushdown_automaton
https://en.wikipedia.org/wiki/Turing_machine

Grammar 	Languages            	Automaton                                        	Production rules (constraints)*
Type-0  	Recursively enumerable 	Turing machine                                   	α A β → γ
Type-1 	    Context-sensitive 	    Linear-bounded non-deterministic Turing machine 	α A β → α γ β
Type-2 	    Context-free 	        Non-deterministic pushdown automaton 	            A → α
Type-3 	    Regular              	Finite state automaton 	                            A → a
                                                                                        and
                                                                                        A → a B
 */
// TODO: Finite state machines can be subdivided into transducers, acceptors, classifiers and sequencers.
//  https://en.wikipedia.org/wiki/Finite-state_machine#Classification
//  https://en.wikipedia.org/wiki/Finite-state_transducer
//  https://en.wikipedia.org/wiki/Moore_machine
//  https://en.wikipedia.org/wiki/Mealy_machine

class StateMachines {
    class FiniteStateMachine<T> {
        val state = Classes().State()
        var tapeStack = Stack<Stack<T>>()
        fun addTape() = tapeStack.push(Stack())
        var inputTape = 0
        var outputTape = 1

        fun getInput(): T? {
            if (tapeStack.stack.isEmpty()) return null
            val tape = tapeStack[inputTape]
            return if (tape != null) {
                if (tape.peek() != null) {
                    tape.pop()
                } else null
            } else null
        }

        inner class Classes {
            inner class State {
                inner class StateList(s: Int, a: (state: State) -> Any) {
                    val state: Int = s
                    val action: (state: State) -> Any = a
                }

                val stateList = mutableListOf<StateList>()
                private var defaultState: Int = -1

                private var state: Int = defaultState

                fun currentState() = state
                fun defaultState() = defaultState

                fun add(state: Int, action: (state: State) -> Any) = stateList.add(StateList(state, action))

                fun get(targetState: Int) = stateList.find { it.state == targetState }

                fun setDefaultState(targetState: Int): StateList? =
                    get(targetState)?.also { defaultState = targetState }

                fun setState(targetState: Int): StateList? = get(targetState)?.also { state = targetState }

                fun erase() {
                    stateList.clear()
                    state = -1
                    defaultState = -1
                }

                fun transitionState(targetState: Int): Pair<((State) -> Any)?, State> =
                    Pair(setState(targetState)?.action, this)

                fun transitionDefault(): Pair<((State) -> Any)?, State> = Pair(setState(defaultState)?.action, this)
                fun transitionStop(): Pair<((State) -> Any)?, State> = Pair(null, this)
                fun executeState(targetState: Int) = get(targetState)?.also { Lamdatrampoline(it.action, this) } != null
                fun executeDefault() = get(defaultState)?.also { Lamdatrampoline(it.action, this) } != null
                fun executeCurrent() = get(state)?.also { Lamdatrampoline(it.action, this) } != null
            }
        }

        fun transition(targetState: Int) = state.transitionState(targetState)
        fun transitionDefault() = state.transitionDefault()
        fun transitionStop() = state.transitionStop()
        fun executeState(targetState: Int) = state.executeState(targetState)
        fun executeDefault() = state.executeDefault()
        fun executeCurrent() = state.executeCurrent()

        fun reset() {
            state.setState(state.defaultState())
        }

        fun erase() {
            state.erase()
        }
    }

    class TuringMachine<T> {
        val state = Classes().State()
        var tapeStack = Stack<LinkedList<T>>()
        fun addTape() = tapeStack.push(LinkedList())
        var inputTape = 0
        var outputTape = 0
        var head = 0
        var offsetLeft = 0
        var blank: T? = null
        var Empty = null
        fun hasTape() = tapeStack.stack.isEmpty()
        fun hasTape(tapeIndex: Int) = if (hasTape()) tapeStack[tapeIndex] != null else false
        fun getInputTape() = tapeStack[inputTape]!!
        fun getOutputTape() = tapeStack[outputTape]!!
        fun tapeHasInput() = hasTape(inputTape)
        fun tapeHasOutput() = hasTape(outputTape)

        fun readTape() =
            if (tapeHasInput()) {
                getInputTape().nodeAtIndex(head)?.value
            } else Empty

        fun writeTape(value: T?) =
            if (tapeHasOutput()) {
                getOutputTape().nodeAtIndex(head)?.value
                true
            } else false

        fun moveTapeRight(tapeIndex: Int) =
            if (hasTape(tapeIndex)) {
                val tape = getInputTape()
                if (tape.isLast(head - offsetLeft)) {
                    tape.appendLast(blank)
                }
                if (offsetLeft == 0) head++ else offsetLeft--
                true
            } else false

        fun moveTapeLeft(tapeIndex: Int) =
            if (hasTape(tapeIndex)) {
                val tape = getInputTape()
                if (tape.isFirst(head - offsetLeft)) {
                    tape.appendFirst(blank)
                }
                offsetLeft++
                true
            } else false

        fun moveInputTapeRight() = moveTapeRight(inputTape)
        fun moveInputTapeLeft() = moveTapeLeft(inputTape)
        fun moveOutputTapeRight() = moveTapeRight(outputTape)
        fun moveOutputTapeLeft() = moveTapeLeft(outputTape)

        inner class Classes {
            inner class State {
                inner class StateList(s: Int, a: (state: State) -> Any) {
                    val state: Int = s
                    val action: (state: State) -> Any = a
                }

                val stateList = mutableListOf<StateList>()
                private var defaultState: Int = -1

                private var state: Int = defaultState

                fun currentState() = state
                fun defaultState() = defaultState

                fun add(state: Int, action: (state: State) -> Any) = stateList.add(StateList(state, action))

                fun get(targetState: Int) = stateList.find { it.state == targetState }

                fun setDefaultState(targetState: Int): StateList? =
                    get(targetState)?.also { defaultState = targetState }

                fun setState(targetState: Int): StateList? = get(targetState)?.also { state = targetState }

                fun erase() {
                    stateList.clear()
                    state = -1
                    defaultState = -1
                }

                fun transitionState(targetState: Int): Pair<((State) -> Any)?, State> =
                    Pair(setState(targetState)?.action, this)

                fun transitionDefault(): Pair<((State) -> Any)?, State> = Pair(setState(defaultState)?.action, this)
                fun transitionStop(): Pair<((State) -> Any)?, State> = Pair(null, this)
                fun executeState(targetState: Int) = get(targetState)?.also { Lamdatrampoline(it.action, this) } != null
                fun executeDefault() = get(defaultState)?.also { Lamdatrampoline(it.action, this) } != null
                fun executeCurrent() = get(state)?.also { Lamdatrampoline(it.action, this) } != null
            }
        }

        fun transition(targetState: Int) = state.transitionState(targetState)
        fun transitionDefault() = state.transitionDefault()
        fun transitionStop() = state.transitionStop()
        fun executeState(targetState: Int) = state.executeState(targetState)
        fun executeDefault() = state.executeDefault()
        fun executeCurrent() = state.executeCurrent()

        fun reset() {
            state.setState(state.defaultState())
        }

        fun erase() {
            state.erase()
        }
    }

    fun test() {
        val FiniteStateMachine = StateMachines.FiniteStateMachine<Int>()
        // no tapes are used here
        FiniteStateMachine.state.add(FiniteStateMachine.state.defaultState()) {
            println("current state: ${it.currentState()}")
            it.transitionState(1)
        }
        FiniteStateMachine.state.add(1) {
            println("current state: ${it.currentState()}")
            it.transitionStop()
        }
        FiniteStateMachine.executeDefault();
    }
}

fun StateMachines.TuringMachineSimpleProgram() {
    val TM = StateMachines.TuringMachine<String>()
    // both input and output tapes are set to zero
    TM.state.add(0) {
        when (TM.readTape()) {
            TM.blank -> {
                TM.moveOutputTapeLeft()
                it.transitionState(1)
            }
            "0" -> {
                TM.writeTape("1")
                TM.moveOutputTapeRight()
                it.transitionState(1)
            }
            "1" -> {
                TM.writeTape("0")
                TM.moveOutputTapeRight()
                it.transitionState(0)
            }
            else -> it.transitionStop()
        }
    }
    TM.state.add(1) {
        when (TM.readTape()) {
            TM.blank -> {
                TM.moveOutputTapeRight()
                it.transitionStop()
            }
            "0" -> {
                TM.writeTape("1")
                TM.moveOutputTapeLeft()
                it.transitionState(1)
            }
            "1" -> {
                TM.writeTape("0")
                TM.moveOutputTapeLeft()
                it.transitionState(1)
            }
            else -> it.transitionStop()
        }
    }
    TM.blank = " "
    TM.addTape()
    TM.getInputTape().appendFirst(TM.blank)
    TM.getInputTape().appendFirst("0")
    TM.getInputTape().appendFirst("0")
    TM.getInputTape().appendFirst("1")
    TM.executeState(0)
    println(TM.getOutputTape())
}

fun acceptor() {
    val Acceptor = StateMachines.FiniteStateMachine<String>()
    Acceptor.state.add(Acceptor.state.defaultState()) {
        println("state 1")
        when (Acceptor.getInput()) {
            "a" -> it.transitionState(0)
            else -> it.transitionState(3)
        }
    }
    Acceptor.state.add(0) {
        println("state 2")
        when (Acceptor.getInput()) {
            "b" -> it.transitionState(1)
            else -> it.transitionState(3)
        }
    }
    Acceptor.state.add(1) {
        println("state 3")
        when (Acceptor.getInput()) {
            "c" -> it.transitionState(2)
            else -> it.transitionState(3)
        }
    }
    Acceptor.state.add(2) {
        println("state 4")
        when (Acceptor.getInput()) {
            null -> println("success")
            else -> it.transitionState(3)
        }
    }
    Acceptor.state.add(3) {
        println("state 5")
        println("INVALID INPUT")
    }
    Acceptor.addTape() // inputTape is 0 by default so we do not need to set it
    Acceptor.tapeStack[0] = "abc".toStack()
    Acceptor.executeDefault()
    Acceptor.tapeStack[0] = "abcef".toStack()
    Acceptor.executeDefault()
    Acceptor.tapeStack[0] = "c".toStack()
    Acceptor.executeDefault()
    Acceptor.tapeStack[0] = "cba".toStack()
    Acceptor.executeDefault()
    Acceptor.tapeStack[0] = "abb".toStack()
    Acceptor.executeDefault()
}