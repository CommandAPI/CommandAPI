package dev.jorel.commandapi.spying;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandTestBase;
import dev.jorel.commandapi.MockCommandSource;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.executors.ExecutionInfo;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExecutionQueueTests extends CommandTestBase {
	// Setup
	private ExecutionQueue queue;

	@BeforeEach
	public void setUp() {
		super.setUp();

		queue = getCommandAPIPlatform().getCommandAPIHandlerSpy().getExecutionQueue();

		new CommandAPICommand("test")
			.withOptionalArguments(new BooleanArgument("shouldFail"))
			.executes(info -> {
				if (info.args().getOrDefaultUnchecked("shouldFail", false)) {
					throw CommandAPI.failWithString("Command failed");
				}
			})
			.register();
	}

	@AfterEach
	public void tearDown() {
		super.tearDown();
	}

	// Tests
	@Test
	void testQueueClear() {
		PlayerMock player = server.addPlayer();

		assertCommandSucceeds(player, "test");
		assertCommandSucceeds(player, "test");

		queue.clear();
		queue.assertNoMoreCommandsWereRun();
	}

	@Test
	void testQueueAddPoll() {
		ExecutionInfo<CommandSender, MockCommandSource> info1 = Mockito.mock();
		ExecutionInfo<CommandSender, MockCommandSource> info2 = Mockito.mock();

		queue.add(info1);
		queue.add(info2);

		// Ensure queue like first-in first-out
		assertEquals(info1, queue.poll());
		assertEquals(info2, queue.poll());

		queue.assertNoMoreCommandsWereRun();
	}

	@Test
	void testQueueAutoAdd() {
		PlayerMock player1 = server.addPlayer();
		PlayerMock player2 = server.addPlayer();

		assertCommandSucceeds(player1, "test");
		assertCommandSucceeds(player2, "test");

		// Ensure queue-like first-in first-out when automatically adding
		assertEquals(player1, queue.poll().sender());
		assertEquals(player2, queue.poll().sender());

		queue.assertNoMoreCommandsWereRun();
	}

	@Test
	void testQueueAutoAddOnlyWhenParseSucceeds() {
		PlayerMock player = server.addPlayer();

		// Parse fails, so the CommandAPI executor is never invoked
		assertAssertionFails(
			() -> getExecutionInfoOfFailingCommand(
				player, "invalid",
				"Unknown command at position 0: <--[HERE]"
			),
			"No CommandAPI executor was invoked ==> expected: not <null>"
		);

		// Parse succeeds, though the CommandAPI executor throws its own exception
		assertNotNull(getExecutionInfoOfFailingCommand(
			player, "test true",
			"Command failed"
		));
	}

	@Test
	void testAssertNoMoreCommandsWereRun() {
		// Successful use
		queue.assertNoMoreCommandsWereRun();

		// Unsuccessful use
		PlayerMock player = server.addPlayer();
		assertCommandSucceeds(player, "test");

		assertAssertionFails(
			() -> queue.assertNoMoreCommandsWereRun(),
			"Expected no more commands to be run, but found 1 command(s) left"
		);

		// Successful use
		queue.poll();
		queue.assertNoMoreCommandsWereRun();

		// Parse failed commands should not appear in queue
		assertCommandFails(player, "invalid", "Unknown command at position 0: <--[HERE]");
		queue.assertNoMoreCommandsWereRun();
	}
}
