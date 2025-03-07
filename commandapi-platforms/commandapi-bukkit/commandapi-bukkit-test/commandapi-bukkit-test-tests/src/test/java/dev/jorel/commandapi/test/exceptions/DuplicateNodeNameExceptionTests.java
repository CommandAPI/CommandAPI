package dev.jorel.commandapi.test.exceptions;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.exceptions.DuplicateNodeNameException;
import dev.jorel.commandapi.test.TestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Tests for the {@link DuplicateNodeNameException}.
 */
class DuplicateNodeNameExceptionTests extends TestBase {

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
	void testCommandAPICommandDuplicateArgumentNames() {
		// This command is not okay because it has duplicate names for Arguments 1 and 3
		CommandAPICommand commandWithDuplicateArgumentNames = new CommandAPICommand("test")
			.withArguments(
				new StringArgument("alice"),
				new StringArgument("bob"),
				new StringArgument("alice")
			)
			.executesPlayer(P_EXEC);

		assertThrowsWithMessage(
			DuplicateNodeNameException.class,
			"Duplicate node names for listed arguments are not allowed! Going down the [test<MultiLiteralArgument> alice<StringArgument> bob<StringArgument>] branch, found alice<StringArgument>, which had a duplicated node name",
			commandWithDuplicateArgumentNames::register
		);
	}

	@Test
	void testCommandAPICommandUnlistedArguments() {
		// This command is okay because unlisted arguments do not cause conflict
		CommandAPICommand commandWithDuplicateUnlistedArgumentNames = new CommandAPICommand("test")
			.withArguments(
				new StringArgument("alice").setListed(false),
				new StringArgument("bob"),
				new StringArgument("alice"),
				new StringArgument("bob").setListed(false)
			)
			.executesPlayer(P_EXEC);

		assertDoesNotThrow(() -> commandWithDuplicateUnlistedArgumentNames.register());
	}

	@Test
	void testCommandAPICommandWithLiteralArguments() {
		// This command is okay because LiteralArguments are unlisted by default
		CommandAPICommand commandWithDuplicateLiteralArgumentNames = new CommandAPICommand("test")
			.withArguments(
				new LiteralArgument("alice"),
				new LiteralArgument("bob"),
				new LiteralArgument("alice")
			)
			.executesPlayer(P_EXEC);

		assertDoesNotThrow(() -> commandWithDuplicateLiteralArgumentNames.register());

		// However, listed LiteralArguments do conflict
		CommandAPICommand commandWithDuplicateListedLiteralArgumentNames = new CommandAPICommand("test")
			.withArguments(
				new LiteralArgument("alice").setListed(true),
				new LiteralArgument("bob").setListed(true),
				new LiteralArgument("alice").setListed(true)
			)
			.executesPlayer(P_EXEC);

		assertThrowsWithMessage(
			DuplicateNodeNameException.class,
			"Duplicate node names for listed arguments are not allowed! Going down the [test<MultiLiteralArgument> alice<LiteralArgument> bob<LiteralArgument>] branch, found alice<LiteralArgument>, which had a duplicated node name",
			commandWithDuplicateListedLiteralArgumentNames::register
		);

		CommandAPICommand command = new CommandAPICommand("test")
			.withArguments(
				new MultiLiteralArgument("literal", "a", "b", "c"),
				new MultiLiteralArgument("literal", "c", "d", "e")
			)
			.executesPlayer(P_EXEC);

		assertThrowsWithMessage(
			DuplicateNodeNameException.class,
			"Duplicate node names for listed arguments are not allowed! Going down the [test<MultiLiteralArgument> literal<MultiLiteralArgument>] branch, found literal<MultiLiteralArgument>, which had a duplicated node name",
			command::register
		);
	}

	@Test
	void testCommandTreeDuplicateArgumentNames() {
		// This command is not okay because it has duplicate names for Arguments 1 and 3
		CommandTree commandWithDuplicateArgumentNames = new CommandTree("test")
			.then(
				new StringArgument("alice").then(
					new StringArgument("bob").then(
						new StringArgument("alice")
							.executesPlayer(P_EXEC)
					)
				)
			);

		assertThrowsWithMessage(
			DuplicateNodeNameException.class,
			"Duplicate node names for listed arguments are not allowed! Going down the [test<MultiLiteralArgument> alice<StringArgument> bob<StringArgument>] branch, found alice<StringArgument>, which had a duplicated node name",
			commandWithDuplicateArgumentNames::register
		);
	}

	@Test
	void testCommandTreeUnlistedArguments() {
		// This command is okay because unlisted arguments do not cause conflict
		CommandTree commandWithDuplicateUnlistedArgumentNames = new CommandTree("test")
			.then(
				new StringArgument("alice").setListed(false).then(
					new StringArgument("bob").then(
						new StringArgument("alice").then(
							new StringArgument("bob").setListed(false)
								.executesPlayer(P_EXEC)
						)
					)
				)
			);

		assertDoesNotThrow(() -> commandWithDuplicateUnlistedArgumentNames.register());
	}

	@Test
	void testCommandTreeWithLiteralArguments() {
		// This command is okay because LiteralArguments are unlisted by default
		CommandTree commandWithDuplicateLiteralArgumentNames = new CommandTree("test")
			.then(
				new LiteralArgument("alice").then(
					new LiteralArgument("bob").then(
						new LiteralArgument("alice")
							.executesPlayer(P_EXEC)
					)
				)
			);

		assertDoesNotThrow(() -> commandWithDuplicateLiteralArgumentNames.register());

		// However, listed LiteralArguments do conflict
		CommandTree commandWithDuplicateListedLiteralArgumentNames = new CommandTree("test")
			.then(
				new LiteralArgument("alice").setListed(true).then(
					new LiteralArgument("bob").setListed(true).then(
						new LiteralArgument("alice").setListed(true)
							.executesPlayer(P_EXEC)
					)
				)
			);

		assertThrowsWithMessage(
			DuplicateNodeNameException.class,
			"Duplicate node names for listed arguments are not allowed! Going down the [test<MultiLiteralArgument> alice<LiteralArgument> bob<LiteralArgument>] branch, found alice<LiteralArgument>, which had a duplicated node name",
			commandWithDuplicateListedLiteralArgumentNames::register
		);
	}

	@Test
	void testCommandTreeWithSeparatedDuplicateNames() {
		// This command is okay because the duplicate names are on different paths
		CommandTree commandWithDuplicateNamesSeparated = new CommandTree("test")
			.then(new LiteralArgument("path1").then(new StringArgument("alice").executesPlayer(P_EXEC)))
			.then(new LiteralArgument("path2").then(new StringArgument("alice").executesPlayer(P_EXEC)))
			.then(new LiteralArgument("path3").then(new StringArgument("alice").executesPlayer(P_EXEC)))
			.then(new LiteralArgument("path4").then(new StringArgument("alice").executesPlayer(P_EXEC)));

		assertDoesNotThrow(() -> commandWithDuplicateNamesSeparated.register());
	}
}
