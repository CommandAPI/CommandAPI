# Maven

A simple example of making a plugin that uses the CommandAPI with Maven.

Key points:

- The `commandapi-bukkit-plugin` and `commandapi-annotations` dependencies are both added with the provided scope:

  ```xml
  <dependencies>
      <dependency>
          <groupId>dev.jorel</groupId>
          <artifactId>commandapi-bukkit-plugin</artifactId>
          <version>10.1.2</version>
          <scope>provided</scope>
      </dependency>
      <dependency>
          <groupId>dev.jorel</groupId>
          <artifactId>commandapi-annotations</artifactId>
          <version>10.1.2</version>
          <scope>provided</scope>
      </dependency>
  </dependencies>
  ```

- `commandapi-annotations` is used when compiling the project to turn the annotations into commands:

  ```xml
  <build>
      <plugins>
          <plugin>
              <groupId>org.apache.maven.plugins</groupId>
              <artifactId>maven-compiler-plugin</artifactId>
              <version>3.8.1</version>
              <configuration>
                  <annotationProcessorPaths>
                      <path>
                          <groupId>dev.jorel</groupId>
                          <artifactId>commandapi-annotations</artifactId>
                          <version>10.1.2</version>
                      </path>
                  </annotationProcessorPaths>
              </configuration>
        </plugin>
      </plugins>
  </build>
  ```

- In the plugin.yml, CommandAPI is listed as a depend:

  ```yaml
  depend:
      - CommandAPI
  ```

- Commands can be registered using `CommandAPI.registerCommand([ClassName].class)`
- Classes from the NBT API can be accessed in `dev.jorel.commandapi.nbtapi`
