# Maven-shaded

A simple example of shading the CommandAPI with Maven.

Key points:

- The `commandapi-velocity-shade` dependency is used:

  ```xml
  <dependency>
      <groupId>dev.jorel</groupId>
      <artifactId>commandapi-velocity-shade</artifactId>
      <version>11.1.0</version>
  </dependency>
  ```

- In our JavaPlugin, `Main.java`, we call `CommandAPI.onLoad()` and `CommandAPI.onEnable()` to set up the CommandAPI
- In `pom.xml`, we use relocation to relocate the CommandAPI to the `io.github.jorelali.commandapi` package. This gives us the following directory structure in our plugin's `.jar` file:

```text
.
├── io
│   └── github
│       └── jorelali
│           ├── commandapi
│           │   ├── arguments
│           │   │   ├── GreedyStringArgument.class
│           │   │   └── ...
│           │   ├── CommandAPI.class
│           │   ├── CommandAPICommand.class
│           │   └── ...
│           └── Main.class
└── META-INF
    └── MANIFEST.MF
```
