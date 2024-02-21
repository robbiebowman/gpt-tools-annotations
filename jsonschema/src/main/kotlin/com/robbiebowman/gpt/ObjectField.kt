package com.robbiebowman.gpt

import com.fasterxml.jackson.annotation.JsonProperty

class ObjectField<T>(
    @JsonProperty("properties") val properties: T,
    @JsonProperty("description") val description: String? = null
) {
    @JsonProperty("type")
    val type = "object"
}