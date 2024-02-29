package com.robbiebowman.gpt

import com.fasterxml.jackson.annotation.JsonProperty

class ObjectField<T> (
    @JsonProperty("properties") val properties: T,
    @JsonProperty("description") override val description: String? = null
): JsonField {
    @JsonProperty("type")
    override val type = "object"
}