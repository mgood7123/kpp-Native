package preprocessor.utils.`class`.extensions

import preprocessor.utils.core.algorithms.LinkedList
import preprocessor.utils.core.algorithms.Stack

/**
 * converts an [Array] into a [LinkedList]
 * @return the resulting conversion
 */
fun <T> Array<T>.toLinkedList() = LinkedList<T>().also { l -> this.forEach { l.appendLast(it) } }
/**
 * converts an [Array] into a [Stack]
 * @return the resulting conversion
 */
fun <T> Array<T>.toStack() = Stack<T>().also { s -> this.forEach { s.push(it) } }
