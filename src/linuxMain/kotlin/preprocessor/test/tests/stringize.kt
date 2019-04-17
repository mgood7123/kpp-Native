package preprocessor.test.tests

import preprocessor.test.init
import preprocessor.test.utils.expect
import preprocessor.utils.extra.parse

/**
 * tests stringize
 */
fun stringize() {
    val m = init()
    parse(" #define s(x) #x -> x", m)
    expect("s(a)", "\"a\" -> a", m)

    parse("#define a b", m)
    expect("s(a b a)", "\"a b a\" -> b b b", m)
    expect("s(a    b    a)", "\"a b a\" -> b b b", m)
}