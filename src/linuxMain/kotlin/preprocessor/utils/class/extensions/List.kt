package preprocessor.utils.`class`.extensions

fun <T> MutableList<T>.lastIndex(): T = this[this.size - 1]

fun <T> MutableList<T>.addCache(new: T, max: Int) {
    if (max == size) {
        this.remove(this.last())
        this.add(new)
    }
}