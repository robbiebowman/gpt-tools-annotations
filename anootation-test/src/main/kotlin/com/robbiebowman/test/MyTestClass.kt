package com.robbiebowman.test

import com.robbiebowman.gpt.GptDescription
import com.robbiebowman.gpt.GptTool

@GptTool("Gets a good coffee thingy")
fun getCoffee(inputs: MyTestClass) {

}

@GptDescription("Can I do this")
data class MyTestClass(
    @property:GptDescription("The brand of coffee")
    val coffee: String,

    @property:GptDescription("Coffee weight")
    val weight: Double,

    @property:GptDescription("Obj description")
    val anotherObj: NestedClass
)

data class NestedClass(
    @property:GptDescription("Nested description") val nestedValue: Double
)
