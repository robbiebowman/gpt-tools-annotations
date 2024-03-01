package com.robbiebowman.gpt

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType

internal fun getJsonType(type: KSType, resolver: Resolver): String {
    val numberType = resolver.getClassDeclarationByName(
        resolver.getKSNameFromString("kotlin.Number")
    )!!.asType(emptyList())
    val iterableType = resolver.getClassDeclarationByName(
        resolver.getKSNameFromString("kotlin.collections.Iterable")
    )!!.asStarProjectedType()
    return when {
        type.isAssignableFrom<String>(resolver) -> {
            "string"
        }

        type.isAssignableFrom<Long>(resolver) ||
                type.isAssignableFrom<Int>(resolver) ||
                type.isAssignableFrom<Short>(resolver) ||
                type.isAssignableFrom<Byte>(resolver) -> {
            "int"
        }

        numberType.isAssignableFrom(type) -> {
            "number"
        }

        type.isAssignableFrom<Boolean>(resolver) -> {
            "boolean"
        }

        iterableType.isAssignableFrom(type) -> {
            "array"
        }

        else -> {
            "object"
        }
    }
}

internal inline fun <reified T> KSType.isAssignableFrom(resolver: Resolver): Boolean {
    val classDeclaration = requireNotNull(resolver.getClassDeclarationByName<T>()) {
        "Unable to resolve ${KSClassDeclaration::class.simpleName} for type ${T::class.simpleName}"
    }
    return isAssignableFrom(classDeclaration.asStarProjectedType())
}