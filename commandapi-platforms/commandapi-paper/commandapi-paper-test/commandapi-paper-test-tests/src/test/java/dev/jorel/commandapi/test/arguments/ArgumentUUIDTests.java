package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.UUIDArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * Tests for the {@link UUIDArgument}
 */
class ArgumentUUIDTests extends TestBase {

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

	@RepeatedTest(10)
	void executionTestWithUUIDArgument() {
		Mut<UUID> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new UUIDArgument("uuid"))
			.executesPlayer((player, args) -> {
				results.set((UUID) args.get("uuid"));
			})
			.register();

		Player player = addPlayer();

		UUID randomUUID = UUID.randomUUID();
		assertStoresResult(player, "test " + randomUUID, results, randomUUID);

		// /test blah
		// Fails because 'blah' is not a valid UUID
		assertCommandFailsWith(player, "test blah", "Invalid UUID at position 5: test <--[HERE]");

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithUUIDArgument() {
		new CommandAPICommand("test")
			.withArguments(new UUIDArgument("uuid"))
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// /test
		// Should suggest nothing
		assertNoSuggestions(player, "test ");
	}
}
