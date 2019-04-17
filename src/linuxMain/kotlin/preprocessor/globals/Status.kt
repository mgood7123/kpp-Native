package preprocessor.globals

/**
 * status variables
 */
class Status {
    /**
     * this is used by [testFile][preprocessor.utils.Sync.testFile]
     */
    var currentFileContainsPreprocessor: Boolean = false

    /*
        TODO: implement file cache
        var currentFileIsCashed: Boolean = false
        var cachedFileContainsPreprocessor: Boolean = false
    */

    /**
     *
     */
    var firstLine: Boolean = true
    /**
     *
     */
    var currentMacroExists: Boolean = false
    /**
     *
     */
    var abortOnComplete: Boolean = true

}
