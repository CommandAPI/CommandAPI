# Gradle Kotlin

A simple example of making a plugin that uses the CommandAPI with Maven.

Key points:

- The `commandapi-bukkit-plugin` dependency is used:

  ```kotlin
  implementation("dev.jorel:commandapi-bukkit-plugin:10.1.2")
  ```

- In the plugin.yml, CommandAPI is listed as a depend:

  ```yaml
  depend:
      - CommandAPI
  ```

- Classes from the NBT API can be accessed in `dev.jorel.commandapi.nbtapi`
