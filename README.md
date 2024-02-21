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
- Add support for kotlin iterable types.
- Improve the discoverability of the generated FunctionDefinition.