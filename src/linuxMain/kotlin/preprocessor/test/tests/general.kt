package preprocessor.test.tests

import preprocessor.test.init
import preprocessor.test.utils.expect
import preprocessor.utils.core.abort
import preprocessor.utils.extra.parse

/**
 * tests general capabilities
 */
fun general() {
    val m = init()
    parse(
        "#define a(b) b\n" +
                "#define c() d c\n" +
                "#define d Once\n",
        m
    )
    expect("a(    c()    )", "Once c", m)

    parse("#define c() d d", m)
    expect("a(    c()    )", "Once Once", m)
    expect("d", "Once", m)

    parse(
        "#define a(b, y) b y\n" +
                "#define c() d c\n" +
                "#define e() d c\n",
        m
    )
    expect("a(    c()    ,    e()    )", "Once c Once c", m)

    parse("#define a b", m)
    expect("a", "b", m)

    parse("#define f g", m)
    expect("f", "g", m)

    parse("#define x y", m)
    expect("x", "y", m)
    expect("a", "b", m)
    expect("aa", "aa", m)
    expect("a a", "b b", m)
    expect("a N a", "b N b", m)

    parse("#define a(x) {x}", m)
    expect("a()k", "{}k", m)

    parse("#define a(x) {b()x}", m)
    parse("#define b(x) {x}", m)
    expect("a()k", "{{}}k", m)

    parse("#define a(x) {b() x}", m)
    parse("#define b(x) { x}", m)
    expect("a()k", "{{ } }k", m)

    parse("#define a(x) [b() x]", m)
    parse("#define b(x) [1 x", m)
    expect("a(2)", "[[1 2]", m)

    parse("#define b(x) [1 x]", m)
    expect("a(2)", "[[1 ] 2]", m)
    parse("#define b(x) [1 x ]", m)
    expect("a(2)", "[[1 ] 2]", m)

}