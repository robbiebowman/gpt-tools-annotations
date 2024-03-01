package com.robbiebowman.gpt.fields

import com.fasterxml.jackson.annotation.JsonProperty

class ArrayField<T : JsonField>(
    itemField: T,
    @JsonProperty("description") override val description: String? = null
): JsonField {
    @JsonProperty("type")
    override val type = "array"

    @JsonProperty("items")
    val items = itemField

}