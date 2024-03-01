package com.robbiebowman.gpt.fields

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty


internal data class SimpleField @JsonCreator constructor(
    @JsonProperty("type")
    override val type: String,
    @JsonProperty("description")
    override val description: String? = null
) : JsonField
