//package preprocessor.utils.core.classTools
//
///**
// * obtains the parent class of [objectA]
// *
// * @params objectA the current class
// * @return **null** if [objectA] is **null**
// *
// * [objectA] if [objectA] is a top level class
// *
// * otherwise the parent class of [objectA]
// */
//fun getDeclaringUpperLevelClassObject(objectA: Any?): Any? {
//    if (objectA == null) {
//        return null
//    }
//    val cls = objectA.javaClass ?: return objectA
//    val outerCls = cls.enclosingClass
//        ?: // this is top-level class
//        return objectA
//    // get outer class object
//    var outerObj: Any? = null
//    try {
//        val fields = cls.declaredFields
//        for (field in fields) {
//            if (field != null && field.type === outerCls
//                && field.name != null && field.name.startsWith("this$")
//            ) {
//                /*
//                `field.isAccessible = true` does not work with a Security Manager
//                java.security.AccessControlException: access denied
//                ("java.lang.reflect.ReflectPermission" "suppressAccessChecks")
//                */
//                field.isAccessible = true
//                outerObj = field.get(objectA)
//                break
//            }
//        }
//    } catch (e: Exception) {
//        e.printStackTrace()
//    }
//
//    return outerObj
//}
