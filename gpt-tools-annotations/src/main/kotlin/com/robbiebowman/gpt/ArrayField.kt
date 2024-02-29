package com.robbiebowman.gpt

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSType

class ArrayField<T : JsonField>(
    itemField: T,
    @JsonProperty("description") override val description: String? = null
): JsonField {
    @JsonProperty("type")
    override val type = "array"

    @JsonProperty("items")
    val items = itemField

}