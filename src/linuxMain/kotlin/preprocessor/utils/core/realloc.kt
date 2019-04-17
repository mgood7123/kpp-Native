@file:Suppress("unused")
package preprocessor.utils.core

import preprocessor.core.Macro

// NOT AVAILABLE IN KOTLIN-NATIVE
//
//import preprocessor.utils.core.classTools.instanceChain
//import preprocessor.utils.core.classTools.chain
//
//
///**
// * reallocates a [MutableList][kotlin.collections.MutableList] to the specified size
// *
// * this attempts to handle all types supported by [MutableList][kotlin.collections.MutableList] in the
// * initialization of a new element
// *
// * uses unsigned integers that are experimental: [UByte], [UInt], [ULong], [UShort]
// * @param v the list to resize
// * @param size the desired size to resize to
// * @sample reallocTest
// */
//@UseExperimental(ExperimentalUnsignedTypes::class)
//@Suppress("UNCHECKED_CAST")
//fun <E> realloc(v: kotlin.collections.MutableList<E?>, a : Any?, size: Int, isNullable: Boolean = true) {
//    while (v.size != size) {
//        if (size > v.size) {
//            v.add(
//                run {
//                    if (a!!::class.javaPrimitiveType != null) {
//                        if (isNullable) null
//                        /** copied from
//                         * /.../kotlin-stdlib-1.3.21-sources.jar!/kotlin/Primitives.kt
//                         * /.../kotlin-stdlib-1.3.21-sources.jar!/kotlin/Boolean.kt
//                         *
//                         * unsigned integers are experimental: [UByte], [UInt], [ULong], [UShort]
//                         */
//                        else when (a!!) {
//                            is Byte -> 0
//                            is UByte -> 0U
//                            is Short -> 0
//                            is UShort -> 0U
//                            is Int -> 0
//                            is UInt -> 0U
//                            is Long -> 0L
//                            is ULong -> 0UL
//                            is Float -> 0.0F
//                            is Double -> 0.0
//                            is Char -> java.lang.Character.MIN_VALUE // null ('\0') as char
//                            is Boolean -> false
//                            else -> abort(preprocessor.base.globalVariables.depthAsString() + "unknown non-nullable type: ${a!!::class.javaPrimitiveType}")
//                        }
//                    } else {
//                        /*
//                        `::class.isInner` does not work with a Security Manager
//                        Exception in thread "main" java.lang.IllegalStateException: No BuiltInsLoader implementation was
//                        found. Please ensure that the META-INF/services/ is not stripped from your application and that
//                        the Java virtual machine is not running under a security manager
//                        */
//                        if (a!!::class.isInner) instanceChain(chain(a!!))
//                        else a!!::class.java.newInstance()
//                    }
//                } as E
//            )
//        } else {
//            v.remove(v.last())
//        }
//    }
//}
//
///**
// * reallocates a [MutableList][kotlin.collections.MutableList] to the specified size
// *
// * this attempts to handle all types supported by [MutableList][kotlin.collections.MutableList] in the
// * initialization of a new element
// *
// *  uses unsigned integers that are experimental: [UByte], [UInt], [ULong], [UShort]
// * @param v the list to resize
// * @param size the desired size to resize to
// * @sample reallocTest
// */
//@UseExperimental(ExperimentalUnsignedTypes::class)
//@Suppress("UNCHECKED_CAST")
//fun <E> realloc(v: kotlin.collections.MutableList<E>, size: Int): Unit = realloc(
//    v = v as kotlin.collections.MutableList<E?>,
//    a = v[0],
//    size = size,
//    isNullable = false
//)
//
///**
// * @see reallocTest
// */
//private class A {
//    inner class B {
//        inner class C {
//            var empty: Int = 0
//        }
//
//        var empty: Int = 0
//        var a: MutableList<C>
//
//        init {
//            a = mutableListOf(C())
//        }
//    }
//
//    var a: MutableList<B>
//
//    init {
//        a = mutableListOf(B())
//    }
//
//    /**
//     * test variable
//     */
//    var empty: Int = 0
//}
//
//fun reallocTest() {
//    val f = mutableListOf<A>()
//    val ff = mutableListOf<Int>()
//    val fff = mutableListOf<Double?>()
//    f.add(A()); f[0].empty = 5
//    ff.add(5)
//    fff.add(5.5)
//
//    realloc(f, 5)
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "f[0].empty = ${f[0].empty}")
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "f[4].empty = ${f[4].empty}")
//
//    f[0].a[0].empty = 88
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "f[0].a[0].empty = ${f[0].a[0].empty}")
//    f[4].a[0].empty = 88
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "f[4].a[0].empty = ${f[4].a[0].empty}")
//
//    realloc(f[0].a, 5)
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "f[0].a[4].empty = ${f[0].a[4].empty}")
//    realloc(f[4].a, 5)
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "f[4].a[4].empty = ${f[4].a[4].empty}")
//
//    realloc(ff, 5)
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "ff[0] = ${ff[0]}")
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "ff[4] = ${ff[4]}")
//    realloc(fff, 5)
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "fff[0] = ${fff[0]}")
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "fff[4] = ${fff[4]}")
//    abort()
//}

fun realloc(m: MutableList<Macro.MacroInternal>, newSize: Int) {
    m.add(Macro().MacroInternal())
    m[0].size = newSize
}

fun realloc(m: MutableList<Macro>, newSize: Int) {
    m.add(Macro())
    m[0].size = newSize
}