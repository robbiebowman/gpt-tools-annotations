package com.robbiebowman.gpt

import com.fasterxml.jackson.annotation.JsonProperty

class ObjectField<T>(
    @JsonProperty("description") val description: String,
    @JsonProperty("properties") val properties: T
) {
    @JsonProperty("type")
    val type = "object"
}