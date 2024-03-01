# About
To understand what makes this tool useful, first try exporting 
one of your functions as a GPT Tool using the [Azure Open AI SDK](https://github.com/Azure/azure-sdk-for-java/blob/azure-ai-openai_1.0.0-beta.6/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/StreamingToolCall.java).
It's a pain in the neck. They make you convert your actual function's types into
JSON Schema compliant Java/Kotlin classes. And if your function has multiple
parameters, you've got to create another class to allow the tool's arguments to
be deserialized.

The annotation `@GptTool` in this library takes care of everything for you.

Put it above a function, build your project, and you will have a FunctionDefinition
that can be supplied to the Azure Open AI SDK, plus a `<myFunc>Result` class
that you can deserialize the tool call arguments into.

You can also help GPT understand your functions with optional `@GptDescription` annotations
above your function's types and parameters.

# Usage

For a fully working example, check out [the sample code](example/src/main/kotlin/com/robbiebowman/test/Main.kt), which 
itself is a modification of Azure Open AI SDK's official [Tools example](https://github.com/Azure/azure-sdk-for-java/blob/azure-ai-openai_1.0.0-beta.6/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/ChatCompletionsFunctionCall.java).

Firstly, I'll put a `@GptTool` annotation above the function you'd like to turn into a GPT Tool.
I'll also label the arguments with useful descriptions with `@GptDescriotion`.
```kotlin
@GptTool("Gets the temperature in the future")
fun futureTemperature(
    @GptDescription("The locations the user wants to know the temperature for")
    location: List<Location>,
    @GptDescription("The month and year the user wants to find the temperature for")
    monthAndYear: String
): String {
    return "-7 C"
}
```

Now I'll build the project.

You'll find I now have two new classes:
1. `futureTemperatureFunctionDefinition` which extends `com.azure.ai.openai.models.FunctionDefinition`
2. `FutureTemperatureResult` which has my two parameters `location` and `monthAndYear`

I now supply the function definition to the Azure Open AI SDK and start chatting.
```kotlin
val toolDefinition = ChatCompletionsFunctionToolDefinition(
    futureTemperatureFunctionDefinition
)
```

Once the chat results in a function call, I can deserialize the arguments and call the very same function
I annotated with `@GptTool` at the start. No argument mapping necessary.
```kotlin
val arguments = BinaryData.fromString(functionArguments).toObject(FutureTemperatureResult::class.java)
futureTemperature(arguments.location!!, arguments.monthAndYear!!)
```

# Installation

Apply the Kotlin Symbol Processing plugin, add the project as both a
ksp (for generating classes) and implementation (for the annotations) dependency.
```kotlin
plugins {
    id("com.google.devtools.ksp") version "1.9.21-1.0.15"
}

repositories {
    // For ksp
    gradlePluginPortal()
    // For the annotations
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.robbiebowman:gpt-tools-annotations:1.0.1")
    // Use whichever version you like
    implementation("com.azure:azure-ai-openai:1.0.0-beta.6")
    ksp("com.github.robbiebowman:gpt-tools-annotations:1.0.1")
}
```

# Debugging Locally via IntelliJ
1. Run `./gradlew -Dkotlin.daemon.jvm.options="-Xdebug,-Xrunjdwp:transport=dt_socket\,address=5005\,server=y\,suspend=n" clean build` to get the KotlinCompileDaemon started.
2. You can easily attach to the process in IntelliJ after this completes: `Run -> Attach To Process...`. If the daemon doesn't appear here, try killing the process and restarting these steps: `./gradlew --stop; pkill -f KotlinCompileDaemon`
3. To debug the next build, place your breakpoints in code and rerun the gradle build above.

# Note on Kotlin data classes
There's currently a bug in ksp where annotations aren't properly associated with value parameters of data classes.
As a workaround, when describing the parameters of a data class, use `@property:GptDescription` instead.

See issue: https://github.com/google/ksp/issues/1562

# To-do's
- Make the kotlin type -> json type mapping extensible.
- Improve the discoverability of the generated FunctionDefinition.