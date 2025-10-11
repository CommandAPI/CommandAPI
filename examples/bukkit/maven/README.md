# Maven

A simple example of making a plugin that uses the CommandAPI with Maven.

Key points:

- The `commandapi-paper-plugin` dependency is used with the provided scope

  ```xml
  <dependency>
      <groupId>dev.jorel</groupId>
      <artifactId>commandapi-paper-plugin</artifactId>
      <version>11.0.1-SNAPSHOT</version>
      <scope>provided</scope>
  </dependency>
  ```

- In the plugin.yml, CommandAPI is listed as a depend:

  ```yaml
  depend:
      - CommandAPI
  ```

- Classes from the NBT API can be accessed in `dev.jorel.commandapi.nbtapi`
