package preprocessor.utils.extra

import preprocessor.utils.core.abort

/**
 * extracts the arguments of a function and puts them into an array
 *
 * @returns an array of parameters
 */
fun extractArguments(arg: String): MutableList<String>? {
    fun filterSplit(arg: String, ex: Balanced, b: Balanced.BalanceList): MutableList<String> {
        val arguments: MutableList<String> = mutableListOf()
        if (ex.containsL(arg, b)) {
            if (ex.isBalancedSplit(arg, b, ',')) {
                ex.info()
                if (ex.splitterCount == 0) {
                    arguments.add(arg)
                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + arguments[0])
                } else {
                    var s: String = arg.substring(0, ex.splitterLocation[0]).trimStart()
                    arguments.add(s)
                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + arguments[0])
                    var i = 0
                    while (i < ex.splitterLocation.lastIndex) {
                        s = arg.substring(ex.splitterLocation[i] + 1, ex.splitterLocation[i + 1]).trimStart()
                        arguments.add(s)
                        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + arguments[i])
                        i++
                    }
                    s = arg.substring(ex.splitterLocation[i] + 1, ex.index).trimStart()
                    arguments.add(s)
                    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + arguments[i])
                }
            } else {
                ex.info()
                abort(preprocessor.base.globalVariables.depthAsString() + "unBalanced code")
            }
        } else if (ex.containsR(arg, b)) {
            // unBalanced
            abort(preprocessor.base.globalVariables.depthAsString() + "unBalanced code")
        } else {
            val a: MutableList<String> = arg.split(',').toMutableList()
            // next, remove whitespaces from the start and end of each index string
            var i = 0
            while (i <= a.lastIndex) {
                val s: String = a[i].trimStart().trimEnd()
                arguments.add(s)
                i++
            }
        }
        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "arguments List = $arguments")
        return arguments
    }
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "extracting arguments for $arg")
    // first, determine the positions of all tokens
    val balance = Balanced.BalanceList()
    balance.addPair('(', ')')
    balance.addPair('{', '}')
    balance.addPair('[', ']')
    val ex = Balanced()
    return filterSplit(arg, ex, balance)
}
