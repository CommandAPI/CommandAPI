# kotlin-dsl Gradle

A simple example showcasing creating a command using the Kotlin DSL for the CommandAPI!

Key points:

- You do not need to use the `.register()` method
- You do not need to initialise any arguments.
- Add the `commandapi-kotlin-paper` dependency to your project:

  ```kotlin
  compileOnly("dev.jorel:commandapi-kotlin-paper:11.0.1-SNAPSHOT")
  ```

- The Kotlin DSL must not be shaded into your plugin
