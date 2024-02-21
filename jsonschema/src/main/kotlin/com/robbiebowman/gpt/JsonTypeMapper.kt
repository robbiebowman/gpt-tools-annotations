package com.robbiebowman.gpt

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType

fun getJsonType(type: KSType, resolver: Resolver): String {
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

        type.isAssignableFrom<Double>(resolver) ||
                type.isAssignableFrom<Float>(resolver) -> {
            "number"
        }

        type.isAssignableFrom<Boolean>(resolver) -> {
            "boolean"
        }

        else -> {
            "object"
        }
    }
}

inline fun <reified T> KSType.isAssignableFrom(resolver: Resolver): Boolean {
    val classDeclaration = requireNotNull(resolver.getClassDeclarationByName<T>()) {
        "Unable to resolve ${KSClassDeclaration::class.simpleName} for type ${T::class.simpleName}"
    }
    return isAssignableFrom(classDeclaration.asStarProjectedType())
}