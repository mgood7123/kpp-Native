package preprocessor.utils.core.algorithms

class Tree<T> {

    constructor() {}

    constructor(v: T?) {
        value = v
    }

    var depth: Int = 0
    var value: T? = null
    var parent: Tree<T>? = null
    var child: MutableList<Tree<T>>? = null
    var last: Tree<T>? = null
    var current: Tree<T>? = this
    private var groupStart = false
    private var groupFinish = false

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
        if (parent != null) {
            var x = parent
            while (x != null) {
                if (x.parent == null) break
                if (x.groupStart) {
                    current = x.parent
                    break
                }
                x = x.parent
            }
        }
        if (parent != null) {
            var x = parent
            while (x != null) {
                if (x.parent == null) break
                x = x.parent
            }
            x!!.current = current
        }
        return current!!
    }

    fun add(value: T?): Tree<T> {
        val tree = Tree<T>()
        tree.value = value
        return add(tree)
    }

    fun add(tree: Tree<T>): Tree<T> {
        tree.parent = this
        tree.depth += 1
        if (child == null) child = mutableListOf()
        child!!.add(tree)
        last = child!![child!!.indexOf(tree)]
        if (parent != null) {
            var x = parent
            while (x != null) {
                if (x.parent == null) break
                x = x.parent
            }
            x!!.last = last
        }
        current = last
        if (parent != null) {
            var x = parent
            while (x != null) {
                if (x.parent == null) break
                x = x.parent
            }
            x!!.current = current
        }
        return child!![child!!.indexOf(tree)]
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

    private var final = false

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
}
