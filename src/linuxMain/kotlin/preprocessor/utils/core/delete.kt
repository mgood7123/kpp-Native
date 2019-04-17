//package preprocessor.utils.core
//
//import java.io.File
//
///**
// * deletes **src**
// *
// * [abort]s on failure
// */
//fun delete(src: File) {
//    if (!src.exists()) {
//        if (preprocessor.base.globalVariables.flags.debug) println(preprocessor.base.globalVariables.depthAsString() + "deletion of ${src.path} failed: file or directory does not exist")
//    }
//    if (!src.delete()) {
//        abort(preprocessor.base.globalVariables.depthAsString() + "deletion of \"${src.path}\" failed")
//    }
//}
