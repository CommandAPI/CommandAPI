# Gradle Groovy

A simple example of making a plugin that uses the CommandAPI with Maven.

Key points:

- The `commandapi-paper-plugin` dependency is used:

  ```groovy
  implementation 'dev.jorel:commandapi-paper-plugin:11.0.1-SNAPSHOT'
  ```

- In the plugin.yml, CommandAPI is listed as a depend:

  ```yaml
  depend:
      - CommandAPI
  ```

- Classes from the NBT API can be accessed in `dev.jorel.commandapi.nbtapi`
