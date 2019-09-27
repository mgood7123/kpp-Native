package preprocessor.utils.core.algorithms
//
//class BinaryTree<T>() {
//    private var tree = Tree<T>()
//    var value: T?
//        get() = tree.value
//        set(value) {
//            tree.value = value
//        }
//    var left: BinaryTree<T>? = null
//        get() {
//            if (tree.child == null) return null
//            val new = BinaryTree<T>()
//            new.tree = tree.child!![0]
//            return new
//        }
//        set(value) {
//            tree.child!![0].value = value!!.value
//        }
//    var right: BinaryTree<T>? = null
//        get() {
//            if (tree.child == null) return null
//            if (tree.child!!.size == 1) return null
//            val new = BinaryTree<T>()
//            new.tree = tree.child!![1]
//            return new
//        }
//        set(value) {
//            tree.child!![1].value = value!!.value
//        }
//
//    fun add(value: T) {
//        if (left == null) {
//            left = BinaryTree<T>()
//            println("setting left")
//            left!!.value = value
//            println("set left")
//        } else if (right == null) {
//            right = BinaryTree<T>()
//            println("setting right")
//            right!!.value = value
//            println("set right")
//        }
//    }
//
//    override fun toString(): String = tree.toString()
//}
