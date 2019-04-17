package preprocessor.utils.core

/**
 * a wrapper for Exception, Default message is **Aborted**
 *
 * if gradle is used, abort using the following
 *
 * import org.gradle.api.GradleException
 *
 * ...
 *
 * throw GradleException(e)
 */
fun abort(e: String = "Aborted"): Nothing {
    if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "Aborting with error: $e")
    else println("Aborting with error: $e")
    throw Exception(e).also {ex ->
        println("stack trace:").also {
            ex.printStackTrace()
        }
    }
}
