package preprocessor.utils.core.algorithms

import preprocessor.utils.core.abort

class Tree<T> {

    constructor() {}

    var NOT: Boolean = false
    var STOP = false
    var DEBUG = false

    var root = true
    var subroot = false

    var depth: Int = 0
    var value: T? = null
    var parent: Tree<T>? = null
    var child: MutableList<Tree<T>>? = null
    var last: Tree<T>? = this
    var current: Tree<T>? = this
    var LeftParenthesis = false
    var RightParenthesis = false

    fun probogateUpAndMatchAll(condition: (v: Tree<T>) -> Boolean): Boolean {
        var matches = 0
        var total = 0
        if (condition(this)) matches += 1
        total += 1
        if (parent != null) {
            var x = parent
            while (x != null) {
                if (condition(x!!)) matches += 1
                total += 1
                if (x!!.parent == null) break
                x = x!!.parent
            }
        }
        return matches == total
    }

    fun probogateUp(action: (v: Tree<T>) -> Unit) {
        action(this)
        val loop = mutableListOf<Tree<T>>()
        if (parent != null) {
            var x = parent
            while (x != null) {
                val recursion = loop.contains(x)
                if (DEBUG) println("loop contains parent: $recursion")
                if (recursion) abort("infinite recursion detected")
                loop.add(x)
                action(x)
                if (x.parent == null) break
                x = x.parent
            }
        }
    }

    fun probogateDownAndMatchAll(condition: (v: Tree<T>) -> Boolean): Boolean {
        var matches = 0
        var total = 0
        if (condition(this)) matches += 1
        total += 1
        if (hasChildren()) {
            child!!.forEach {
                if (it.probogateDownAndMatchAll(condition)) matches += 1
                total += 1
            }
        }
        return matches == total
    }

    fun probogateDown(action: (v: Tree<T>) -> Unit) {
        action(this)
        if (hasChildren()) {
            child!!.forEach {
                it.probogateDown(action)
            }
        }
    }

    fun detectCyclic(name: String? = null, loop: MutableList<Tree<T>>? = mutableListOf<Tree<T>>()) {
        if (DEBUG) if (name != null) println("cyclic test: $name")
        val recursion = loop!!.contains(this)
        if (DEBUG) println("loop contains this: $recursion")
        if (recursion) abort("cyclic recursion detected")
        loop.add(this)
        if (hasChildren()) {
            child!!.forEach {
                it.detectCyclic(null, loop)
            }
        }
        if (DEBUG) if (name != null) println("cyclic test passed for $name")
    }

    fun <T> MutableList<Tree<T>>?.detectCyclic(name: String? = null) {
        if (DEBUG) if (name != null) println("cyclic test (MutableList): $name")
        this?.forEach {
            it.detectCyclic("${name ?: "it"}[${this.indexOf(it)}]")
        }
        if (DEBUG) if (name != null) println("cyclic test (MutableList) passed for $name")
    }

    private fun checkUnique(STABLE_NAME: String = "STABLE_TREE", UNSTABLE_NAME: String = "UNSTABLE_TREE", STABLE_TREE: Tree<T>, STABLE_INDEX: MutableList<Int> = mutableListOf(), UNSTABLE_INDEX: MutableList<Int> = mutableListOf()) {
        STABLE_TREE.detectCyclic(STABLE_NAME) // ensure STABLE_TREE is cyclic free
        if (this == STABLE_TREE) {
            var Ustr = UNSTABLE_NAME
            UNSTABLE_INDEX.forEach { Ustr += ".child!![$it]" }
            var Sstr = STABLE_NAME
            STABLE_INDEX.forEach { Sstr += ".child!![$it]" }
            abort("$UNSTABLE_NAME ($Ustr) CONTAINS A REFERENCE TO $STABLE_NAME ($Sstr)")
        }
        val UI = UNSTABLE_INDEX.toMutableList()
        if (hasChildren()) child!!.forEach {
            UI.add(child!!.indexOf(it))
            it.checkUnique(STABLE_NAME, UNSTABLE_NAME, STABLE_TREE, STABLE_INDEX, UI)
        }
    }

    private fun isUnique(STABLE_NAME: String = "STABLE_TREE", UNSTABLE_NAME: String = "UNSTABLE_TREE", UNSTABLE_TREE: Tree<T>, STABLE_INDEX: MutableList<Int> = mutableListOf(), UNSTABLE_INDEX: MutableList<Int> = mutableListOf()) {
        this.detectCyclic(STABLE_NAME) // ensure STABLE_TREE is cyclic free
        UNSTABLE_TREE.checkUnique(STABLE_NAME, UNSTABLE_NAME, this, STABLE_INDEX, UNSTABLE_INDEX)
        val SI = STABLE_INDEX.toMutableList()
        if (hasChildren()) child!!.forEach {
            SI.add(child!!.indexOf(it))
            it.isUnique(STABLE_NAME, UNSTABLE_NAME, UNSTABLE_TREE, SI, UNSTABLE_INDEX)
        }
    }

