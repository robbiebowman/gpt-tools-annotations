package com.robbiebowman.test

import com.fasterxml.jackson.annotation.JsonProperty
import com.robbiebowman.gpt.GptDescription
import com.robbiebowman.gpt.GptTools
import com.robbiebowman.gpt.SimpleField

@GptTools("Testy test")
data class MyTestClass(
    @GptDescription("The brand of coffee")
    val coffee: String,

    @GptDescription("Coffee weight")
    val weight: Double,

    @GptDescription("Obj description")
    val anotherObj: NestedClass
)

data class NestedClass(
    @GptDescription("Nested description") val nestedValue: Double
)
