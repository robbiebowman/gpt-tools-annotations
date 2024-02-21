package com.robbiebowman.gpt

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Nullability

class FileCreator(private val resolver: Resolver) {

    fun createClass(codeGenerator: CodeGenerator, basedOn: KSClassDeclaration) {
        val packageName = basedOn.containingFile!!.packageName.asString()
        val className = "${basedOn.simpleName.asString()}_Input"
        val file = codeGenerator.createNewFile(Dependencies(true, basedOn.containingFile!!), packageName , className)
        file.appendText("package $packageName\n\n")
        file.appendText("//import HELLO\n\n")
        file.appendText("class ${className}{\n")
        basedOn.getDeclaredProperties().forEach { property ->
            val name = property.simpleName.getShortName()
            val typeName = StringBuilder(property.type.resolve().declaration.qualifiedName?.asString() ?: "<ERROR>")
            val typeArgs = property.type.element!!.typeArguments
            if (property.type.element!!.typeArguments.isNotEmpty()) {
                typeName.append("<")
                typeName.append(
                    typeArgs.joinToString(", ") {
                        val type = it.type?.resolve()
                        "${it.variance.label} ${type?.declaration?.qualifiedName?.asString() ?: "ERROR"}" +
                                if (type?.nullability == Nullability.NULLABLE) "?" else ""
                    }
                )
                typeName.append(">")
            }
            file.appendText("    private var $name: $typeName? = null\n")
            file.appendText("    internal fun with${name.replaceFirstChar { it.uppercase() } }($name: $typeName): $className {\n")
            file.appendText("        this.$name = $name\n")
            file.appendText("        return this\n")
            file.appendText("    }\n\n")
        }
        file.appendText("    internal fun build(): ${basedOn.qualifiedName!!.asString()} {\n")
        file.appendText("        return ${basedOn.qualifiedName!!.asString()}(")
        file.appendText(
            basedOn.getDeclaredProperties().map {
                "${it.simpleName.getShortName()}!!"
            }.joinToString(", ")
        )
        file.appendText(")\n")
        file.appendText("    }\n")
        file.appendText("}\n")
        file.close()
    }

    fun createInputClass(codeGenerator: CodeGenerator, basedOn: KSClassDeclaration, depth: Int): String {
        if (depth > 10) throw Exception("Too much nesting. Can't generate a schema")
        val packageName = basedOn.containingFile!!.packageName.asString()
        val className = "${basedOn.simpleName.asString()}_Props"
        val file = codeGenerator.createNewFile(Dependencies(true, basedOn.containingFile!!), packageName , className)
        file.appendText("package $packageName\n\n")
        file.appendText("import com.robbiebowman.gpt.SimpleField\n")
        file.appendText("import com.robbiebowman.gpt.ObjectField\n\n")
        file.appendText("class ${className}{\n")
        basedOn.getDeclaredProperties().forEach { property ->
            val resolvedType = property.type.resolve()
            val description = property.annotations
                .firstOrNull{it.shortName.asString() == "GptDescription"}
                ?.arguments?.firstOrNull()?.value as String? ?: ""
            val jsonType = getJsonType(resolvedType, resolver)
            val name = property.simpleName.getShortName()
            if (jsonType == "object") {
                val kotlinType = createInputClass(codeGenerator, resolvedType.declaration as KSClassDeclaration, depth+1)
                file.appendText("""    val $name = ObjectField("$description", $kotlinType())""")
                file.appendText("\n")
            } else {
                file.appendText("""    val $name = SimpleField("$description", "$jsonType")""")
                file.appendText("\n")
            }
        }
        file.appendText("}\n")
        file.close()
        return "${packageName}.${className}"
    }
}