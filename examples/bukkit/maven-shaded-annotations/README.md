# Maven

A simple example of making a plugin that uses the CommandAPI with Maven.

Key points:

- The `commandapi-paper-shade` and `commandapi-paper-annotations` dependencies are both added, with annotations having the provided scope:

  ```xml
  <dependencies>
      <dependency>
          <groupId>dev.jorel</groupId>
          <artifactId>commandapi-paper-shade</artifactId>
          <version>11.1.0</version>
      </dependency>
      <dependency>
          <groupId>dev.jorel</groupId>
          <artifactId>commandapi-paper-annotations</artifactId>
          <version>11.1.0</version>
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
                          <version>11.1.0</version>
                      </path>
                  </annotationProcessorPaths>
              </configuration>
        </plugin>
      </plugins>
  </build>
  ```

- In our JavaPlugin, `Main.java`, we call `CommandAPI.onLoad()` and `CommandAPI.onEnable()` to set up the CommandAPI
- Commands can be registered using `CommandAPI.registerCommand([ClassName].class)`
- In `pom.xml`, we use relocation to relocate the CommandAPI to the `io.github.jorelali.commandapi` package. This gives us the following directory structure in our plugin's `.jar` file:

  ```text
  .
  ├── io
  │   └── github
  │       └── jorelali
  │           ├── commandapi
  │           │   ├── arguments
  │           │   │   ├── AdvancementArgument.class
  │           │   │   └── ...
  │           │   ├── CommandAPI.class
  │           │   ├── CommandAPICommand.class
  │           │   └── ...
  │           ├── Main.class
  │           └── MyCommands.class
  ├── META-INF
  │   └── MANIFEST.MF
  └── plugin.yml
  ```
