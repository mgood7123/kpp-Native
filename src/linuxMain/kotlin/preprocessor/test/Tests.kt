package preprocessor.test

import preprocessor.test.tests.*
import preprocessor.utils.core.abort

/**
 * holds all the tests for this library
 */
class Tests {
    private fun begin(name: String = "Tests", message: String = "starting $name"): Unit =
        println(message)

    private fun end(name: String = "Tests", message: String = "$name finished"): Unit =
        println(message)

    /**
     * if this value is true, the function will abort if all tests pass
     */
    var abortOnComplete: Boolean = false

    fun doAll() {
        begin()
        general()
        stringize()
        selfReferencing()
        if (abortOnComplete) {
            end()
//            abort("All Tests Passed")
        }
    }
}