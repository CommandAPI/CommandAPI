package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.NamespacedKeyArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Tests for the {@link NamespacedKeyArgument}
 */
class ArgumentNamespacedKeyTests extends TestBase {

	/*********
	 * Setup *
	 *********/

	@BeforeEach
	public void setUp() {
		super.setUp();
	}

	@AfterEach
	public void tearDown() {
		super.tearDown();
	}

	/*********
	 * Tests *
	 *********/

	@Test
	void executionTestWithNamespacedKeyArgument() {
		Mut<NamespacedKey> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new NamespacedKeyArgument("namespace"))
			.executesPlayer((player, args) -> {
				results.set((NamespacedKey) args.get("namespace"));
			})
			.register();

		Player player = addPlayer();

		// /test key
		assertStoresResult(player, "test key", results, new NamespacedKey("minecraft", "key"));

		// /test mynamespace:key
		assertStoresResult(player, "test mynamespace:key", results, new NamespacedKey("mynamespace", "key"));

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithNamespacedKeyArgument() {
		new CommandAPICommand("test")
			.withArguments(new NamespacedKeyArgument("namespace"))
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// /test
		// Should suggest nothing
		assertNoSuggestions(player, "test ");
	}

	// TODO: Not working, see https://github.com/JorelAli/CommandAPI/issues/479
	@Disabled
	@Test
	void suggestionTestWithNamespacedKeyArgumentSuggestions() {
		new CommandAPICommand("test")
			.withArguments(new NamespacedKeyArgument("namespace")
				.replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
					String input = info.currentArg();
					return Arrays.stream(Material.values())
						.filter(it -> !it.isLegacy())
						.map(it -> it.getKey().toString())
						.filter(it -> it.startsWith(input) || it.contains("_" + input))
						.toList();
				}))
			)
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		List<String> suggestions = Arrays.stream(Material.values())
			.filter(s -> !s.isLegacy())
			.map(Material::getKey)
			.map(NamespacedKey::toString)
			.filter(s -> s.contains("_log") || s.startsWith("log"))
			.sorted()
			.toList();

		// /test log
		// Expecting this to pass, but it doesn't for some reason?
		assertCommandSuggests(player, "test log", suggestions);
	}
}
