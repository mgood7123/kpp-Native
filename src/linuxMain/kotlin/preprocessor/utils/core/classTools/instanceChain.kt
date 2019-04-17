//package preprocessor.utils.core.classTools
//
///**
// * creates and returns an instance of the class given as the parameter to
// * [chain][preprocessor.utils.core.classTools.chain]
// *
// * this class can be outer or inner
// * @param chain the current chain, this must originate from a list returned
// *
// * by [chain][preprocessor.utils.core.classTools.chain]
// * @param index used internally to traverse the [chain]
// * @param debug if true, debug output will be printed
// * @sample instanceChainSample
// * @see preprocessor.utils.core.classTools.chain
// */
//fun instanceChain(chain: MutableList<Any>, index: Int = chain.lastIndex, debug: Boolean = false): Any {
//    return if (index == 0) {
//        chain[index]
//    } else {
//        if (debug) if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "chain[$index] = " + chain[index])
//        val outer = chain[index]
//        val toRun = Class.forName(chain[index].javaClass.name + "$" + chain[index - 1].javaClass.simpleName)
//        val ctor = toRun.getDeclaredConstructor(chain[index]::class.java)
//        val lowerCInstance = ctor.newInstance(outer)
//        if (debug) if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "lowerCInstance = " + lowerCInstance!!::class.java)
//        if (index == 1) lowerCInstance
//        else instanceChain(
//            chain = chain,
//            index = index - 1,
//            debug = debug
//        )
//    }
//}
//
//private fun instanceChainSample() {
//    class A {
//        inner class B {
//            inner class C {
//                var empty: Int = 0
//            }
//
//            var empty: Int = 0
//            var a: MutableList<C>
//
//            init {
//                a = mutableListOf(C())
//            }
//        }
//
//        var a: MutableList<B>
//
//        init {
//            a = mutableListOf(B())
//        }
//
//        var empty: Int = 0
//    }
//
//    val f = mutableListOf<A>()
//    f.add(A()) // this is required, as i do not know how to do accomplish this in the init block
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "f[0] = ${instanceChain(chain(f[0]))}")
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "f[0].a[0] = ${instanceChain(chain(f[0].a[0]))}")
//    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "f[0].a[0].a[0] = ${instanceChain(chain(f[0].a[0].a[0]))}")
//}