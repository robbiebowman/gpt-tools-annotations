package com.robbiebowman.gpt.annotations

@Target(AnnotationTarget.FUNCTION)
annotation class GptTool(val description: String)