    private fun copy(tree: Tree<T>): Tree<T> {
        val new = Tree<T>()
        new.current = tree.current
        new.parent = tree.parent
        new.NOT = tree.NOT
        new.STOP = tree.STOP
        new.DEBUG = tree.DEBUG
        new.root = tree.root
        new.LeftParenthesis = tree.LeftParenthesis
        new.RightParenthesis = tree.RightParenthesis
        new.depth = tree.depth
        new.subroot = tree.subroot
        new.child = tree.child?.toMutableList()
        new.last = tree.last
        new.TAG = tree.TAG
        new.value = tree.value
        return new
    }

    private fun deepCopy(tree: Tree<T>, newParent: Tree<T>? = null): Tree<T> {
        tree.detectCyclic("deepCopy_tree")
        val new = copy(tree)
        new.parent = newParent
        if (tree.hasChildren()) {
            tree.child!!.forEach {
                new.child!![tree.child!!.indexOf(it)] = it.deepCopy(it, new)
            }
        }
        tree.isUnique("deepCopy_tree", "deepCopy_new", new)
        return new
    }

    fun leftParenthesis(): Tree<T> {
        val tree = Tree<T>()
        tree.parent = this
        tree.depth = depth + 1
        tree.LeftParenthesis = true
        return add(tree)
    }

    fun rightParenthesis(): Tree<T> {
        val tree = Tree<T>()
        tree.parent = this
        tree.depth = depth + 1
        tree.RightParenthesis = true
        add(tree)
        probogateUp { it.last = last }
        current = last
        probogateUp { it.current = current }
        return current!!
    }

    var TAG: String? = null

    fun add(value: T?): Tree<T> {
        return add(null, value)
    }

    fun add(tag: String?, value: T?): Tree<T> {
        val tree = Tree<T>()
        tree.TAG = tag
        tree.root = false
        tree.subroot = false
        tree.value = value
        return add(tree)
    }

    fun add(tree: Tree<T>): Tree<T> {
        return add(null, tree)
    }

    fun lastTree(): Tree<T> {
        return if (hasChildren()) child!!.last().lastTree()
        else this
    }

    fun add(tag: String?, tmp: Tree<T>): Tree<T> {
        if (DEBUG) println("\n\n\n\nADD TMP")
        if (DEBUG) println("root: $root, subroot: $subroot, parent: $parent, child: $child")
        tmp.detectCyclic("tmp") // passes
        val tree = deepCopy(tmp)
        tree.DEBUG = DEBUG
        tree.detectCyclic("tree") // passes
        if (tree.TAG == null) tree.TAG = tag
        tree.parent = this
        tree.depth += 1
        tree.detectCyclic("tree (after tree.parent = this)") // passes
        if (child == null) {
            if (DEBUG) println("root: $root, subroot: $subroot, child: $child")
            child = mutableListOf()
        }
        child.detectCyclic("child")
        if (DEBUG) println("child before: $child")
        if (parent != null) parent!!.detectCyclic("parent")
        if (DEBUG) println("parent before: $parent")
        tree.detectCyclic("tree")
        if (DEBUG) println("tree before: $tree")
        if (tree.parent != null) tree.parent!!.detectCyclic("tree.parent")
        if (DEBUG) println("tree.parent before: ${tree.parent}")
        if (DEBUG) println("adding tree to child")
        child!!.add(tree)
        if (DEBUG) println("added tree to child")
        child.detectCyclic("child")
        if (DEBUG) println("child after: $child")
        if (parent != null) parent!!.detectCyclic("parent")
        if (DEBUG) println("parent after: $parent")
        tree.detectCyclic("tree")
        if (DEBUG) println("tree after: $tree")
        if (tree.parent != null) tree.parent!!.detectCyclic("tree.parent")
        if (DEBUG) println("tree.parent after: ${tree.parent}")
        last = child!![child!!.indexOf(tree)]
        last!!.detectCyclic("last") // fails
        if (last!!.root) last = last!!.lastTree()
        if (DEBUG) println("probogating last up")
        probogateUp { it.last = last }
        if (DEBUG) println("probogated up")
        if (DEBUG) if (last == null) println("last = null")
        else {
            last!!.detectCyclic("last")
            if (DEBUG) println("last = $last")
            if (DEBUG) println("last!!::class = ${last!!::class}")
        }
        current = last
        if (DEBUG) println("probogating current up")
        probogateUp { it.current = current }
        if (DEBUG) println("probogated up")
        if (DEBUG) if (current == null) println("current = null")
        else {
            current!!.detectCyclic("current")
            if (DEBUG) println("current = $current")
            if (DEBUG) println("current!!::class = ${current!!::class}")
        }
        last!!.defineSubRoots()
        return last!!
    }

