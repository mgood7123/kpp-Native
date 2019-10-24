package preprocessor.utils.core.algorithms

@Suppress("unused")
class Stack<T>: Iterable<T> {

    constructor() {}
    constructor(vararg item: T) {
        item.forEach {
            push(it)
        }
    }

    constructor(convert: (t: T) -> T, vararg item: T) {
        item.forEach {
            push(convert(it))
        }
    }

    /**
     * @sample usage
     */
    constructor(arrayIterator: (t: T, ACTION: (Any?) -> Int) -> Unit, ACTION: (item: Any?) -> T, vararg item: T) {
        item.forEach {
            arrayIterator(it) { item -> push(ACTION(item)) }
        }
    }

    private fun usage() {
        val x = Stack(
            { t, ACTION -> t.forEach { ACTION(it) } },  // arrayIterator
            { (it as Char).toString()},                 // ACTION
            "hi", "bye"                          // item
        )
    }

    var stack = LinkedList<T>()
    var size = 0
    operator fun get(i: Int) = stack[i]
    operator fun set(i: Int, value: T) {
        stack[i] = value
    }
    fun isEmpty() = stack.isEmpty()
    fun push(value: T): Int {
        val i = stack.append(value)
        size = stack.size
        return i
    }
    fun push(list: List<T>): Int {
        var i = 0
        list.iterator().also {
            while(it.hasNext()) i = push(it.next())
        }
        return i
    }
    fun peek(): T? = stack.first()?.value
    fun pop(): T? = when {
        stack.isEmpty() -> throw NoSuchElementException()
        else -> {
            val i = stack.removeAtIndex(0)
            size = stack.count()
            i
        }
    }
    fun contains(s: T): Boolean = stack.contains(s)
    fun indexOfNode(node: LinkedList<T>.Node) = stack.indexOfNode(node)
    override fun toString() = stack.toString()
    /**
     * returns this stack as a string with each element appended to the end of the string
     */
    fun toStringConcat() = stack.toStringConcat()

    override fun iterator() = stack.iterator()
    fun forEach(action: (T) -> Unit) = stack.forEach(action)
    fun clear() = stack.clear().apply { size = stack.count() }
    fun clone() = Stack<T>().also { it.stack = stack.clone() }

    fun test() {
        val s = Stack<String>()
        s.push("John")
        println(s)
        s.push("Carl")
        println(s)
        println("peek item: ${s.peek()}")
        println("pop item: ${s.pop()}")
        println("peek item: ${s.peek()}")
    }
}