package preprocessor.utils.core.algorithms

@Suppress("unused")
class LinkedList<T> : Iterable<T> {

    constructor() {}
    constructor(vararg item: T) {
        item.forEach {
            appendLast(it)
        }
    }

    constructor(convert: (t: T) -> T, vararg item: T) {
        item.forEach {
            appendLast(convert(it))
        }
    }

    constructor(isIterable: Boolean, vararg item: T) {
        if (isIterable) {
            LinkedList<T>().also { l ->
                item.forEach {
                    val item = (it as Iterable<T>).iterator()
                    while (item.hasNext()) l.appendLast(item.next())
                }
            }
        }
    }

    /**
     * @sample usage
     */
    constructor(arrayIterator: (t: T, ACTION: (Any?) -> Int) -> Unit, ACTION: (item: Any?) -> T, vararg item: T) {
        item.forEach {
            arrayIterator(it) { item -> appendLast(ACTION(item)) }
        }
    }

    private fun usage() {
        val x = LinkedList(
            { t, ACTION -> t.forEach { ACTION(it) } },  // arrayIterator
            { (it as Char).toString()},                 // ACTION
            "hi", "bye"                          // item
        )
    }

    inner class Node(value: T) {
        var value: T = value
        var next: Node? = null
        var previous: Node? = null
    }

    private var head: Node? = null
    fun isEmpty(): Boolean = head == null
    fun first(): Node? = head
    fun last(): Node? {
        var node = head
        if (node != null) {
            while (node?.next != null) {
                node = node.next
            }
            return node
        } else {
            return null
        }
    }
    fun isFirst(index: Int) = nodeAtIndex(index) == first()
    fun isLast(index: Int) = nodeAtIndex(index) == last()

    fun count(): Int {
        var node = head
        if (node != null) {
            var counter = 1
            while (node?.next != null) {
                node = node.next
                counter += 1
            }
            return counter
        } else {
            return 0
        }
    }

    fun nodeAtIndex(index: Int): Node? {
        if (index >= 0) {
            var node = head
            var i = index
            while (node != null) {
                if (i == 0) return node
                i -= 1
                node = node.next
            }
        }
        return null
    }

    operator fun get(i: Int) = when {
        isEmpty() || i > count() -> throw IndexOutOfBoundsException("index: $i, count: ${count()}")
        else -> nodeAtIndex(i)!!.value
    }
    operator fun set(i: Int, value: T) = when {
        isEmpty() || i > count() -> throw IndexOutOfBoundsException("index: $i, count: ${count()}")
        else -> nodeAtIndex(i)!!.value = value
    }

    var size = 0

    fun append(value: T) = appendLast(value)

    fun appendFirst(value: T): Int {
        var newNode = Node(value)
        var firstNode = this.first()
        if (firstNode != null) {
            newNode.next = firstNode
            firstNode.previous = newNode
            head = newNode
        } else {
            head = newNode
        }
        size++
        return indexOfNode(newNode)
    }

    fun appendLast(value: T): Int {
        var newNode = Node(value)
        var lastNode = this.last()
        if (lastNode != null) {
            newNode.previous = lastNode
            lastNode.next = newNode
        } else {
            head = newNode
        }
        size++
        return indexOfNode(newNode)
    }

    fun removeNode(node: Node): T {
        val prev = node.previous
        val next = node.next
        if (prev != null) {
            prev.next = next
        } else {
            head = next
        }
        next?.previous = prev
        node.previous = null
        node.next = null
        size--
        return node.value
    }

    fun removeLast() = last()?.let { removeNode(it) }

    fun removeAtIndex(index: Int) = nodeAtIndex(index)?.let { removeNode(it) }

    override fun toString(): String {
        var s = "["
        var node = head
        while (node != null) {
            s += "${node.value}"
            node = node.next
            if (node != null) {
                s += ", "
            }
        }
        return s + "]"
    }

    /**
     * returns this LinkedList as a string with each element appended to the end of the string
     */
    fun toStringConcat(): String {
        val result = StringBuilder()
        val dq = iterator()
        while (dq.hasNext()) {
            result.append(dq.next())
        }
        return result.toString()
    }

    fun contains(element: T): Boolean {
        var node = head
        while (node != null) {
            if (node.value == element) return true
            node = node.next
        }
        return false
    }

    fun indexOfNode(node: Node): Int {
        var currentNode = head
        var index = 0
        while (currentNode != null) {
            if (node == currentNode) return index
            index++
            currentNode = currentNode.next
        }
        throw NoSuchElementException()
    }

    override fun iterator(): Iterator<T> {
        return object : Iterator<T> {
            var node = head
            /**
             * Returns true if the iteration has more elements.
             *
             * @see next
             */
            override fun hasNext(): Boolean = node != null

            /**
             * Returns the next element in the iteration.
             *
             * NOTE: this always returns the **first** element when first called, not the **second** element
             *
             * @see hasNext
             */
            override fun next(): T {
                if (node == null) throw NoSuchElementException()
                val var0 = node!!.value
                node = node?.next
                return var0
            }
        }
    }

    fun clone(): LinkedList<T> = LinkedList<T>().also { l -> forEach { l.append(it) } }

    fun clear() {
        while (!isEmpty()) removeLast()
    }

    fun test() {
        val ll = LinkedList<String>()
        ll.append("John")
        println(ll)
        ll.append("Carl")
        println(ll)
        ll.append("Zack")
        println(ll)
        ll.append("Tim")
        println(ll)
        ll.append("Steve")
        println(ll)
        ll.append("Peter")
        println(ll)
        print("\n\n")
        println("first item: ${ll.first()?.value}")
        println("last item: ${ll.last()?.value}")
        println("second item: ${ll.first()?.next?.value}")
        println("penultimate item: ${ll.last()?.previous?.value}")
        println("\n4th item: ${ll.nodeAtIndex(3)?.value}")
        println("\nthe list has ${ll.count()} items")
    }
}