package preprocessor.test

import preprocessor.core.Macro

/**
 * initializes the Macro list
 * @return a new Macro list
 */
fun init(): MutableList<Macro> {
    return mutableListOf<Macro>(Macro())
}