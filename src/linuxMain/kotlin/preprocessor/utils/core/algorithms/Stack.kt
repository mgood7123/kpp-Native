package preprocessor.utils.core.algorithms

@Suppress("unused")
class Stack<T>() {

    var stack = LinkedList<T>()
    var size = 0
    fun addLast(value: T) = stack.append(value).apply { size = stack.count() }
    fun addLast(list: List<T>) { list.iterator().also { while(it.hasNext()) this.addLast(it.next()) } }
    fun push(value: T) = stack.append(value).apply { size = stack.count() }
    fun peek(): T? = stack.first()?.value
    fun pop(): T? = when { stack.isEmpty() -> throw NoSuchElementException() ; else -> stack.removeAtIndex(0).apply { size = stack.count() } }
    fun contains(s: T?): Boolean = stack.contains(s)
    override fun toString(): String = stack.toString()
    /**
     * returns this stack as a string with each element appended to the end of the string
     */
    fun toStringConcat(): String {
        val result = StringBuilder()
        val dq = stack.iterator()
        while (dq.hasNext()) {
            result.append(dq.next())
        }
        return result.toString()
    }

    fun iterator() = stack.iterator()
    fun forEach(action: (T?) -> Unit) = stack.forEach(action)
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
