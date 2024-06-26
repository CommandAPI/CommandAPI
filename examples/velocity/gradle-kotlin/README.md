# Gradle Kotlin

A simple example of making a plugin that uses the CommandAPI with Maven.

Key points:

- The `commandapi-velocity-core` dependency is used:

  ```kotlin
  implementation("dev.jorel:commandapi-velocity-core:9.6.0-SNAPSHOT")
  ```

- In the `@Plugin` annotation, `commandapi` is listed as a dependency:

  ```java
  @Plugin(id = "maven-example", description = "An example for using the CommandAPI with maven",
  // Add a dependency on the CommandAPI
  dependencies = {@Dependency(id = "commandapi")})
  ```
