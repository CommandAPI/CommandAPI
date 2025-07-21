# kotlin-dsl

A simple example showcasing creating a command using the Kotlin DSL for the CommandAPI!

Key points:

- You do not need to use the `.register()` method
- You do not need to initialise any arguments.
- Add the `commandapi-kotlin-bukkit` dependency to your project:

  ```xml
  <dependency>
    <groupId>dev.jorel</groupId>
    <artifactId>commandapi-kotlin-bukkit</artifactId>
    <version>10.1.2</version>
  </dependency>
  ```

- The Kotlin DSL must not be shaded into your plugin
