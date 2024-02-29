package com.robbiebowman.gpt

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty


data class SimpleField @JsonCreator constructor(
    @JsonProperty("type")
    override val type: String,
    @JsonProperty("description")
    override val description: String? = null
) : JsonField
