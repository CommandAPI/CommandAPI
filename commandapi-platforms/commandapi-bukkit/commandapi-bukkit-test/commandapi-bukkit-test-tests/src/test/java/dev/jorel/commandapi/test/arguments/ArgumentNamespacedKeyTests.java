package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.NamespacedKeyArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;

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

		PlayerMock player = server.addPlayer();

		// /test key
		server.dispatchCommand(player, "test key");
		assertEquals(new NamespacedKey("minecraft", "key"), results.get());
		
		// /test mynamespace:key
		server.dispatchCommand(player, "test mynamespace:key");
		assertEquals(new NamespacedKey("mynamespace", "key"), results.get());

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

		PlayerMock player = server.addPlayer();

		// /test
		// Should suggest nothing
		assertEquals(List.of(), server.getSuggestions(player, "test "));
	}

	// TODO: Not working, see https://github.com/JorelAli/CommandAPI/issues/479
	@Disabled
	@Test
	void suggestionTestWithNamespacedKeyArgumentSuggestions() {
		new CommandAPICommand("test")
			.withArguments(new NamespacedKeyArgument("namespace")
				.replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
					String input = info.currentArg();
					Collection<String> s = Arrays.stream(Material.values())
						.filter(it -> !it.isLegacy())
						.map(it -> it.getKey().toString())
						.filter(it -> it.toString().startsWith(input) || it.toString().contains("_" + input))
						.toList();
					return s;
				}))
			)
			.executesPlayer(P_EXEC)
			.register();

		PlayerMock player = server.addPlayer();

		List<String> suggestions = Arrays.stream(Material.values()).filter(s -> !s.isLegacy()).map(i -> i.getKey()).filter(k -> {
			return k.toString().contains("_log") || k.toString().startsWith("log");
		}).map(s -> s.toString()).sorted().toList();
		
		// /test log
		// Expecting this to pass, but it doesn't for some reason?
		assertEquals(suggestions, server.getSuggestions(player, "test log"));
	}

}
