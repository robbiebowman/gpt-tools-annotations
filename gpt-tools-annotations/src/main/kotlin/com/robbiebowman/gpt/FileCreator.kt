package com.robbiebowman.gpt

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.robbiebowman.gpt.annotations.GptDescription

internal class FileCreator(private val resolver: Resolver) {

    fun createInputClass(
        codeGenerator: CodeGenerator,
        basedOn: KSDeclaration,
        props: List<PropertyDefinition>,
        depth: Int
    ): String {
        if (depth > 10) throw Exception("Too much nesting. Can't generate a schema")
        val packageName = basedOn.containingFile!!.packageName.asString()
        val className = "${basedOn.simpleName.asString()}_Props"
        val file = codeGenerator.createNewFile(Dependencies(true, basedOn.containingFile!!), packageName, className)
        file.appendText("package $packageName\n\n")
        file.appendText("import com.robbiebowman.gpt.fields.ArrayField\n")
        file.appendText("import com.robbiebowman.gpt.fields.SimpleField\n")
        file.appendText("import com.robbiebowman.gpt.fields.ObjectField\n\n")
        file.appendText("class ${className}{\n")
        props.forEach { property ->
            val resolvedType = property.type.resolve()
            val fieldDeclaration = getFieldDeclaration(codeGenerator, resolvedType, property.annotations, depth)
            file.appendText("    val ${property.name} = $fieldDeclaration\n")
        }
        file.appendText("}\n")
        file.close()
        return "${packageName}.${className}"
    }

    private fun getGptDescription(annotations: Sequence<KSAnnotation>): String? {
        return annotations
            .firstOrNull {
                it.annotationType
                    .resolve()
                    .isAssignableFrom<GptDescription>(resolver)
            }
            ?.arguments?.firstOrNull()?.value as String?
    }

    fun createFunctionDefinition(
        codeGenerator: CodeGenerator,
        functionDeclaration: KSFunctionDeclaration,
        description: String,
        parentPropClass: String
    ) {
        val packageName = functionDeclaration.containingFile!!.packageName.asString()
        val name = functionDeclaration.simpleName.getShortName()
        val functionName = "${name}FunctionDefinition"
        val file = codeGenerator.createNewFile(
            Dependencies(true, functionDeclaration.containingFile!!),
            packageName,
            functionName
        )
        file.appendText("package $packageName\n\n")
        file.appendText("import com.robbiebowman.gpt.fields.ObjectField\n")
        file.appendText("import com.azure.ai.openai.models.FunctionDefinition\n")
        file.appendText("import com.azure.core.util.BinaryData\n\n")
        file.appendText("val $functionName = FunctionDefinition(\"$name\").apply {\n")
        file.appendText("    description = \"$description\"\n")
        file.appendText("    parameters = BinaryData.fromObject(ObjectField($parentPropClass()))\n")
        file.appendText("}\n")
        file.close()
    }

    fun createFunctionReturn(codeGenerator: CodeGenerator, functionDeclaration: KSFunctionDeclaration) {
        val packageName = functionDeclaration.containingFile!!.packageName.asString()
        val name = functionDeclaration.simpleName.getShortName()
        val functionName = "${name.first().uppercase()}${name.drop(1)}Result"
        val parameters = functionDeclaration.parameters
        val file = codeGenerator.createNewFile(
            Dependencies(true, functionDeclaration.containingFile!!),
            packageName,
            functionName
        )
        file.appendText("package $packageName\n\n")
        parameters.forEach { param ->
            file.appendText("import ${param.type.resolve().declaration.qualifiedName?.asString()}\n")
        }
        file.appendText("\ndata class $functionName (\n")
        parameters.forEach { param ->
            val resolvedType = param.type.resolve()
            val isNullable = resolvedType.isMarkedNullable
            val typeArgs = resolvedType.arguments.mapNotNull{ it.type?.resolve()}.joinToString().let {
                if (it.isBlank()) "" else "<$it>"
            }
            val paramName = param.name?.asString()
            val paramType = resolvedType.declaration.simpleName.asString()
            val nullabilityTag = if (isNullable) "" else "? = null"
            file.appendText("    val $paramName: $paramType$typeArgs$nullabilityTag,\n")
        }
        file.appendText(")\n")
        file.close()
    }

    private fun getFieldDeclaration(codeGenerator: CodeGenerator, resolvedType: KSType, annotations: Sequence<KSAnnotation>, depth: Int): String {
        val description = (getGptDescription(annotations)
            ?: getGptDescription(resolvedType.declaration.annotations))
        val formattedDescription = description?.let { "\"${it}\"" } ?: "null"
        val jsonType = getJsonType(resolvedType, resolver)
        return when (jsonType) {
            "object" -> {
                val newProps = PropertyDefinition.getProps(resolvedType.declaration as KSClassDeclaration)
                val kotlinType =
                    createInputClass(codeGenerator, resolvedType.declaration, newProps, depth + 1)
                "ObjectField($kotlinType(), $formattedDescription)"
            }
            "array" -> {
                val listType = resolvedType.arguments.first().type!!.resolve()
                val itemsField = getFieldDeclaration(codeGenerator, listType, listType.annotations, depth + 1)
                "ArrayField($itemsField, $formattedDescription)"
            }
            else -> {
                "SimpleField(\"$jsonType\", $formattedDescription)\n"
            }
        }
    }
}
