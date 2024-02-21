package com.robbiebowman.gpt

@Target(AnnotationTarget.FUNCTION)
annotation class GptTool(val description: String)