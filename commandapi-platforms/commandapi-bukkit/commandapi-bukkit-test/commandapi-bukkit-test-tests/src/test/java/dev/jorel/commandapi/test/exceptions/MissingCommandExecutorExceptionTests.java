package dev.jorel.commandapi.test.exceptions;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.exceptions.MissingCommandExecutorException;
import dev.jorel.commandapi.test.TestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Tests for the {@link MissingCommandExecutorException}.
 */
class MissingCommandExecutorExceptionTests extends TestBase {

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
	void testCommandAPICommandNoBaseExecutor() {
		// This command has no executor, should complain because this isn't runnable
		CommandAPICommand commandWithNoExecutors = new CommandAPICommand("test")
			.withArguments(new StringArgument("arg1"));

		assertThrowsWithMessage(
			MissingCommandExecutorException.class,
			"The command path test<LiteralArgument> is not executable!",
			commandWithNoExecutors::register
		);
	}

	@Test
	void testCommandAPICommandSubcommandExecutors() {
		// This command has no executable subcommands, should complain because this isn't runnable
		CommandAPICommand commandWithNoRunnableSubcommands = new CommandAPICommand("test")
			.withSubcommand(new CommandAPICommand("sub"));

		assertThrowsWithMessage(
			MissingCommandExecutorException.class,
			"The command path sub<LiteralArgument> is not executable!",
			commandWithNoRunnableSubcommands::register
		);

		// This command is okay because it is eventually executable through a subcommand
		CommandAPICommand commandWithEventuallyRunnableSubcommand = new CommandAPICommand("test")
			.withSubcommand(new CommandAPICommand("sub")
				.withSubcommand(new CommandAPICommand("sub")
					.withSubcommand(new CommandAPICommand("sub")
						.withSubcommand(new CommandAPICommand("sub")
							.executesPlayer(P_EXEC)
						)
					)
				)
			);

		assertDoesNotThrow(() -> commandWithEventuallyRunnableSubcommand.register());
	}

	@Test
	void testCommandAPICommandExecutableSubcommandButBaseArguments() {
		// This command is not okay, because the presence of arguments on
		//  the root indicate the root path was meant to be executed
		CommandAPICommand commandWithNotExecutableRootArgument = new CommandAPICommand("test")
			.withArguments(new StringArgument("arg1"))
			.withSubcommand(
				new CommandAPICommand("sub")
					.executesPlayer(P_EXEC)
			);

		assertThrowsWithMessage(
			MissingCommandExecutorException.class,
			"The command path test<LiteralArgument> is not executable!",
			commandWithNotExecutableRootArgument::register
		);
	}

	@Test
	void testCommandTreeNoBaseExecutor() {
		// This command has no executor, should complain because this isn't runnable
		CommandTree commandWithNoExecutors = new CommandTree("test");

		assertThrowsWithMessage(
			MissingCommandExecutorException.class,
			"The command path test<LiteralArgument> is not executable!",
			commandWithNoExecutors::register
		);
	}

	@Test
	void testCommandTreeBranchExecutors() {
		// This command has no executable arguments, should complain because this isn't runnable
		CommandTree commandWithNoRunnableSubcommands = new CommandTree("test")
			.then(new LiteralArgument("sub"));

		assertThrowsWithMessage(
			MissingCommandExecutorException.class,
			"The command path [test<MultiLiteralArgument>] ending with sub<LiteralArgument> is not executable!",
			commandWithNoRunnableSubcommands::register
		);

		// This command is okay because it eventually has an executable argument
		CommandTree commandWithEventuallyRunnableSubcommand = new CommandTree("test")
			.then(new LiteralArgument("sub")
				.then(new LiteralArgument("sub")
					.then(new LiteralArgument("sub")
						.then(new LiteralArgument("sub")
							.executesPlayer(P_EXEC)
						)
					)
				)
			);

		assertDoesNotThrow(() -> commandWithEventuallyRunnableSubcommand.register());

		// This command is not okay because some paths are not executable
		CommandTree commandTreeWithSomeNotExecutablePaths = new CommandTree("test")
			.then(new LiteralArgument("executable1").then(new LiteralArgument("sub").executesPlayer(P_EXEC)))
			.then(new LiteralArgument("notExecutable1").then(new LiteralArgument("sub")))
			.then(new LiteralArgument("notExecutable2").then(new LiteralArgument("sub")))
			.then(new LiteralArgument("executable2").then(new LiteralArgument("sub").executesPlayer(P_EXEC)));

		assertThrowsWithMessage(
			MissingCommandExecutorException.class,
			"The command path [test<MultiLiteralArgument> notExecutable1<LiteralArgument>] ending with sub<LiteralArgument> is not executable!",
			commandTreeWithSomeNotExecutablePaths::register
		);
	}
}
