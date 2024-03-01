package com.robbiebowman.gpt.annotations

@Target(AnnotationTarget.CLASS, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
annotation class GptDescription(val description: String)
