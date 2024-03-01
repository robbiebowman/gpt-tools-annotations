package com.robbiebowman.gpt

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference

internal data class PropertyDefinition(val name: String, val type: KSTypeReference, val annotations: Sequence<KSAnnotation>) {

    companion object {
        fun getProps(classDeclaration: KSClassDeclaration): List<PropertyDefinition> {
            return classDeclaration.getDeclaredProperties().map {
                PropertyDefinition(it.simpleName.getShortName(), it.type, it.annotations)
            }.toList()
        }

        fun getProps(function: KSFunctionDeclaration): List<PropertyDefinition> {
            return function.parameters.map {
                PropertyDefinition(it.name?.getShortName() ?: "", it.type, it.annotations)
            }
        }
    }
}