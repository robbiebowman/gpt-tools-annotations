package com.robbiebowman.gpt

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Nullability

class FileCreator(private val resolver: Resolver) {

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
        file.appendText("import com.robbiebowman.gpt.SimpleField\n")
        file.appendText("import com.robbiebowman.gpt.ObjectField\n\n")
        file.appendText("class ${className}{\n")
        props.forEach { property ->
            val resolvedType = property.type.resolve()
            val description = resolvedType.declaration.annotations
                .firstOrNull {
                    it.annotationType
                        .resolve()
                        .isAssignableFrom<GptDescription>(resolver)}
                ?.arguments?.firstOrNull()?.value?.let{"\"${it}\""} ?: "null"
            val jsonType = getJsonType(resolvedType, resolver)
            if (jsonType == "object") {
                val newProps = PropertyDefinition.getProps(resolvedType.declaration as KSClassDeclaration)
                val kotlinType =
                    createInputClass(codeGenerator, resolvedType.declaration, newProps, depth + 1)
                file.appendText("""    val ${property.name} = ObjectField($kotlinType(), $description)""")
                file.appendText("\n")
            } else {
                file.appendText("""    val ${property.name} = SimpleField("$jsonType", $description)""")
                file.appendText("\n")
            }
        }
        file.appendText("}\n")
        file.close()
        return "${packageName}.${className}"
    }

    fun createFunctionDefinition(codeGenerator: CodeGenerator, functionDeclaration: KSFunctionDeclaration, description: String, parentPropClass: String) {
        val packageName = functionDeclaration.containingFile!!.packageName.asString()
        val name = functionDeclaration.simpleName.getShortName()
        val file = codeGenerator.createNewFile(Dependencies(true, functionDeclaration.containingFile!!), packageName, name)
        file.appendText("package $packageName\n\n")
        file.appendText("import com.robbiebowman.gpt.ObjectField\n")
        file.appendText("import com.azure.ai.openai.models.FunctionDefinition\n")
        file.appendText("import com.azure.core.util.BinaryData\n\n")
        file.appendText("val ${name}FunctionDefinition = FunctionDefinition(\"$name\").apply {\n")
        file.appendText("    description = \"$description\"\n")
        file.appendText("    parameters = BinaryData.fromObject(ObjectField($parentPropClass()))\n")
        file.appendText("}\n")
        file.close()
    }
}
