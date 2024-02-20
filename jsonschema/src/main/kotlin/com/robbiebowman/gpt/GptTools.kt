package com.robbiebowman.gpt

@Target(AnnotationTarget.CLASS, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
annotation class GptTools(val description: String)