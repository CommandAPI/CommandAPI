package dev.jorel.commandapi.test.exceptions;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.exceptions.CommandConflictException;
import dev.jorel.commandapi.executors.NormalExecutorInfo;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommandConflictExceptionTests extends TestBase {

	/*********
	 * Setup *
	 *********/

	private Mut<String> results;
	private Player player;
	private NormalExecutorInfo<Player, ?> firstExecutor;
	private NormalExecutorInfo<Player, ?> secondExecutor;

	@BeforeEach
	public void setUp() {
		super.setUp();

		results = Mut.of();
		player = server.addPlayer();

		// The executor we register first should not be overwritten and should always run
		firstExecutor = info -> results.set("first");
		secondExecutor = info -> results.set("second");
	}

	@AfterEach
	public void tearDown() {
		super.tearDown();
	}

	/*********
	 * Tests *
	 *********/

	@Test
	void testExecutorsAreNotOverridden() {
		// No arguments
		new CommandAPICommand("noArguments")
			.executesPlayer(firstExecutor)
			.register();

		CommandAPICommand noArguments = new CommandAPICommand("noArguments")
			.executesPlayer(secondExecutor);

		assertThrowsWithMessage(
			CommandConflictException.class,
			"The command path \"/noArguments\" could not be registered because it conflicts with a previously registered command.",
			noArguments::register
		);
		assertStoresResult(player, "noArguments", results, "first");

		assertNoMoreResults(results);
	}

	@Test
	void testConflictDetectionWithSameArgumentPath() {
		// Arguments
		new CommandAPICommand("arguments")
			.withArguments(
				new LiteralArgument("literal"),
				new StringArgument("string")
			)
			.executesPlayer(firstExecutor)
			.register();

		CommandAPICommand arguments = new CommandAPICommand("arguments")
			.withArguments(
				new LiteralArgument("literal"),
				new StringArgument("string")
			)
			.executesPlayer(secondExecutor);

		assertThrowsWithMessage(
			CommandConflictException.class,
			"The command path \"/arguments literal string\" could not be registered because it conflicts with a previously registered command.",
			arguments::register
		);
		assertStoresResult(player, "arguments literal string", results, "first");

		assertNoMoreResults(results);
	}

	@Test
	void testConflictDetectionWithDifferentArgumentTypes() {
		// Different argument types
		new CommandAPICommand("argumentTypes")
			.withArguments(
				new IntegerArgument("alice"),
				new FloatArgument("bob")
			)
			.executesPlayer(firstExecutor)
			.register();

		CommandAPICommand argumentTypes = new CommandAPICommand("argumentTypes")
			.withArguments(
				new LongArgument("alice"),
				new DoubleArgument("bob")
			)
			.executesPlayer(secondExecutor);

		assertThrowsWithMessage(
			CommandConflictException.class,
			"The command path \"/argumentTypes alice bob\" could not be registered because it conflicts with a previously registered command.",
			argumentTypes::register
		);
		assertStoresResult(player, "argumentTypes 0 0", results, "first");

		assertNoMoreResults(results);
	}

	@Test
	void testConflictDetectionWithCommandTreeBranches() {
		// Different branches
		new CommandTree("branches")
			.then(
				new LiteralArgument("alice")
					.executesPlayer(firstExecutor)
			)
			.then(
				new LiteralArgument("bob")
					.executesPlayer(firstExecutor)
			)
			.register();

		CommandTree branches = new CommandTree("branches")
			.then(
				new LiteralArgument("bob")
					.executesPlayer(secondExecutor)
			)
			.then(
				new LiteralArgument("alice")
					.executesPlayer(secondExecutor)
			);

		assertThrowsWithMessage(
			CommandConflictException.class,
			"The command path \"/branches bob\" could not be registered because it conflicts with a previously registered command.",
			branches::register
		);
		assertStoresResult(player, "branches alice", results, "first");
		assertStoresResult(player, "branches bob", results, "first");

		assertNoMoreResults(results);
	}
}