    fun find(target: T): Tree<T>? {
        if (value == target) return this
        if (child == null) return null
        else child!!.forEach {
            val result = it.find(target)
            if (result != null) return result
        }
        return null
    }

    fun toString(getValue: (obj: T?) -> String?): String {
        // pre-order depth first tree traversal
        var x = "[ "
        x += when {
            LeftParenthesis -> "<LeftParenthesis>"
            RightParenthesis -> "<RightParenthesis>"
            else -> getValue(value)
        }
        if (hasChildren()) {
            x += ", "
            val c = child!!.iterator()
            while (c.hasNext()) {
                x += c.next().toString(getValue)
                if (c.hasNext()) x += ", "
            }
        }
        x += " ]"
        return x
    }

    override fun toString(): String = toString { it.toString() }

    fun prettyPrint(getValue: (obj: T?) -> String?, prev: MutableList<Boolean>): Pair<Boolean, String> {
        // pre-order tree traversal
        var x = ""
        prev.forEach {
            x += when {
                it -> "│  "
                else -> "   "
            }
        }
        if (prev.isNotEmpty()) {
            x = x.dropLast(3)
            x += when {
                prev.last() -> "├──"
                else -> "└──"
            }
        }
        x += when {
            LeftParenthesis -> "NOT: $NOT TAG: $TAG <LeftParenthesis>"
            RightParenthesis -> "TAG: $TAG <RightParenthesis>"
            root -> "TAG: $TAG <root>"
            subroot -> "┐"
            else -> "TAG: $TAG VALUE: ${getValue(value)}"
        }
        x += '\n'
        if (STOP) return Pair(false, x)
        if (hasChildren()) {
            val c = child!!.iterator()
            while (c.hasNext()) {
                val z = c.next()
                val np = prev.toMutableList()
                np.add(c.hasNext())
                val y = z.prettyPrint(getValue, np)
                x += y.second
                if (!y.first) return Pair(false, x)
            }
        }
        return Pair(true, x)
    }

    fun prettyPrint(getValue: (obj: T?) -> String?): String = prettyPrint(getValue, mutableListOf()).second

    fun prettyPrint(): String = prettyPrint { it.toString() }

    fun hasChildren() = child != null

    fun getRoot(): Tree<T> = if (parent == null) this else parent!!.getRoot()

    fun defineSubRoots() {
        val x = getRoot()
        x.subroot = false
        x.defineSubRootsRecursive(true)
    }

    fun defineSubRootsRecursive(r: Boolean = false) {
        if (hasChildren()) {
            child!!.forEach {
                it.defineSubRootsRecursive()
            }
        }
        if (root && !LeftParenthesis && !RightParenthesis && !r) {
            root = false
            subroot = true
        }
    }

    fun process(printProgress: Boolean = false, match: (obj: T?) -> Boolean, getValue: (obj: T?) -> String?, not: Boolean = false): Boolean {
        val condition = true
        val isNotRoot = !root && !subroot
        var result = false
        if (isNotRoot) {
            result = match(value) == condition
            if (printProgress) {
                STOP = true
                println("trying: ${getRoot().prettyPrint(getValue)}result: $result")
                STOP = false
            }
            if (child == null) return !NOT
        }
        val pass = !isNotRoot || result
        if (pass) {
            val x = child!!.iterator()
            var LeftParenthesisMatch = false
            while(x.hasNext()) {
                val r = x.next()
                when {
                    r.LeftParenthesis -> {
                        // group_enter()
                        // child_enter()
                        LeftParenthesisMatch = r.child!![0].process(printProgress, match, getValue, NOT)
                    }
                    r.RightParenthesis -> {
                        // group_leave()
                        if (LeftParenthesisMatch) {
                            // child_success()
                            // child_leave()
                            LeftParenthesisMatch = false
                            if (r.TAG == "AND") {
                                // child_enter()
                                if (r.child!![0].process(printProgress, match, getValue, not)) {
                                    // child_success()
                                    return !NOT
                                }
                                // child_leave()
                            } else return !NOT
                        }
                        // child_leave()
                    }
                    else -> {
                        // child_enter()
                        if (r.process(printProgress, match, getValue, not)) {
                            // child_success()
                            return !NOT
                        }
                        // child_leave()
                    }
                }
            }
        } else return NOT
        return NOT
    }
}