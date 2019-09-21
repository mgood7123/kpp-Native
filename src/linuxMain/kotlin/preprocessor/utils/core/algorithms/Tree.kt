package preprocessor.utils.core.algorithms

class Tree<T> {

    constructor() {}

    constructor(v: T?) {
        value = v
    }

    var root = true
    var subroot = false

    var depth: Int = 0
    var value: T? = null
    var parent: Tree<T>? = null
    var child: MutableList<Tree<T>>? = null
    var last: Tree<T>? = null
    var current: Tree<T>? = this
    private var groupStart = false
    private var groupFinish = false
    private var optimizereturn = false

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
        if (parent != null) {
            var x = parent
            while (x != null) {
                action(x!!)
                if (x!!.parent == null) break
                x = x!!.parent
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

    fun groupBegin(): Tree<T> {
        val tree = Tree<T>()
        tree.parent = this
        tree.depth = depth + 1
        tree.groupStart = true
        return add(tree)
    }

    fun groupEnd(): Tree<T> {
        val tree = Tree<T>()
        tree.parent = this
        tree.depth = depth + 1
        tree.groupFinish = true
        add(tree)
        probogateUp { it.last = last }
        current = last
        probogateUp { it.current = current }
        return current!!
    }

    fun add(value: T?): Tree<T> {
        val tree = Tree<T>()
        tree.root = false
        tree.subroot = false
        tree.value = value
        return add(tree)
    }

    fun add(tree: Tree<T>): Tree<T> {
        tree.parent = this
        tree.depth += 1
        if (child == null) child = mutableListOf()
        child!!.add(tree)
        last = child!![child!!.indexOf(tree)]
        probogateUp { it.last = last }
        current = last
        probogateUp { it.current = current }
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

    fun toString(action: (obj: T?) -> String?): String {
        var x = "[ "
        x += when {
            groupStart -> "<groupStart>"
            groupFinish -> "<groupFinish>"
            else -> action(value)
        }
        if (child != null) {
            x += ", "
            val c = child!!.iterator()
            while (c.hasNext()) {
                x += c.next().toString(action)
                if (c.hasNext()) x += ", "
            }
        }
        x += " ]"
        return x
    }

    override fun toString(): String = toString { it.toString() }

    fun prettyPrint(action: (obj: T?) -> String?, prev: MutableList<Boolean>): String {
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
            groupStart -> "<groupStart>"
            groupFinish -> "<groupFinish>"
            root -> "<root>"
            subroot -> "<subroot>"
            else -> action(value)
        }
        x += '\n'
        if (child != null) {
            val c = child!!.iterator()
            while (c.hasNext()) {
                val z = c.next()
                val np = prev.toMutableList()
                np.add(c.hasNext())
                x += z.prettyPrint(action, np)
            }
        }
        return x
    }

    fun prettyPrint(action: (obj: T?) -> String?): String = prettyPrint(action, mutableListOf())

    fun prettyPrint(): String = prettyPrint { it.toString() }

    fun hasChildren() = child != null

    fun getRoot(): Tree<T> = if (parent == null) this else parent!!.getRoot()

    fun defineSubRoots() {
        val x = getRoot()
        x.subroot = false
        x.defineSubRootsRecursive(true)
        x.optimize()
    }

    fun defineSubRootsRecursive(r: Boolean = false) {
        if (hasChildren()) {
            child!!.forEach {
                it.defineSubRootsRecursive()
            }
        }
        if (root && !groupStart && !groupFinish && !r) {
            root = false
            subroot = true
        }
    }

    fun reparentNull(): Boolean {
        if (subroot) {
            if (parent!!.groupStart) {
                val node = parent!!.parent!!.child!!
                val index = node.indexOf(parent!!)
                if (index == -1) return false
                if (node.size > index+1) {
                    val node0 = node[index]
                    val node1 = node[index+1]
                    if (node1.groupFinish) {
                        val parentIndex = parent!!.parent!!.child!!.indexOf(parent!!)
                        var parentNode = parent!!.parent!!.child!![parentIndex]
                        node[index] = this
                        node.removeAt(index+1)
                        parent!!.groupStart = false
                        return true
                    }
                }
            }
        }
        return false
    }

    fun optimize(): Boolean {
        optimizereturn = false
        optimize(0)
        return optimizereturn
    }

    fun optimize(index: Int): Boolean {
        if (reparentNull()) {
            getRoot().optimizereturn = true
            return true
        }
        if (hasChildren()) {
            return if (child!![index].optimize(0)) getRoot().optimize(0)
            else if (child!!.size > index+1) optimize(index+1)
            else false
        }
        return false
    }
}