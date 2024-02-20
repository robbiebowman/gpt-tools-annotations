# Debugging Locally via IntelliJ
1. Run `./gradlew -Dkotlin.daemon.jvm.options="-Xdebug,-Xrunjdwp:transport=dt_socket\,address=5005\,server=y\,suspend=n" clean build` to get the KotlinCompileDaemon started.
2. You can easily attach to the process in IntelliJ after this completes: `Run -> Attach To Process...`. If the daemon doesn't appear here, try killing the process and restarting these steps: `./gradlew --stop; pkill -f KotlinCompileDaemon`
3. To debug the next build, place your breakpoints in code and rerun the gradle build above.