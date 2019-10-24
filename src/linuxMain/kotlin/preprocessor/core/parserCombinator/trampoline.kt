tailrec fun <functionArgumentType, functionReturnType> Lamdatrampoline(
    function: ((functionArgumentType) -> functionReturnType)?,
    argument: functionArgumentType
): Any? = when (function) {
    null -> argument
    else -> {
        // Lamda CAN return a Unit, in which case no Pair was returned
        val r = function(argument)
        @Suppress("UNCHECKED_CAST") Lamdatrampoline<functionArgumentType, functionReturnType>(
            if (r is Unit)
                null
            else (r as Pair<((argument: functionArgumentType) -> functionReturnType)?, functionArgumentType>).first,
            if (r is Unit)
                argument
            else (r as Pair<((argument: functionArgumentType) -> functionReturnType)?, functionArgumentType>).second
        )
    }
}