package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.UUIDArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;

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

		PlayerMock player = server.addPlayer();

		UUID randomUUID = UUID.randomUUID();
		server.dispatchCommand(player, "test " + randomUUID);
		assertEquals(randomUUID, results.get());

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

		PlayerMock player = server.addPlayer();

		// /test
		// Should suggest nothing
		assertEquals(List.of(), server.getSuggestions(player, "test "));
	}

}
