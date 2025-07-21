# Maven

A simple example of making a plugin that uses the CommandAPI with Maven.

Key points:

- The `commandapi-bukkit-plugin` dependency is used with the provided scope

  ```xml
  <dependency>
      <groupId>dev.jorel</groupId>
      <artifactId>commandapi-bukkit-plugin</artifactId>
      <version>10.1.2</version>
      <scope>provided</scope>
  </dependency>
  ```

- In the plugin.yml, CommandAPI is listed as a depend:

  ```yaml
  depend:
      - CommandAPI
  ```

- Classes from the NBT API can be accessed in `dev.jorel.commandapi.nbtapi`
