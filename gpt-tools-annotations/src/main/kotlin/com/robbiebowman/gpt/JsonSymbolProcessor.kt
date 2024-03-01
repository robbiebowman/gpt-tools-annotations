package com.robbiebowman.gpt

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.robbiebowman.gpt.annotations.GptTool
import java.io.OutputStream

internal fun OutputStream.appendText(str: String) {
    this.write(str.toByteArray())
}

internal class BuilderProcessor(
    val codeGenerator: CodeGenerator
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val functions = resolver.getSymbolsWithAnnotation("com.robbiebowman.gpt.annotations.GptTool")
        val ret = functions.filter { !it.validate() }.toList()
        functions
            .filter { it.validate() }
            .forEach {
                it.accept(BuilderVisitor(resolver), Unit)
            }
        return ret
    }

    inner class BuilderVisitor(private val resolver: Resolver) : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val props = classDeclaration.getDeclaredProperties().map {
                PropertyDefinition(it.simpleName.getShortName(), it.type, it.annotations)
            }.toList()
            FileCreator(resolver).createInputClass(codeGenerator, classDeclaration, props, 1)
        }

        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
            val fileCreator = FileCreator(resolver)
            val props = function.parameters.map {
                PropertyDefinition(it.name?.getShortName() ?: "", it.type, it.annotations)
            }
            val description = function.annotations
                .firstOrNull {
                    it.annotationType
                        .resolve()
                        .isAssignableFrom<GptTool>(resolver)
                }?.arguments?.firstOrNull()?.value as String? ?: ""
            val topLevelClass = fileCreator.createInputClass(codeGenerator, function, props, 1)
            fileCreator.createFunctionDefinition(codeGenerator, function, description, topLevelClass)
            if (function.parameters.size > 1){
                fileCreator.createFunctionReturn(codeGenerator, function)
            }
        }
    }
}

internal class BuilderProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return BuilderProcessor(environment.codeGenerator)
    }
}
