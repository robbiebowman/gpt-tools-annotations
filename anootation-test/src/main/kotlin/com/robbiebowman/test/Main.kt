package com.robbiebowman.test

import com.azure.ai.openai.OpenAIClientBuilder
import com.azure.ai.openai.models.*
import com.azure.core.credential.KeyCredential
import com.azure.core.util.BinaryData
import com.robbiebowman.gpt.GptDescription
import com.robbiebowman.gpt.GptTool
import java.util.*

fun main() {
    val openaiKey = System.getenv("OPEN_AI_KEY")
    val deploymentOrModelId = "gpt-4"
    val client = OpenAIClientBuilder()
        .credential(KeyCredential(openaiKey))
        .buildClient()
    val chatMessages = listOf(
        ChatRequestSystemMessage("You are a helpful assistant."),
        ChatRequestUserMessage("What sort of clothing should I wear during my winter trip to Berlin and Paris")
    )
    val toolDefinition: ChatCompletionsToolDefinition = ChatCompletionsFunctionToolDefinition(
        futureTemperatureFunctionDefinition
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
        val parameters = BinaryData.fromString(functionArguments).toObject(FutureTemperatureResult::class.java)
        println("Location Name: " + parameters.location)
        println("Date: " + parameters.monthAndYear)
        val functionCallResult = futureTemperature(parameters.location!!, parameters.monthAndYear!!)
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

@GptTool("Gets the temperature in the future")
fun futureTemperature(
    @GptDescription("The locations the user wants to know the temperature for")
    location: List<Location>,
    @GptDescription("The month and year the user wants to find the temperature for")
    monthAndYear: String
): String {
    return "-7 C"
}

@GptDescription("This will be overridden where it's parameter with a description annotation.")
data class Location(
    @property:GptDescription("The name of the location to get the future temperature for")
    val city: String? = null,

    @property:GptDescription("The 2 digit country code, as following the ISO 3166 A-2 standard.")
    val country: String? = null
)