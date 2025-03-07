package dev.jorel.commandapi.test;

import dev.jorel.commandapi.*;
import dev.jorel.commandapi.RegisteredCommand.Node;
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

import static dev.jorel.commandapi.test.RegisteredCommandTestBase.NodeBuilder.children;
import static dev.jorel.commandapi.test.RegisteredCommandTestBase.NodeBuilder.node;

/**
 * Tests for making sure the {@link RegisteredCommand} information is correct when registering {@link CommandAPICommand}s
 */
class CommandAPICommandRegisteredCommandTests extends RegisteredCommandTestBase {

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
		return node(nodeName, CommandAPICommand.class, executable).helpString(nodeName);
	}

	/*********
	 * Tests *
	 *********/

	@Test
	void testRegister() {
		new CommandAPICommand("command")
			.executesPlayer(P_EXEC)
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", true),
			List.of("command:CommandAPICommand")
		);
	}

	@Test
	void testRegisterHelpInformation() {
		new CommandAPICommand("command")
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
		new CommandAPICommand("command")
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
		new CommandAPICommand("command")
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

		new CommandAPICommand("command")
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
		new CommandAPICommand("command")
			.withAliases("alias1")
			.executesPlayer(P_EXEC)
			.register();

		RegisteredCommand<CommandSender> expectedCommand = simpleRegisteredCommand(
			"command", "minecraft",
			commandNode("command", true),
			"alias1"
		);

		assertCreatedRegisteredCommands(expectedCommand.copyWithEmptyNamespace(), expectedCommand);
	}

	@Test
	void testRegisterTwoAliases() {
		new CommandAPICommand("command")
			.withAliases("alias1", "alias2")
			.executesPlayer(P_EXEC)
			.register();

		RegisteredCommand<CommandSender> expectedCommand = simpleRegisteredCommand(
			"command", "minecraft",
			commandNode("command", true),
			"alias1", "alias2"
		);

		assertCreatedRegisteredCommands(expectedCommand.copyWithEmptyNamespace(), expectedCommand);
	}

	@Test
	void testRegisterNamespace() {
		new CommandAPICommand("command")
			.executesPlayer(P_EXEC)
			.register("custom");

		RegisteredCommand<CommandSender> expectedCommand = simpleRegisteredCommand(
			"command", "custom",
			commandNode("command", true)
		);

		assertCreatedRegisteredCommands(expectedCommand.copyWithEmptyNamespace(), expectedCommand);
	}

	@Test
	void testRegisterOneArgument() {
		new CommandAPICommand("command")
			.withArguments(new StringArgument("string"))
			.executesPlayer(P_EXEC)
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", false)
				.withChildren(node("string", StringArgument.class, true)),
			List.of("command:CommandAPICommand", "string:StringArgument")
		);
	}

	@Test
	void testRegisterTwoArguments() {
		new CommandAPICommand("command")
			.withArguments(
				new StringArgument("string"),
				new IntegerArgument("integer")
			)
			.executesPlayer(P_EXEC)
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", false)
				.withChildren(node("string", StringArgument.class, false)
					.withChildren(node("integer", IntegerArgument.class, true))
				),
			List.of("command:CommandAPICommand", "string:StringArgument", "integer:IntegerArgument")
		);
	}

	@Test
	void testRegisterArgumentPermissions() {
		new CommandAPICommand("command")
			.withArguments(
				new StringArgument("noPermission"),
				new StringArgument("opPermission").withPermission(CommandPermission.OP),
				new StringArgument("stringPermission").withPermission(CommandPermission.fromString("permission"))
			)
			.executesPlayer(P_EXEC)
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", false).withChildren(
				node("noPermission", StringArgument.class, false).withChildren(
				node("opPermission", StringArgument.class, false).permission(CommandPermission.OP).withChildren(
				node("stringPermission", StringArgument.class, true).permission(CommandPermission.fromString("permission"))
			))),
			List.of("command:CommandAPICommand", "noPermission:StringArgument", "opPermission:StringArgument", "stringPermission:StringArgument")
		);
	}

	@Test
	void testRegisterArgumentRequirement() {
		Predicate<CommandSender> requirement = sender -> sender instanceof Player;

		new CommandAPICommand("command")
			.withArguments(new StringArgument("string").withRequirement(requirement))
			.executesPlayer(P_EXEC)
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", false).withChildren(
				node("string", StringArgument.class, true).requirements(requirement)
			),
			List.of("command:CommandAPICommand", "string:StringArgument")
		);
	}

	@Test
	void testRegisterMultiLiteralArguments() {
		new CommandAPICommand("command")
			.withArguments(
				new MultiLiteralArgument("literal1", "a", "b", "c"),
				new MultiLiteralArgument("literal2", "d", "e", "f")
			)
			.executesPlayer(P_EXEC)
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", false).withChildren(
				node("literal1", MultiLiteralArgument.class, false).helpString("(a|b|c)").withChildren(
					node("literal2", MultiLiteralArgument.class, true).helpString("(d|e|f)")
				)
			),
			List.of("command:CommandAPICommand", "literal1:MultiLiteralArgument", "literal2:MultiLiteralArgument")
		);
	}

	@Test
	void testRegisterOneOptionalArgument() {
		new CommandAPICommand("command")
			.withOptionalArguments(new StringArgument("string"))
			.executesPlayer(P_EXEC)
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", true)
				.withChildren(node("string", StringArgument.class, true)),
			List.of("command:CommandAPICommand"),
			List.of("command:CommandAPICommand", "string:StringArgument")
		);
	}

	@Test
	void testRegisterTwoOptionalArguments() {
		new CommandAPICommand("command")
			.withOptionalArguments(
				new StringArgument("string"),
				new IntegerArgument("integer")
			)
			.executesPlayer(P_EXEC)
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", true)
				.withChildren(node("string", StringArgument.class, true)
					.withChildren(node("integer", IntegerArgument.class, true))
				),
			List.of("command:CommandAPICommand"),
			List.of("command:CommandAPICommand", "string:StringArgument"),
			List.of("command:CommandAPICommand", "string:StringArgument", "integer:IntegerArgument")
		);
	}

	@Test
	void testRegisterCombinedOptionalArguments() {
		new CommandAPICommand("command")
			.withOptionalArguments(
				new LiteralArgument("1").combineWith(new LiteralArgument("2")),
				new LiteralArgument("3").combineWith(new LiteralArgument("4"))
			)
			.executesPlayer(P_EXEC)
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", true)
				.withChildren(node("1", LiteralArgument.class, false).helpString("1")
				.withChildren(node("2", LiteralArgument.class, true).helpString("2")
				.withChildren(node("3", LiteralArgument.class, false).helpString("3")
				.withChildren(node("4", LiteralArgument.class, true).helpString("4")
			)))),
			List.of("command:CommandAPICommand"),
			List.of("command:CommandAPICommand", "1:LiteralArgument", "2:LiteralArgument"),
			List.of("command:CommandAPICommand", "1:LiteralArgument", "2:LiteralArgument", "3:LiteralArgument", "4:LiteralArgument")
		);
	}

	@Test
	void testRegisterCombinedRequiredAndOptionalArguments() {
		new CommandAPICommand("command")
			.withArguments(
				new LiteralArgument("1").combineWith(new LiteralArgument("2")),
				new LiteralArgument("3").combineWith(new LiteralArgument("4"))
			)
			.withOptionalArguments(
				new LiteralArgument("5").combineWith(new LiteralArgument("6")),
				new LiteralArgument("7").combineWith(new LiteralArgument("8"))
			)
			.executesPlayer(P_EXEC)
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
			List.of("command:CommandAPICommand",
				"1:LiteralArgument", "2:LiteralArgument", "3:LiteralArgument", "4:LiteralArgument"
			),
			List.of("command:CommandAPICommand",
				"1:LiteralArgument", "2:LiteralArgument", "3:LiteralArgument", "4:LiteralArgument",
				"5:LiteralArgument", "6:LiteralArgument"
			),
			List.of("command:CommandAPICommand",
				"1:LiteralArgument", "2:LiteralArgument", "3:LiteralArgument", "4:LiteralArgument",
				"5:LiteralArgument", "6:LiteralArgument", "7:LiteralArgument", "8:LiteralArgument"
			)
		);
	}

	//////////////////////////////////////
	// SUBCOMMANDS                      //
	// The same as commands, but deeper //
	//////////////////////////////////////

	@Test
	void testRegisterOneSubcommand() {
		new CommandAPICommand("command")
			.withSubcommand(
				new CommandAPICommand("subcommand")
					.executesPlayer(P_EXEC)
			)
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", false)
				.withChildren(commandNode("subcommand", true)),
			List.of("command:CommandAPICommand", "subcommand:CommandAPICommand")
		);
	}

	@Test
	void testRegisterTwoSubcommands() {
		new CommandAPICommand("command")
			.withSubcommands(
				new CommandAPICommand("subcommand1")
					.executesPlayer(P_EXEC),
				new CommandAPICommand("subcommand2")
					.executesPlayer(P_EXEC)
			)
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", false).withChildren(
				commandNode("subcommand1", true),
				commandNode("subcommand2", true)
			),
			List.of("command:CommandAPICommand", "subcommand1:CommandAPICommand"),
			List.of("command:CommandAPICommand", "subcommand2:CommandAPICommand")
		);
	}

	@Test
	void testRegisterOneSubcommandAndBaseExecutable() {
		new CommandAPICommand("command")
			.executesPlayer(P_EXEC)
			.withSubcommand(
				new CommandAPICommand("subcommand")
					.executesPlayer(P_EXEC)
			)
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", true)
				.withChildren(commandNode("subcommand", true)),
			List.of("command:CommandAPICommand"),
			List.of("command:CommandAPICommand", "subcommand:CommandAPICommand")
		);
	}

	@Test
	void testRegisterSubcommandWithAliases() {
		new CommandAPICommand("command")
			.withSubcommand(
				new CommandAPICommand("subcommand")
					.withAliases("alias1", "alias2")
					.executesPlayer(P_EXEC)
			)
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", false).withChildren(
				commandNode("subcommand", true),
				commandNode("alias1", true),
				commandNode("alias2", true)
			),
			List.of("command:CommandAPICommand", "subcommand:CommandAPICommand"),
			List.of("command:CommandAPICommand", "alias1:CommandAPICommand"),
			List.of("command:CommandAPICommand", "alias2:CommandAPICommand")
		);
	}

	@Test
	void testRegisterSubcommandsWithArguments() {
		new CommandAPICommand("command")
			.withSubcommands(
				new CommandAPICommand("subcommand1")
					.withArguments(
						new StringArgument("string1"),
						new IntegerArgument("integer1")
					)
					.executesPlayer(P_EXEC),
				new CommandAPICommand("subcommand2")
					.withArguments(
						new StringArgument("string2"),
						new IntegerArgument("integer2")
					)
					.executesPlayer(P_EXEC)
			)
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", false).withChildren(
				commandNode("subcommand1", false).withChildren(
					node("string1", StringArgument.class, false).withChildren(
						node("integer1", IntegerArgument.class, true)
					)
				),
				commandNode("subcommand2", false).withChildren(
					node("string2", StringArgument.class, false).withChildren(
						node("integer2", IntegerArgument.class, true)
					)
				)
			),
			List.of("command:CommandAPICommand", "subcommand1:CommandAPICommand", "string1:StringArgument", "integer1:IntegerArgument"),
			List.of("command:CommandAPICommand", "subcommand2:CommandAPICommand", "string2:StringArgument", "integer2:IntegerArgument")
		);
	}

	@Test
	void testRegisterSubcommandWithAliasesAndMultiLiteralArgument() {
		new CommandAPICommand("command")
			.withSubcommand(
				new CommandAPICommand("subcommand")
					.withAliases("alias1", "alias2")
					.withArguments(
						new MultiLiteralArgument("literal1", "a", "b"),
						new MultiLiteralArgument("literal2", "c", "d")
					)
					.executesPlayer(P_EXEC)
			)
			.register();

		List<Node<CommandSender>> literal2 = children(
			node("literal2", MultiLiteralArgument.class, true).helpString("(c|d)")
		);

		List<Node<CommandSender>> literal1 = children(
			node("literal1", MultiLiteralArgument.class, false).helpString("(a|b)").withChildren(literal2)
		);

		List<Node<CommandSender>> subcommands = children(
			commandNode("subcommand", false).withChildren(literal1),
			commandNode("alias1", false).withChildren(literal1),
			commandNode("alias2", false).withChildren(literal1)
		);

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", false).withChildren(subcommands),
			List.of("command:CommandAPICommand", "subcommand:CommandAPICommand", "literal1:MultiLiteralArgument", "literal2:MultiLiteralArgument"),
			List.of("command:CommandAPICommand", "alias1:CommandAPICommand", "literal1:MultiLiteralArgument", "literal2:MultiLiteralArgument"),
			List.of("command:CommandAPICommand", "alias2:CommandAPICommand", "literal1:MultiLiteralArgument", "literal2:MultiLiteralArgument")
		);
	}

	@Test
	void testRegisterSubcommandsWithOptionalArguments() {
		new CommandAPICommand("command")
			.withSubcommand(
				new CommandAPICommand("subcommand1")
					.withOptionalArguments(
						new LiteralArgument("a"),
						new LiteralArgument("b")
					)
					.executesPlayer(P_EXEC)
			)
			.withSubcommand(
				new CommandAPICommand("subcommand2")
					.withOptionalArguments(
						new LiteralArgument("c"),
						new LiteralArgument("d")
					)
					.executesPlayer(P_EXEC)
			)
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", false).withChildren(
				commandNode("subcommand1", true).withChildren(
					node("a", LiteralArgument.class, true).helpString("a").withChildren(
						node("b", LiteralArgument.class, true).helpString("b")
					)
				),
				commandNode("subcommand2", true).withChildren(
					node("c", LiteralArgument.class, true).helpString("c").withChildren(
						node("d", LiteralArgument.class, true).helpString("d")
					)
				)
			),
			List.of("command:CommandAPICommand", "subcommand1:CommandAPICommand"),
			List.of("command:CommandAPICommand", "subcommand1:CommandAPICommand", "a:LiteralArgument"),
			List.of("command:CommandAPICommand", "subcommand1:CommandAPICommand", "a:LiteralArgument", "b:LiteralArgument"),
			List.of("command:CommandAPICommand", "subcommand2:CommandAPICommand"),
			List.of("command:CommandAPICommand", "subcommand2:CommandAPICommand", "c:LiteralArgument"),
			List.of("command:CommandAPICommand", "subcommand2:CommandAPICommand", "c:LiteralArgument", "d:LiteralArgument")
		);
	}

	@Test
	void testRegisterSubcommandsWithCombinedRequiredAndOptionalArguments() {
		new CommandAPICommand("command")
			.withSubcommand(
				new CommandAPICommand("subcommand1")
					.withArguments(
						new LiteralArgument("1a").combineWith(new LiteralArgument("1b"))
					)
					.withOptionalArguments(
						new LiteralArgument("1c").combineWith(new LiteralArgument("1d"))
					)
					.executesPlayer(P_EXEC)
			)
			.withSubcommand(
				new CommandAPICommand("subcommand2")
					.withArguments(
						new LiteralArgument("2a").combineWith(new LiteralArgument("2b"))
					)
					.withOptionalArguments(
						new LiteralArgument("2c").combineWith(new LiteralArgument("2d"))
					)
					.executesPlayer(P_EXEC)
			)
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", false).withChildren(
				commandNode("subcommand1", false)
					.withChildren(node("1a", LiteralArgument.class, false).helpString("1a")
					.withChildren(node("1b", LiteralArgument.class, true).helpString("1b")
					.withChildren(node("1c", LiteralArgument.class, false).helpString("1c")
					.withChildren(node("1d", LiteralArgument.class, true).helpString("1d")
				)))),
				commandNode("subcommand2", false)
					.withChildren(node("2a", LiteralArgument.class, false).helpString("2a")
					.withChildren(node("2b", LiteralArgument.class, true).helpString("2b")
					.withChildren(node("2c", LiteralArgument.class, false).helpString("2c")
					.withChildren(node("2d", LiteralArgument.class, true).helpString("2d")
				))))
			),
			List.of("command:CommandAPICommand", "subcommand1:CommandAPICommand", "1a:LiteralArgument", "1b:LiteralArgument"),
			List.of("command:CommandAPICommand", "subcommand1:CommandAPICommand", "1a:LiteralArgument", "1b:LiteralArgument", "1c:LiteralArgument", "1d:LiteralArgument"),
			List.of("command:CommandAPICommand", "subcommand2:CommandAPICommand", "2a:LiteralArgument", "2b:LiteralArgument"),
			List.of("command:CommandAPICommand", "subcommand2:CommandAPICommand", "2a:LiteralArgument", "2b:LiteralArgument", "2c:LiteralArgument", "2d:LiteralArgument")
		);
	}

	/////////////////////////
	// Information merging //
	/////////////////////////

	@Test
	void testRegisterTwoSeparateCommands() {
		new CommandAPICommand("command1")
			.executesPlayer(P_EXEC)
			.register();

		new CommandAPICommand("command2")
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
		new CommandAPICommand("command")
			.withArguments(new StringArgument("string"))
			.executesPlayer(P_EXEC)
			.register();

		new CommandAPICommand("command")
			.withArguments(new IntegerArgument("integer"))
			.executesPlayer(P_EXEC)
			.register();

		assertCreatedSimpleRegisteredCommand(
			"command",
			commandNode("command", false).withChildren(
				node("string", StringArgument.class, true),
				node("integer", IntegerArgument.class, true)
			),
			List.of("command:CommandAPICommand", "string:StringArgument"),
			List.of("command:CommandAPICommand", "integer:IntegerArgument")
		);
	}

	@Test
	void testRegisterMergeNamespaces() {
		new CommandAPICommand("command")
			.withArguments(new LiteralArgument("first"))
			.executesPlayer(P_EXEC)
			.withAliases("first")
			.register("first");

		new CommandAPICommand("command")
			.withArguments(new LiteralArgument("second"))
			.executesPlayer(P_EXEC)
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
		new CommandAPICommand("command")
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
		new CommandAPICommand("command")
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
		new CommandAPICommand("command")
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
