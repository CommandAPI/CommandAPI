# Automated tests (unshaded)

A simple example showcasing testing CommandAPI commands with [MockBukkit](https://github.com/MockBukkit/MockBukkit). For more information on the utility provided by the CommandAPI's testing framework, check the documentation [here](https://docs.commandapi.dev/test/intro).

Key points:

- The MockBukkit and `commandapi-spigot-test-toolkit` dependencies are listed with the `test` scope before the normal dependencies for `commandapi-spigot-core` and `spigot-api`. This ensures that when running tests, certain classes that are compatible with the testing environment override the regular classes. There is also a dependency for the [JUnit](https://junit.org/junit5/) API, which helps when writing the tests.

```xml
<dependencies>
	<dependency>
		<groupId>org.mockbukkit.mockbukkit</groupId>
		<artifactId>mockbukkit-v1.21</artifactId>
		<version>4.45.1</version>
		<scope>test</scope>
	</dependency>

	<dependency>
		<groupId>dev.jorel</groupId>
		<artifactId>commandapi-spigot-test-toolkit</artifactId>
		<version>11.0.0-SNAPSHOT</version>
		<scope>test</scope>
	</dependency>

	<dependency>
		<groupId>dev.jorel</groupId>
		<artifactId>commandapi-spigot-core</artifactId>
		<version>11.0.0-SNAPSHOT</version>
		<scope>provided</scope>
	</dependency>

	<dependency>
		<groupId>org.spigotmc</groupId>
		<artifactId>spigot-api</artifactId>
		<version>1.21.1-R0.1-SNAPSHOT</version>
		<scope>provided</scope>
	</dependency>

	<dependency>
		<groupId>org.junit.jupiter</groupId>
		<artifactId>junit-jupiter-engine</artifactId>
		<version>5.8.2</version>
		<scope>test</scope>
	</dependency>
</dependencies>
```

- Before running a test, you need to set up MockBukkit, which is described in their documentation [here](https://mockbukkit.readthedocs.io/en/latest/first_tests.html#creating-the-test-class). Since your plugin depends on the CommandAPI plugin, make sure to load the `MockCommandAPIPlugin` before loading your `JavaPlugin` class. After each test, call `MockBukkit.unmock()` to reset everything for the next test.

```java
@BeforeEach
public void setUp() {
	// Set up MockBukkit server
	server = MockBukkit.mock();

	// Load the CommandAPI plugin
	MockBukkit.load(MockCommandAPIPlugin.class);

	// Load our plugin
	MockBukkit.load(Main.class);
}

@AfterEach
public void tearDown() {
	// Reset for a clean slate next test
	MockBukkit.unmock();
}
```

- The `CommandAPITestUtilities` class provides static methods that allow you to interact with CommandAPI commands. You can verify that a command runs successfully, fails with a certain error message, runs with certain arguments, and suggests strings or tooltips.
