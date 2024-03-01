package com.robbiebowman.gpt.fields

import com.fasterxml.jackson.annotation.JsonProperty

internal class ObjectField<T> (
    @JsonProperty("properties") val properties: T,
    @JsonProperty("description") override val description: String? = null
): JsonField {
    @JsonProperty("type")
    override val type = "object"
}