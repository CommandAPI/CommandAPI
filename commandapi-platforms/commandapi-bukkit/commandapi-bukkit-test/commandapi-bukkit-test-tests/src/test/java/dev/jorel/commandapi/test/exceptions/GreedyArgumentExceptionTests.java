package dev.jorel.commandapi.test.exceptions;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.exceptions.GreedyArgumentException;
import dev.jorel.commandapi.test.TestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Tests for the {@link GreedyArgumentException}.
 */
class GreedyArgumentExceptionTests extends TestBase {

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
	void testCommandAPICommandGreedyArgumentException() {
		// Shouldn't throw, greedy argument is at the end
		CommandAPICommand validGreedyCommand = new CommandAPICommand("test")
			.withArguments(new StringArgument("arg1"))
			.withArguments(new GreedyStringArgument("arg2"))
			.executesPlayer(P_EXEC);

		assertDoesNotThrow(() -> validGreedyCommand.register());

		// Should throw, greedy argument is not at the end
		CommandAPICommand invalidGreedyCommand = new CommandAPICommand("test")
			.withArguments(new GreedyStringArgument("arg1"))
			.withArguments(new StringArgument("arg2"))
			.executesPlayer(P_EXEC);

		assertThrowsWithMessage(
			GreedyArgumentException.class,
			"A greedy argument can only be declared at the end of a command. Going down the [test<MultiLiteralArgument>] branch, found the greedy argument arg1<GreedyStringArgument> followed by arg2<StringArgument>",
			invalidGreedyCommand::register
		);
	}

	@Test
	void testCommandTreeGreedyArgumentException() {
		// Shouldn't throw, greedy argument is at the end
		CommandTree validGreedyCommand = new CommandTree("test")
			.then(
				new StringArgument("arg1")
					.then(
						new GreedyStringArgument("arg2")
							.executesPlayer(P_EXEC)
					)
			);

		assertDoesNotThrow(() -> validGreedyCommand.register());

		// Should throw, greedy argument is not at the end
		CommandTree invalidGreedyCommand = new CommandTree("test")
			.then(
				new GreedyStringArgument("arg1")
					.then(
						new StringArgument("arg2")
							.executesPlayer(P_EXEC)
					)
			);

		assertThrowsWithMessage(
			GreedyArgumentException.class,
			"A greedy argument can only be declared at the end of a command. Going down the [test<MultiLiteralArgument>] branch, found the greedy argument arg1<GreedyStringArgument> followed by arg2<StringArgument>",
			invalidGreedyCommand::register
		);
	}
}
