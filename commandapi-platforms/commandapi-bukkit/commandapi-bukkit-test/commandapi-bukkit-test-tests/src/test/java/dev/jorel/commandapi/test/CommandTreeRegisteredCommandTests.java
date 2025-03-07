package dev.jorel.commandapi.test;

import dev.jorel.commandapi.*;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.help.EditableHelpTopic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.function.Predicate;

import static dev.jorel.commandapi.test.RegisteredCommandTestBase.NodeBuilder.node;

/**
 * Tests for making sure the {@link RegisteredCommand} information is correct when registering {@link CommandTree}s
 */
class CommandTreeRegisteredCommandTests extends RegisteredCommandTestBase {

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

	private NodeBuilder commandNode(String nodeName, boolean executable) {
		return node(nodeName, CommandTree.class, executable).helpString(nodeName);
	}

	/*********
	 * Tests *
	 *********/

	@Test
	void testRegister() {
		new CommandTree("command")
			.executesPlayer(P_EXEC)
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", true),
			List.of("command:CommandTree")
		);
	}

	@Test
	void testRegisterHelpInformation() {
		new CommandTree("command")
			.withHelp("short description", "full description")
			.withUsage(
				"usage 1",
				"usage 2",
				"usage 3"
			)
			.executesPlayer(P_EXEC)
			.register();

		RegisteredCommand<CommandSender> expectedCommand = new RegisteredCommand<>(
			"command", new String[0], "minecraft",
			new EditableHelpTopic<>("short description", "full description", new String[]{"usage 1", "usage 2", "usage 3"}),
			commandNode("command", true).build()
		);

		assertCreatedRegisteredCommands(expectedCommand.copyWithEmptyNamespace(), expectedCommand);
	}

	@Test
	void testRegisterOpPermission() {
		new CommandTree("command")
			.withPermission(CommandPermission.OP)
			.executesPlayer(P_EXEC)
			.register();

		RegisteredCommand<CommandSender> expectedCommand = new RegisteredCommand<>(
			"command", new String[0], "minecraft",
			new EditableHelpTopic<>(),
			commandNode("command", true).permission(CommandPermission.OP).build()
		);

		assertCreatedRegisteredCommands(expectedCommand.copyWithEmptyNamespace(), expectedCommand);
	}

	@Test
	void testRegisterStringPermission() {
		new CommandTree("command")
			.withPermission("permission")
			.executesPlayer(P_EXEC)
			.register();

		RegisteredCommand<CommandSender> expectedCommand = new RegisteredCommand<>(
			"command", new String[0], "minecraft",
			new EditableHelpTopic<>(),
			commandNode("command", true).permission(CommandPermission.fromString("permission")).build()
		);

		assertCreatedRegisteredCommands(expectedCommand.copyWithEmptyNamespace(), expectedCommand);
	}

	@Test
	void testRegisterRequirement() {
		Predicate<CommandSender> requirement = sender -> sender instanceof Player;

		new CommandTree("command")
			.withRequirement(requirement)
			.executesPlayer(P_EXEC)
			.register();

		RegisteredCommand<CommandSender> expectedCommand = new RegisteredCommand<>(
			"command", new String[0], "minecraft",
			new EditableHelpTopic<>(),
			commandNode("command", true).requirements(requirement).build()
		);

		assertCreatedRegisteredCommands(expectedCommand.copyWithEmptyNamespace(), expectedCommand);
	}

	@Test
	void testRegisterOneAlias() {
		new CommandTree("command")
			.withAliases("alias1")
			.executesPlayer(P_EXEC)
			.register();

		RegisteredCommand<CommandSender> expectedCommand = simpleRegisteredCommand("command", "minecraft", commandNode("command", true), "alias1");

		assertCreatedRegisteredCommands(expectedCommand.copyWithEmptyNamespace(), expectedCommand);
	}

	@Test
	void testRegisterTwoAliases() {
		new CommandTree("command")
			.withAliases("alias1", "alias2")
			.executesPlayer(P_EXEC)
			.register();

		RegisteredCommand<CommandSender> expectedCommand = simpleRegisteredCommand("command", "minecraft", commandNode("command", true), "alias1", "alias2");

		assertCreatedRegisteredCommands(expectedCommand.copyWithEmptyNamespace(), expectedCommand);
	}

	@Test
	void testRegisterNamespace() {
		new CommandTree("command")
			.executesPlayer(P_EXEC)
			.register("custom");

		RegisteredCommand<CommandSender> expectedCommand = simpleRegisteredCommand("command", "custom", commandNode("command", true));

		assertCreatedRegisteredCommands(expectedCommand.copyWithEmptyNamespace(), expectedCommand);
	}

	@Test
	void testRegisterOneBranch() {
		new CommandTree("command")
			.then(new StringArgument("string").executesPlayer(P_EXEC))
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", false)
				.withChildren(node("string", StringArgument.class, true)),
			List.of("command:CommandTree", "string:StringArgument")
		);
	}

	@Test
	void testRegisterTwoBranches() {
		new CommandTree("command")
			.then(new StringArgument("string").executesPlayer(P_EXEC))
			.then(new IntegerArgument("integer").executesPlayer(P_EXEC))
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", false).withChildren(
				node("string", StringArgument.class, true),
				node("integer", IntegerArgument.class, true)
			),
			List.of("command:CommandTree", "string:StringArgument"),
			List.of("command:CommandTree", "integer:IntegerArgument")
		);
	}

	@Test
	void testRegisterArgumentPermissions() {
		new CommandTree("command")
			.then(
				new StringArgument("noPermission")
					.then(
						new StringArgument("opPermission")
							.withPermission(CommandPermission.OP)
							.then(
								new StringArgument("stringPermission")
									.withPermission(CommandPermission.fromString("permission"))
									.executesPlayer(P_EXEC)
							)
					)
			)
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", false).withChildren(
				node("noPermission", StringArgument.class, false).withChildren(
				node("opPermission", StringArgument.class, false).permission(CommandPermission.OP).withChildren(
				node("stringPermission", StringArgument.class, true).permission(CommandPermission.fromString("permission"))
			))),
			List.of("command:CommandTree", "noPermission:StringArgument", "opPermission:StringArgument", "stringPermission:StringArgument")
		);
	}

	@Test
	void testRegisterArgumentRequirement() {
		Predicate<CommandSender> requirement = sender -> sender instanceof Player;

		new CommandTree("command")
			.then(new StringArgument("string").withRequirement(requirement).executesPlayer(P_EXEC))
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", false).withChildren(
				node("string", StringArgument.class, true).requirements(requirement)
			),
			List.of("command:CommandTree", "string:StringArgument")
		);
	}

	@Test
	void testRegisterMultiLiteralArguments() {
		new CommandTree("command")
			.then(
				new MultiLiteralArgument("literal1", "a", "b", "c")
					.then(new MultiLiteralArgument("literal2", "d", "e", "f").executesPlayer(P_EXEC))
			)
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", false).withChildren(
				node("literal1", MultiLiteralArgument.class, false).helpString("(a|b|c)").withChildren(
					node("literal2", MultiLiteralArgument.class, true).helpString("(d|e|f)")
				)
			),
			List.of("command:CommandTree", "literal1:MultiLiteralArgument", "literal2:MultiLiteralArgument")
		);
	}

	@Test
	void testRegisterCombinedArguments() {
		new CommandTree("command")
			.then(
				new LiteralArgument("1").combineWith(new LiteralArgument("2"))
					.then(
						new LiteralArgument("3").combineWith(new LiteralArgument("4"))
							.executesPlayer(P_EXEC)
							.then(
								new LiteralArgument("5").combineWith(new LiteralArgument("6"))
									.executesPlayer(P_EXEC)
									.then(
										new LiteralArgument("7").combineWith(new LiteralArgument("8"))
											.executesPlayer(P_EXEC)
									)
							)
					)
			)
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", false)
				.withChildren(node("1", LiteralArgument.class, false).helpString("1")
				.withChildren(node("2", LiteralArgument.class, false).helpString("2")
				.withChildren(node("3", LiteralArgument.class, false).helpString("3")
				.withChildren(node("4", LiteralArgument.class, true).helpString("4")
				.withChildren(node("5", LiteralArgument.class, false).helpString("5")
				.withChildren(node("6", LiteralArgument.class, true).helpString("6")
				.withChildren(node("7", LiteralArgument.class, false).helpString("7")
				.withChildren(node("8", LiteralArgument.class, true).helpString("8")
			)))))))),
			List.of("command:CommandTree",
				"1:LiteralArgument", "2:LiteralArgument", "3:LiteralArgument", "4:LiteralArgument"
			),
			List.of("command:CommandTree",
				"1:LiteralArgument", "2:LiteralArgument", "3:LiteralArgument", "4:LiteralArgument",
				"5:LiteralArgument", "6:LiteralArgument"
			),
			List.of("command:CommandTree",
				"1:LiteralArgument", "2:LiteralArgument", "3:LiteralArgument", "4:LiteralArgument",
				"5:LiteralArgument", "6:LiteralArgument", "7:LiteralArgument", "8:LiteralArgument"
			)
		);
	}

	@Test
	void testRegisterThenNested() {
		// Make sure dispatcher is cleared from any previous tests
		CommandAPIHandler.getInstance().writeDispatcherToFile();

		// Register a command using the new `thenNested` method
		new CommandTree("command").thenNested(
			new LiteralArgument("a"),
			new LiteralArgument("b"),
			new LiteralArgument("c")
				.executesPlayer(P_EXEC)
		).register();

		// Command added to tree
		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", false)
				.withChildren(node("a", LiteralArgument.class, false).helpString("a")
				.withChildren(node("b", LiteralArgument.class, false).helpString("b")
				.withChildren(node("c", LiteralArgument.class, true).helpString("c")
			))),
			List.of("command:CommandTree", "a:LiteralArgument", "b:LiteralArgument", "c:LiteralArgument")
		);
	}

	//////////////////////////////////////
	// SUBTREES                         //
	// The same as commands, but deeper //
	//////////////////////////////////////

	@Test
	void testRegisterOneBranchAndBaseExecutable() {
		new CommandTree("command")
			.executesPlayer(P_EXEC)
			.then(
				new LiteralArgument("subcommand")
					.executesPlayer(P_EXEC)
			)
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", true)
				.withChildren(node("subcommand", LiteralArgument.class, true).helpString("subcommand")),
			List.of("command:CommandTree"),
			List.of("command:CommandTree", "subcommand:LiteralArgument")
		);
	}

	@Test
	void testRegisterBranchesWithBranches() {
		new CommandTree("command")
			.then(
				new LiteralArgument("subcommand1")
					.then(new StringArgument("string1").executesPlayer(P_EXEC))
					.then(new IntegerArgument("integer1").executesPlayer(P_EXEC))
			)
			.then(
				new LiteralArgument("subcommand2")
					.then(new StringArgument("string2").executesPlayer(P_EXEC))
					.then(new IntegerArgument("integer2").executesPlayer(P_EXEC))
			)
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", false).withChildren(
				node("subcommand1", LiteralArgument.class, false).helpString("subcommand1").withChildren(
					node("string1", StringArgument.class, true),
					node("integer1", IntegerArgument.class, true)
				),
				node("subcommand2", LiteralArgument.class, false).helpString("subcommand2").withChildren(
					node("string2", StringArgument.class, true),
					node("integer2", IntegerArgument.class, true)
				)
			),
			List.of("command:CommandTree", "subcommand1:LiteralArgument", "string1:StringArgument"),
			List.of("command:CommandTree", "subcommand1:LiteralArgument", "integer1:IntegerArgument"),
			List.of("command:CommandTree", "subcommand2:LiteralArgument", "string2:StringArgument"),
			List.of("command:CommandTree", "subcommand2:LiteralArgument", "integer2:IntegerArgument")
		);
	}

	@Test
	void testRegisterBranchesWithCombinedArguments() {
		new CommandTree("command")
			.then(
				new LiteralArgument("subcommand1")
					.then(
						new LiteralArgument("1a").combineWith(new LiteralArgument("1b"))
							.executesPlayer(P_EXEC)
							.then(
								new LiteralArgument("1c").combineWith(new LiteralArgument("1d"))
									.executesPlayer(P_EXEC)
							)
					)
			)
			.then(
				new LiteralArgument("subcommand2")
					.then(
						new LiteralArgument("2a").combineWith(new LiteralArgument("2b"))
							.executesPlayer(P_EXEC)
							.then(
								new LiteralArgument("2c").combineWith(new LiteralArgument("2d"))
									.executesPlayer(P_EXEC)
							)
					)
			)
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", false).withChildren(
				node("subcommand1", LiteralArgument.class, false).helpString("subcommand1")
					.withChildren(node("1a", LiteralArgument.class, false).helpString("1a")
					.withChildren(node("1b", LiteralArgument.class, true).helpString("1b")
					.withChildren(node("1c", LiteralArgument.class, false).helpString("1c")
					.withChildren(node("1d", LiteralArgument.class, true).helpString("1d")
				)))),
				node("subcommand2", LiteralArgument.class, false).helpString("subcommand2")
					.withChildren(node("2a", LiteralArgument.class, false).helpString("2a")
					.withChildren(node("2b", LiteralArgument.class, true).helpString("2b")
					.withChildren(node("2c", LiteralArgument.class, false).helpString("2c")
					.withChildren(node("2d", LiteralArgument.class, true).helpString("2d")
				))))
			),
			List.of("command:CommandTree", "subcommand1:LiteralArgument", "1a:LiteralArgument", "1b:LiteralArgument"),
			List.of("command:CommandTree", "subcommand1:LiteralArgument", "1a:LiteralArgument", "1b:LiteralArgument", "1c:LiteralArgument", "1d:LiteralArgument"),
			List.of("command:CommandTree", "subcommand2:LiteralArgument", "2a:LiteralArgument", "2b:LiteralArgument"),
			List.of("command:CommandTree", "subcommand2:LiteralArgument", "2a:LiteralArgument", "2b:LiteralArgument", "2c:LiteralArgument", "2d:LiteralArgument")
		);
	}

	/////////////////////////
	// Information merging //
	/////////////////////////

	@Test
	void testRegisterTwoSeparateCommands() {
		new CommandTree("command1")
			.executesPlayer(P_EXEC)
			.register();

		new CommandTree("command2")
			.executesPlayer(P_EXEC)
			.register();

		RegisteredCommand<CommandSender> command1 = simpleRegisteredCommand(
			"command1", "minecraft",
			commandNode("command1", true)
		);
		RegisteredCommand<CommandSender> command2 = simpleRegisteredCommand(
			"command2", "minecraft",
			commandNode("command2", true)
		);

		assertCreatedRegisteredCommands(
			command1.copyWithEmptyNamespace(), command1,
			command2.copyWithEmptyNamespace(), command2
		);
	}

	@Test
	void testRegisterMergeArguments() {
		new CommandTree("command")
			.then(new StringArgument("string").executesPlayer(P_EXEC))
			.register();

		new CommandTree("command")
			.then(new IntegerArgument("integer").executesPlayer(P_EXEC))
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", false).withChildren(
				node("string", StringArgument.class, true),
				node("integer", IntegerArgument.class, true)
			),
			List.of("command:CommandTree", "string:StringArgument"),
			List.of("command:CommandTree", "integer:IntegerArgument")
		);
	}

	@Test
	void testRegisterMergeDifferentLengthBranches() {
		new CommandTree("command")
			.then(
				new LiteralArgument("first").then(
					new StringArgument("argument").executesPlayer(P_EXEC)
				)
			)
			.then(
				new LiteralArgument("second").executesPlayer(P_EXEC)
			)
			.register();

		new CommandTree("command")
			.then(
				new LiteralArgument("first").executesPlayer(P_EXEC)
			)
			.then(
				new LiteralArgument("second").then(
					new IntegerArgument("argument").executesPlayer(P_EXEC)
				)
			)
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", false).withChildren(
				node("first", LiteralArgument.class, true).helpString("first").withChildren(
					node("argument", StringArgument.class, true)
				),
				node("second", LiteralArgument.class, true).helpString("second").withChildren(
					node("argument", IntegerArgument.class, true)
				)
			),
			List.of("command:CommandTree", "first:LiteralArgument"),
			List.of("command:CommandTree", "first:LiteralArgument", "argument:StringArgument"),
			List.of("command:CommandTree", "second:LiteralArgument"),
			List.of("command:CommandTree", "second:LiteralArgument", "argument:IntegerArgument")
		);
	}

	@Test
	void testRegisterMergeNamespaces() {
		new CommandTree("command")
			.then(new LiteralArgument("first").executesPlayer(P_EXEC))
			.withAliases("first")
			.register("first");

		new CommandTree("command")
			.then(new LiteralArgument("second").executesPlayer(P_EXEC))
			.withAliases("second")
			.register("second");

		RegisteredCommand<CommandSender> first = simpleRegisteredCommand(
			"command", "first",
			commandNode("command", false).withChildren(
				node("first", LiteralArgument.class, true).helpString("first")
			),
			"first"
		);

		RegisteredCommand<CommandSender> second = simpleRegisteredCommand(
			"command", "second",
			commandNode("command", false).withChildren(
				node("second", LiteralArgument.class, true).helpString("second")
			),
			"second"
		);

		RegisteredCommand<CommandSender> merged = simpleRegisteredCommand(
			"command", "",
			commandNode("command", false).withChildren(
				node("first", LiteralArgument.class, true).helpString("first"),
				node("second", LiteralArgument.class, true).helpString("second")
			),
			"first", "second"
		);

		assertCreatedRegisteredCommands(merged, first, second);
	}

	///////////////////
	// Unregistering //
	///////////////////
	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void testUnregister(boolean useBukkitMethod) {
		new CommandTree("command")
			.executesPlayer(P_EXEC)
			.register();

		RegisteredCommand<CommandSender> expectedCommand = simpleRegisteredCommand(
			"command", "minecraft",
			commandNode("command", true)
		);

		assertCreatedRegisteredCommands(expectedCommand.copyWithEmptyNamespace(), expectedCommand);

		if (!useBukkitMethod) {
			CommandAPI.unregister("command");
		} else {
			// CommandAPIHandler usually removes RegisteredCommands, but the extra parameter in the
			//  CommandAPIBukkit method means it has to be handled separately
			CommandAPIBukkit.unregister("command", false, false);
		}

		// Only the namespaced version should be left
		assertCreatedRegisteredCommands(expectedCommand);
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void testUnregisterNamespace(boolean useBukkitMethod) {
		new CommandTree("command")
			.executesPlayer(P_EXEC)
			.register();

		RegisteredCommand<CommandSender> expectedCommand = simpleRegisteredCommand(
			"command", "minecraft",
			commandNode("command", true)
		);

		assertCreatedRegisteredCommands(expectedCommand.copyWithEmptyNamespace(), expectedCommand);

		if (!useBukkitMethod) {
			CommandAPI.unregister("command", true);
		} else {
			// CommandAPIHandler usually removes RegisteredCommands, but the extra parameter in the
			//  CommandAPIBukkit method means it has to be handled separately
			CommandAPIBukkit.unregister("command", true, false);
		}

		// Command should be fully removed
		assertCreatedRegisteredCommands();
	}

	@Test
	void testUnregisterBukkit() {
		new CommandTree("command")
			.executesPlayer(P_EXEC)
			.register();

		RegisteredCommand<CommandSender> expectedCommand = simpleRegisteredCommand(
			"command", "minecraft",
			commandNode("command", true)
		);

		assertCreatedRegisteredCommands(expectedCommand.copyWithEmptyNamespace(), expectedCommand);

		CommandAPIBukkit.unregister("command", true, true);

		// CommandAPI commands are not Bukkit commands, so they aren't unregistered here
		assertCreatedRegisteredCommands(expectedCommand.copyWithEmptyNamespace(), expectedCommand);
	}
}
