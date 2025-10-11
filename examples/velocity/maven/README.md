# Maven

A simple example of making a plugin that uses the CommandAPI with Maven.

Key points:

- The `commandapi-velocity-core` dependency is used with the provided scope:

  ```xml
  <dependency>
      <groupId>dev.jorel</groupId>
      <artifactId>commandapi-velocity-core</artifactId>
      <version>11.0.1-SNAPSHOT</version>
      <scope>provided</scope>
  </dependency>
  ```

- In the `@Plugin` annotation, `commandapi` is listed as a dependency:

  ```java
  @Plugin(id = "maven-example", description = "An example for using the CommandAPI with maven",
  // Add a dependency on the CommandAPI
  dependencies = {@Dependency(id = "commandapi")})
  ```
