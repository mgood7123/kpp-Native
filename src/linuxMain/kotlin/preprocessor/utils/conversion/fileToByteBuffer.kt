//package preprocessor.utils.conversion
//
//import java.io.File
//import java.io.RandomAccessFile
//import java.nio.ByteArray
//
///**
// * converts a [File] into a [ByteArray]
// * @return the resulting conversion
// * @see stringToByteArray
// */
//fun fileToByteArray(f: File): ByteArray {
//    val file = RandomAccessFile(f, "r")
//    val fileChannel = file.channel
//    val buffer = ByteArray.allocate(fileChannel.size().toInt())
//    fileChannel.read(buffer)
//    buffer.flip()
//    return buffer
//}
