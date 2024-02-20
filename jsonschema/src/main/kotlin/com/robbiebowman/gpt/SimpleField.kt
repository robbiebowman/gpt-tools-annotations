package com.robbiebowman.gpt

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty


data class SimpleField @JsonCreator constructor(
    @JsonProperty("description")
    val description: String,
    @JsonProperty("type")
    val type: String
)
