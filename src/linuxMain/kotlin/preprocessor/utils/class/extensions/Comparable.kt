@file:Suppress("unused")

package preprocessor.utils.`class`.extensions

infix fun <T: Comparable<T>> T.isGreaterThan(i: T) = this > i
infix fun <T: Comparable<T>> T.isGreaterThanOrEqualTo(i: T) = this >= i
infix fun <T: Comparable<T>> T.isLessThan(i: T) = this < i
infix fun <T: Comparable<T>> T.isLessThanOrEqualTo(i: T) = this <= i
infix fun <T: Comparable<T>> T.isEqualTo(i: T) = this == i
infix fun <T: Comparable<T>> T.isNotEqualTo(i: T) = this != i
