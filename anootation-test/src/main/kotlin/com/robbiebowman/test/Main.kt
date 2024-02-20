package com.robbiebowman.test


import com.azure.ai.openai.OpenAIClientBuilder
import com.azure.ai.openai.models.*
import com.azure.core.credential.KeyCredential
import com.azure.core.util.BinaryData
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.robbiebowman.gpt.GptDescription
import com.robbiebowman.gpt.GptTools
import com.robbiebowman.gpt.ObjectField
import java.util.*


fun main() {
    val openaiKey = System.getenv("OPEN_AI_KEY")
    val deploymentOrModelId = "gpt-4"
    val client = OpenAIClientBuilder()
        .credential(KeyCredential(openaiKey))
        .buildClient()
    val chatMessages = listOf(
        ChatRequestSystemMessage("You are a helpful assistant."),
        ChatRequestUserMessage("What sort of clothing should I wear today in Berlin?")
    )
    val toolDefinition: ChatCompletionsToolDefinition = ChatCompletionsFunctionToolDefinition(
        getFutureTemperatureFunctionDefinition()
    )
    val chatCompletionsOptions = ChatCompletionsOptions(chatMessages)
    chatCompletionsOptions.tools = listOf(toolDefinition)
    val chatCompletions = client.getChatCompletions(deploymentOrModelId, chatCompletionsOptions)
    val choice = chatCompletions.choices[0]
    // The LLM is requesting the calling of the function we defined in the original request
    if (choice.finishReason === CompletionsFinishReason.TOOL_CALLS) {
        val toolCall = choice.message.toolCalls[0] as ChatCompletionsFunctionToolCall
        val functionName = toolCall.function.name
        val functionArguments = toolCall.function.arguments
        println("Function Name: $functionName")
        println("Function Arguments: $functionArguments")

        // As an additional step, you may want to deserialize the parameters, so you can call your function
        val parameters = BinaryData.fromString(functionArguments).toObject(Weather::class.java)
        println("Location Name: " + parameters.locationName)
        println("Date: " + parameters.date)
        val functionCallResult = futureTemperature(parameters.locationName, parameters.date)
        val assistantMessage = ChatRequestAssistantMessage("")
        assistantMessage.toolCalls = choice.message.toolCalls

        // We include:
        // - The past 2 messages from the original request
        // - A new ChatRequestAssistantMessage with the tool calls from the original request
        // - A new ChatRequestToolMessage with the result of our function call
        val followUpMessages = Arrays.asList(
            chatMessages[0],
            chatMessages[1],
            assistantMessage,
            ChatRequestToolMessage(functionCallResult, toolCall.id)
        )
        val followUpChatCompletionsOptions = ChatCompletionsOptions(followUpMessages)
        val followUpChatCompletions = client.getChatCompletions(deploymentOrModelId, followUpChatCompletionsOptions)

        // This time the finish reason is STOPPED
        val followUpChoice = followUpChatCompletions.choices[0]
        if (followUpChoice.finishReason === CompletionsFinishReason.STOPPED) {
            println("Chat Completions Result: " + followUpChoice.message.content)
        }
    }
}

// In this example we ignore the parameters for our tool function
fun futureTemperature(locationName: String?, data: String?): String {
    return "-7 C"
}

fun getFutureTemperatureFunctionDefinition(): FunctionDefinition {
    val functionDefinition = FunctionDefinition("FutureTemperature")
    functionDefinition.description = "Get the future temperature for a given location and date."
    val parameters = ObjectField("", Weather_Props())
    functionDefinition.parameters = BinaryData.fromObject(parameters)
    return functionDefinition
}

@GptTools("")
class Weather {
    @GptDescription("The name of the location to get the future temperature for")
    val locationName: String? = null

    @GptDescription("The date to get the future temperature for. The format is YYYY-MM-DD.")
    val date: String? = null

    @GptDescription("Temperature unit. Can be either Celsius or Fahrenheit. Defaults to Celsius.")
    val unit: String? = null
}
