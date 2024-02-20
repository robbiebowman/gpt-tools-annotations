package com.robbiebowman.gpt

import com.google.devtools.ksp.symbol.KSType
import kotlin.reflect.KType

fun getJsonType(type: KSType): String {
    return when(type.declaration.simpleName.asString()) {
        "String" -> "string"
        "Long", "Int", "Short" -> "int"
        "Double", "Float" -> "number"
        "Boolean" -> "boolean"
        else -> "object"
    }
}