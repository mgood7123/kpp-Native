package preprocessor.test.tests

import preprocessor.test.init
import preprocessor.test.utils.expect
import preprocessor.utils.extra.parse

/**
 * tests self referencing macros
 */
fun selfReferencing() {
    val m = init()
    parse("#define a a", m)
    expect("a", "a", m)
    parse("#define d l k j\n", m)
    expect("d", "l k j", m)
    parse("#define a(x) b() x\n" +
                "#define b(x) c() x\n" +
                "#define c(x) d a() x\n", m
    )
    expect("a(\"3\" \"2\" \"1\" \"x\")", "l k j a() \"3\" \"2\" \"1\" \"x\"", m)
    parse("#define x(x) x\n", m)
    expect("x(a(\"3\" \"2\" \"1\" \"x\"))", "l k j a() \"3\" \"2\" \"1\" \"x\"", m)
}