package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ChatArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

/**
 * Tests for the Paper {@link ChatArgument}
 */
class ArgumentChatTests extends TestBase {

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
	void executionTestWithAdventureChatArgument() {
		Mut<SignedMessage> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new ChatArgument("chat"))
			.executesPlayer((player, args) -> {
				results.set((SignedMessage) args.get("chat"));
			})
			.register();

		PlayerMock player = server.addPlayer("APlayer");
		player.setOp(true);

		// /test Hello, @p!
		server.dispatchCommand(player, "test Hello, @p!");
		assertEquals(GsonComponentSerializer.gson().deserialize("[\"Hello, \",\"APlayer\",\"!\"]"), results.get().unsignedContent());

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithAdventureChatArgumentNoOp() {
		Mut<SignedMessage> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new ChatArgument("chat"))
			.executesPlayer((player, args) -> {
				results.set((SignedMessage) args.get("chat"));
			})
			.register();

		PlayerMock player = server.addPlayer("APlayer");

		// The player isn't an operator, so selectors aren't resolved. Since that leaves the message
		//  unchanged, it has no unsigned content, and is only available as the plain message.

		// /test Hello, APlayer!
		server.dispatchCommand(player, "test Hello, APlayer!");
		SignedMessage message = results.get();
		assertEquals("Hello, APlayer!", message.message());
		assertNull(message.unsignedContent());

		// /test Hello, @p!
		server.dispatchCommand(player, "test Hello, @p!");
		message = results.get();
		assertEquals("Hello, @p!", message.message());
		assertNull(message.unsignedContent());

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithAdventureChatArgument() {
		new CommandAPICommand("test")
			.withArguments(new ChatArgument("chat"))
			.executesPlayer(P_EXEC)
			.register();

		PlayerMock player = server.addPlayer();

		// /test
		// Should suggest nothing
		assertEquals(List.of(), server.getSuggestions(player, "test "));
	}

}
