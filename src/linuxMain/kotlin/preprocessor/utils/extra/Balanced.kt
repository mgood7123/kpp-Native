package preprocessor.utils.extra

/**
 *
 * a class for detecting balanced brackets
 *
 * cant be bothered documenting this
 *
 * modified from the original rosetta code in the **See Also**
 *
 * @see <a href="https://rosettacode.org/wiki/Balanced_brackets#Kotlin">Balanced Brackets</a>
 */
@Suppress("MemberVisibilityCanBePrivate")
class Balanced {
    /**
     *
     */
    class BalanceList {
        /**
         *
         */
        var l: MutableList<Char> = mutableListOf()
        /**
         *
         */
        var r: MutableList<Char> = mutableListOf()

        /**
         *
         */
        fun addPair(l: Char, r: Char) {
            this.l.add(l)
            this.r.add(r)
        }
    }

    /**
     *
     */
    var start: MutableList<Int> = mutableListOf()
    /**
     *
     */
    var end: MutableList<Int> = mutableListOf()
    /**
     *
     */
    var index: Int = 0
    /**
     *
     */
    var countLeft: Int = 0  // number of left brackets so far unmatched
    /**
     *
     */
    var splitterCount: Int = 0
    /**
     *
     */
    var splitterLocation: MutableList<Int> = mutableListOf()
    /**
     *
     */
    var lastRegisteredLeftHandSideBalancer: Char = ' '
    /**
     *
     */
    var lastRegisteredRightHandSideBalancer: Char = ' '
    /**
     *
     */
    var lastCheckString: String = ""

    /**
     *
     */
    fun isBalanced(s: String, balancerLeft: Char, balancerRight: Char): Boolean {
        lastCheckString = s
        lastRegisteredLeftHandSideBalancer = balancerLeft
        lastRegisteredRightHandSideBalancer = balancerRight
        start
        end
        if (s.isEmpty()) return true
        for (c in s) {
            if (c == lastRegisteredLeftHandSideBalancer) {
                countLeft++
                if (countLeft == 1) start.add(index)
            } else if (c == lastRegisteredRightHandSideBalancer) {
                if (countLeft == 1) end.add(index + 1)
                if (countLeft > 0) countLeft--
                else return false
            }
            index++
        }
        return countLeft == 0
    }

    /**
     *
     */
    fun isBalancerR(c: Char, balance: BalanceList): Boolean {
        balance.r.forEach {
            if (c == it) return true
        }
        return false
    }

    /**
     *
     */
    fun isBalancerL(c: Char, balance: BalanceList): Boolean {
        balance.l.forEach {
            if (c == it) return true
        }
        return false
    }

    /**
     *
     */
    fun containsL(c: String, balance: BalanceList): Boolean {
        balance.l.forEach {
            if (c.contains(it)) return true
        }
        return false
    }

    /**
     *
     */
    fun containsR(c: String, balance: BalanceList): Boolean {
        balance.r.forEach {
            if (c.contains(it)) return true
        }
        return false
    }

    /**
     *
     */
    fun isBalancedSplit(s: String, balancer: BalanceList, Splitter: Char): Boolean {
        lastCheckString = s
        lastRegisteredLeftHandSideBalancer = balancer.l[balancer.l.lastIndex]
        lastRegisteredRightHandSideBalancer = balancer.r[balancer.r.lastIndex]
        if (s.isEmpty()) return true
        for (c in s) {
            if (countLeft == 0) if (c == Splitter) {
                splitterCount++
                splitterLocation.add(index)
            }
            if (isBalancerL(c, balancer)) {
                countLeft++
                if (countLeft == 1) start.add(index)
            } else if (isBalancerR(c, balancer)) {
                if (countLeft == 1) end.add(index + 1)
                if (countLeft > 0) countLeft--
                else return false
            }
            index++
        }
        return countLeft == 0
    }

    /**
     *
     */
    fun extractText(text: String): String {
        if (isBalanced(text, '(', ')')) {
            if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "text : " + text.substring(start[0], end[0]))
            return text.substring(start[0], end[0])
        }
        return text
    }

    /**
     *
     */
    fun info() {
        if (preprocessor.base.globalVariables.flags.debug) {
            println(preprocessor.base.globalVariables.depthAsString() + "last check string  = $lastCheckString")
            println(preprocessor.base.globalVariables.depthAsString() + "left balancer      = $lastRegisteredLeftHandSideBalancer")
            println(preprocessor.base.globalVariables.depthAsString() + "right balancer     = $lastRegisteredRightHandSideBalancer")
            println(preprocessor.base.globalVariables.depthAsString() + "start index        = $start")
            println(preprocessor.base.globalVariables.depthAsString() + "end index          = $end")
            println(preprocessor.base.globalVariables.depthAsString() + "current index       = $index")
            println(preprocessor.base.globalVariables.depthAsString() + "unmatched brackets = $countLeft")
            println(preprocessor.base.globalVariables.depthAsString() + "splitter count     = $splitterCount")
            println(preprocessor.base.globalVariables.depthAsString() + "splitter location  = $splitterLocation")
        }
    }
}
