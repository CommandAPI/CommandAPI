package dev.jorel.commandapi.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpTopic;
import org.bukkit.permissions.PermissionAttachment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import be.seeseemelk.mockbukkit.MockBukkit;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;

/**
 * Tests to do with command help
 */
class CommandHelpTests extends TestBase {

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

	private void assertHelpTopicCreated(String name, String shortDescription, String fullDescription, CommandSender forWho) {
		assertHelpTopicCreated("/" + name, name, shortDescription, fullDescription, forWho);
	}

	private void assertHelpTopicCreated(String entryName, String topicName, String shortDescription, String fullDescription, CommandSender forWho) {
		// Check the help topic was added
		HelpTopic helpTopic = server.getHelpMap().getHelpTopic(entryName);
		assertNotNull(helpTopic, "Expected to find help topic called <" + entryName + ">, but null was found.");

		// Check the help topic name
		assertEquals(topicName, helpTopic.getName());

		// Check the short description
		assertEquals(shortDescription, helpTopic.getShortText());

		// Check the full description
		assertEquals(ChatColor.translateAlternateColorCodes('&', fullDescription), helpTopic.getFullText(forWho));
	}

	/*********
	 * Tests *
	 *********/

	@Test
	void testRegisterCommandWithHelp() {
		new CommandAPICommand("test")
			.withHelp("short description", "full description")
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer("APlayer");

		// Short and full description are inserted
		assertHelpTopicCreated(
			"test", 
			"short description",
			"""
			short description
			&6Description: &ffull description
			&6Usage: &f/test""", 
			player
		);
	}
	
	@Test
	void testRegisterCommandWithShortDescription() {
		new CommandAPICommand("test")
			.withShortDescription("short description")
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer("APlayer");

		// Short description appears at the start of full text because that's how `CustomHelpTopic` works
		assertHelpTopicCreated(
			"test", 
			"short description", 
			"""
			short description
			&6Usage: &f/test""", 
			player
		);
	}

	@Test
	void testRegisterCommandFullDescription() {
		new CommandAPICommand("test")
			.withFullDescription("full description")
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer("APlayer");

		// Full description replaces short description when not specified
		assertHelpTopicCreated(
			"test", 
			"full description", 
			"""
			full description
			&6Description: &ffull description
			&6Usage: &f/test""", 
			player
		);
	}

	@Test
	void testRegisterCommandNoDescription() {
		new CommandAPICommand("test")
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer("APlayer");

		// Check default message
		assertHelpTopicCreated(
			"test", 
			"A command by the CommandAPITest plugin.", 
			"""
			A command by the CommandAPITest plugin.
			&6Usage: &f/test""", 
			player
		);
	}

	@Test
	void testRegisterWithRemovedUsage() {
		new CommandAPICommand("test")
			.withUsage()
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer("APlayer");

		// Usage can be removed
		assertHelpTopicCreated(
			"test", 
			"A command by the CommandAPITest plugin.", 
			"""
			A command by the CommandAPITest plugin.""", 
			player
		);
	}

	@Test
	void testRegisterWithOneUsage() {
		new CommandAPICommand("test")
			.withUsage("Line one")
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer("APlayer");

		// Usage generation can be overriden with one line
		assertHelpTopicCreated(
			"test", 
			"A command by the CommandAPITest plugin.", 
			"""
			A command by the CommandAPITest plugin.
			&6Usage: &fLine one""", 
			player
		);
	}

	@Test
	void testRegisterWithMultipleUsage() {
		new CommandAPICommand("test")
			.withUsage(
				"Line one",
				"Line two",
				"Line three"
			)
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer("APlayer");

		// Usage generation can be overriden with multiple lines
		assertHelpTopicCreated(
			"test", 
			"A command by the CommandAPITest plugin.", 
			"""
			A command by the CommandAPITest plugin.
			&6Usage: &f
			- Line one
			- Line two
			- Line three""", 
			player
		);
	}

	@Test
	void testRegisterDynamicShortDescription() {
		String[] shortDescription = new String[]{""};

		new CommandAPICommand("test")
			.withShortDescription(() -> Optional.of(shortDescription[0]))
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer();

		// Check changing shortDescription
		shortDescription[0] = "First description";
		assertHelpTopicCreated(
			"test", 
			"First description", 
			"""
			First description
			&6Usage: &f/test""", 
			player
		);

		shortDescription[0] = "Second description";
		assertHelpTopicCreated(
			"test", 
			"Second description", 
			"""
			Second description
			&6Usage: &f/test""", 
			player
		);
	}

	@Test
	void testRegisterDynamicFullDescription() {
		new CommandAPICommand("test")
			.withFullDescription(forWho -> {
				if (forWho == null) return Optional.empty();

				return Optional.of("Special full text just for " + (forWho instanceof Player player ? player.getName() : forWho));
			})
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player1 = server.addPlayer("Player1");
		Player player2 = server.addPlayer("Player2");

		// Check changing text for different CommandSenders
		assertHelpTopicCreated(
			"test",
			"A command by the CommandAPITest plugin.",
			"""
			A command by the CommandAPITest plugin.
			&6Description: &fSpecial full text just for Player1
			&6Usage: &f/test""",
			player1
		);
		assertHelpTopicCreated(
			"test",
			"A command by the CommandAPITest plugin.",
			"""
			A command by the CommandAPITest plugin.
			&6Description: &fSpecial full text just for Player2
			&6Usage: &f/test""",
			player2
		);
	}

	@Test
	void testRegisterDynamicUsage() {
		new CommandAPICommand("test")
			.withUsage((forWho, argumentTree) -> {
				if (forWho == null) return Optional.empty();

				return Optional.of(new String[]{
					"/test " + (forWho instanceof Player player ? player.getName() : forWho)
				});
			})
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player1 = server.addPlayer("Player1");
		Player player2 = server.addPlayer("Player2");

		// Check changing text for different CommandSenders
		assertHelpTopicCreated(
			"test",
			"A command by the CommandAPITest plugin.",
			"""
			A command by the CommandAPITest plugin.
			&6Usage: &f/test Player1""",
			player1
		);
		assertHelpTopicCreated(
			"test",
			"A command by the CommandAPITest plugin.",
			"""
			A command by the CommandAPITest plugin.
			&6Usage: &f/test Player2""",
			player2
		);
	}

	@Test
	void testRegisterCustomHelpTopic() {
		HelpTopic helpTopic = new HelpTopic() {
			{
				this.name = "custom name";
				this.shortText = "short description";
			}

			@Override
			public String getFullText(CommandSender forWho) {
				return "Special full text just for " + (forWho instanceof Player player ? player.getName() : forWho);
			}

			@Override
			public boolean canSee(CommandSender sender) {
				return true;
			}
		};

		// `CommandAPICommand#withHelp(HelpTopic)` and `CommandTree#withHelp(HelpTopic)` are separate methods, so check both for coverage
		new CommandAPICommand("commandAPICommand")
			// Generally, calls to these methods before `withHelp(HelpTopic)` will be overridden
			.withShortDescription("Overridden")
			.withFullDescription("Overridden")
			.withHelp("Overridden", "Overridden")
			.withUsage("Overridden")
			.withShortDescription(() -> Optional.of("Overridden"))
			.withFullDescription(forWho -> Optional.of("Overidden"))
			.withUsage((forWho, argumentTree) -> Optional.of(new String[]{"Overidden"}))
			// Add the custom help
			.withHelp(helpTopic)
			// Generally, calls to these methods after `withHelp(HelpTopic)` should be ignored
			.withShortDescription("Overridden")
			.withFullDescription("Overridden")
			.withHelp("Overridden", "Overridden")
			.withUsage("Overridden")
			.withShortDescription(() -> Optional.of("Overridden"))
			.withFullDescription(forWho -> Optional.of("Overidden"))
			.withUsage((forWho, argumentTree) -> Optional.of(new String[]{"Overidden"}))
			// Finish building
			.withAliases("alias") // Also check alias
			.executesPlayer(P_EXEC)
			.register();

		new CommandTree("commandTree")
			// Generally, calls to these methods before `withHelp(HelpTopic)` will be overridden
			.withShortDescription("Overridden")
			.withFullDescription("Overridden")
			.withHelp("Overridden", "Overridden")
			.withUsage("Overridden")
			.withShortDescription(() -> Optional.of("Overridden"))
			.withFullDescription(forWho -> Optional.of("Overidden"))
			.withUsage((forWho, argumentTree) -> Optional.of(new String[]{"Overidden"}))
			// Add the custom help
			.withHelp(helpTopic)
			// Generally, calls to these methods after `withHelp(HelpTopic)` should be ignored
			.withShortDescription("Overridden")
			.withFullDescription("Overridden")
			.withHelp("Overridden", "Overridden")
			.withUsage("Overridden")
			.withShortDescription(() -> Optional.of("Overridden"))
			.withFullDescription(forWho -> Optional.of("Overidden"))
			.withUsage((forWho, argumentTree) -> Optional.of(new String[]{"Overidden"}))
			// Finish building
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player1 = server.addPlayer("Player1");
		Player player2 = server.addPlayer("Player2");

		// Custom HelpTopic allows changing text for different CommandSenders
		assertHelpTopicCreated(
			"/commandAPICommand", 
			"custom name",
			"short description",
			"""
			Special full text just for Player1""", 
			player1
		);
		assertHelpTopicCreated(
			"/commandAPICommand", 
			"custom name",
			"short description",
			"""
			Special full text just for Player2""", 
			player2
		);
		assertHelpTopicCreated(
			"/commandTree", 
			"custom name",
			"short description",
			"""
			Special full text just for Player1""", 
			player1
		);
		assertHelpTopicCreated(
			"/commandTree", 
			"custom name",
			"short description",
			"""
			Special full text just for Player2""", 
			player2
		);

		// Aliases should also use the custom HelpTopic
		assertHelpTopicCreated(
			"/alias", 
			"custom name",
			"short description",
			"""
			Special full text just for Player1""", 
			player1
		);
		assertHelpTopicCreated(
			"/alias", 
			"custom name",
			"short description",
			"""
			Special full text just for Player2""", 
			player2
		);
	}

	@Test
	void testRegisterCommandWithHelpWithAliases() {
		new CommandAPICommand("test")
			.withHelp("short description", "full description")
			.withAliases("othertest", "othercommand")
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer("APlayer");

		// Check the main help topic was added
		assertHelpTopicCreated(
			"test", 
			"short description", 
			"""
			short description
			&6Description: &ffull description
			&6Usage: &f/test
			&6Aliases: &fothertest, othercommand""", 
			player
		);

		// Check the alias topics
		//  The alias section of each alias does not include itself, but does include the main name
		//  Otherwise everything is the same as the main command
		assertHelpTopicCreated(
			"othertest", 
			"short description", 
			"""
			short description
			&6Description: &ffull description
			&6Usage: &f/test
			&6Aliases: &fothercommand, test""", 
			player
		);
		assertHelpTopicCreated(
			"othercommand", 
			"short description", 
			"""
			short description
			&6Description: &ffull description
			&6Usage: &f/test
			&6Aliases: &fothertest, test""", 
			player
		);
	}
	
	@Test
	void testRegisterCommandWithMultipleArguments() {
		new CommandAPICommand("test")
			.withHelp("short description", "full description")
			.withArguments(new StringArgument("arg1"))
			.withArguments(new IntegerArgument("arg2"))
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer("APlayer");

		// Multiple arguments are stacked in the usage
		assertHelpTopicCreated(
			"test", 
			"short description", 
			"""
			short description
			&6Description: &ffull description
			&6Usage: &f/test <arg1> <arg2>""", 
			player
		);
	}

	@Test
	void testRegisterMultipleCommands() {
		new CommandAPICommand("test")
			.withHelp("short description 1", "full description 1")
			.withArguments(new StringArgument("arg1"))
			.executesPlayer(P_EXEC)
			.register();

		new CommandAPICommand("test")
			.withHelp("short description 2", "full description 2")
			.withArguments(new StringArgument("arg1"))
			.withArguments(new IntegerArgument("arg2"))
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer("APlayer");

		// The first command registered gets priority, but usage should be merged
		assertHelpTopicCreated(
			"test", 
			"short description 1", 
			"""
			short description 1
			&6Description: &ffull description 1
			&6Usage: &f
			- /test <arg1>
			- /test <arg1> <arg2>""", 
			player
		);
	}

	@Test
	void testRegisterDeepBranches() {
		new CommandTree("test")
			.then(
				new LiteralArgument("branch1") // `/tree branch1` should not show up since it is not executable
					.then(new StringArgument("string1").executesPlayer(P_EXEC))
					.then(new IntegerArgument("integer1").executesPlayer(P_EXEC))
			)
			.then(
				new LiteralArgument("branch2").executesPlayer(P_EXEC)
					.then(new StringArgument("string2").executesPlayer(P_EXEC))
					.then(
						new IntegerArgument("integer2")
							.then(new LiteralArgument("continue").executesPlayer(P_EXEC))
					)
			)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer("APlayer");

		// Each executable node should appear in the usage
		assertHelpTopicCreated(
			"test", 
			"A command by the CommandAPITest plugin.", 
			"""
			A command by the CommandAPITest plugin.
			&6Usage: &f
			- /test branch1 <string1>
			- /test branch1 <integer1>
			- /test branch2
			- /test branch2 <string2>
			- /test branch2 <integer2> continue""", 
			player
		);
	}

	@Test
	void testRegisterLiteralArguments() {
		new CommandAPICommand("test")
			.withArguments(
				new MultiLiteralArgument("multiLiteral", "a", "b", "c"),
				new LiteralArgument("literal", "d"),
				new StringArgument("string")
			)
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer("APlayer");

		// Literal and MultiLiteral arguments have different help strings, using thier literals rather than thier node name
		assertHelpTopicCreated(
			"test", 
			"A command by the CommandAPITest plugin.", 
			"""
			A command by the CommandAPITest plugin.
			&6Usage: &f/test (a|b|c) d <string>""", 
			player
		);
	}

	@Test
	void testRegisterAfterServerEnabled() {
		// Enable server early
		enableServer();

		// `CommandAPIBukkit#postCommandRegistration` should still register the help topics
		new CommandAPICommand("test")
			.withHelp("short description", "full description")
			.withUsage("usage line")
			.withAliases("alias")
			.executesPlayer(P_EXEC)
			.register();

		// It is important to add the player after calling 	`.register`
		//  `CommandAPIBukkit#postCommandRegistration` also calls `Player#updateCommands`,
		//  which throws an `UnimplementedOperationException` for `PlayerMock`
		Player player = server.addPlayer("APlayer");

		// Ensure main and alias help topics exist
		assertHelpTopicCreated(
			"test", 
			"short description",
			"""
			short description
			&6Description: &ffull description
			&6Usage: &fusage line
			&6Aliases: &falias""", 
			player
		);
		assertHelpTopicCreated(
			"alias", 
			"short description",
			"""
			short description
			&6Description: &ffull description
			&6Usage: &fusage line
			&6Aliases: &ftest""", 
			player
		);
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	// Test enabling before and after registering since these take different code paths
	void testRegisterMergeNamespaces(boolean enableBeforeRegistering) {
		if (enableBeforeRegistering) enableServer();

		new CommandAPICommand("test")
			.withHelp("first short", "first full")
			.withArguments(new LiteralArgument("first"))
			.executesPlayer(P_EXEC)
			.withAliases("first")
			.register("first");

		new CommandAPICommand("test")
			.withHelp("second short", "second full")
			.withArguments(new LiteralArgument("second"))
			.executesPlayer(P_EXEC)
			.withAliases("second")
			.register("second");

		if (!enableBeforeRegistering) enableServer();

		Player player = server.addPlayer("APlayer");

		// Unnamespaced help should merge usage and aliases together, which is how the command appears for execution
		//  The command registered first determines the descriptions
		assertHelpTopicCreated(
			"test", 
			"first short", 
			"""
			first short
			&6Description: &ffirst full
			&6Usage: &f
			- /test first
			- /test second
			&6Aliases: &ffirst, second""", 
			player
		);
		assertHelpTopicCreated(
			"first", 
			"first short", 
			"""
			first short
			&6Description: &ffirst full
			&6Usage: &f
			- /test first
			- /test second
			&6Aliases: &fsecond, test""", 
			player
		);
		assertHelpTopicCreated(
			"second", 
			"first short", 
			"""
			first short
			&6Description: &ffirst full
			&6Usage: &f
			- /test first
			- /test second
			&6Aliases: &ffirst, test""", 
			player
		);

		// Namespaced version of help should remain separated
		assertHelpTopicCreated(
			"first:test", 
			"first short", 
			"""
			first short
			&6Description: &ffirst full
			&6Usage: &f/test first
			&6Aliases: &ffirst""",
			player
		);
		assertHelpTopicCreated(
			"first:first", 
			"first short", 
			"""
			first short
			&6Description: &ffirst full
			&6Usage: &f/test first
			&6Aliases: &ftest""",
			player
		);
		assertNull(server.getHelpMap().getHelpTopic("/first:second"));

		assertHelpTopicCreated(
			"second:test", 
			"second short", 
			"""
			second short
			&6Description: &fsecond full
			&6Usage: &f/test second
			&6Aliases: &fsecond""",
			player
		);
		assertHelpTopicCreated(
			"second:second", 
			"second short", 
			"""
			second short
			&6Description: &fsecond full
			&6Usage: &f/test second
			&6Aliases: &ftest""",
			player
		);
		assertNull(server.getHelpMap().getHelpTopic("/second:first"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"minecraft", "custom"})
	// Test with the default ("minecraft") and a custom namespace
	void testRegisterConflictWithPluginCommand(String namespace) {
		// Register plugin command
		// MockBukkit does not simulate `org.bukkit.craftbukkit.help.SimpleHelpMap#initializeCommands`, 
		//  which would usually create a help topic for our plugin command.
		//  This is acutally kinda useful, because we can make sure we wouldn't override the help topic
		//  by making sure a help entry is still not there.
		MockBukkit.loadWith(CommandHelpTestsPlugin.class, CommandHelpTestsPlugin.pluginYaml());

		// Register conflicting CommandAPI commands
		new CommandAPICommand("registeredCommand")
			.withAliases("unregisteredAlias")
			.executesPlayer(P_EXEC)
			.register(namespace);

		new CommandAPICommand("unregisteredCommand")
			.withAliases("registeredAlias")
			.executesPlayer(P_EXEC)
			.register(namespace);

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer("APlayer");

		// Unnamespaced help topics are taken by the plugin command by default
		assertNull(server.getHelpMap().getHelpTopic("/registeredCommand"));
		assertNull(server.getHelpMap().getHelpTopic("/registeredAlias"));

		// Commands that don't conflict still show up unnamespaced
		assertHelpTopicCreated(
			"unregisteredCommand", 
			"A command by the CommandAPITest plugin.", 
			"""
			A command by the CommandAPITest plugin.
			&6Usage: &f/unregisteredCommand
			&6Aliases: &fregisteredAlias""",
			player
		);
		assertHelpTopicCreated(
			"unregisteredAlias", 
			"A command by the CommandAPITest plugin.", 
			"""
			A command by the CommandAPITest plugin.
			&6Usage: &f/registeredCommand
			&6Aliases: &fregisteredCommand""",
			player
		);

		// Our help topics defer to the namespaced version so they're at least accessible somewhere
		assertHelpTopicCreated(
			namespace + ":registeredCommand", 
			"A command by the CommandAPITest plugin.", 
			"""
			A command by the CommandAPITest plugin.
			&6Usage: &f/registeredCommand
			&6Aliases: &funregisteredAlias""",
			player
		);
		assertHelpTopicCreated(
			namespace + ":registeredAlias", 
			"A command by the CommandAPITest plugin.", 
			"""
			A command by the CommandAPITest plugin.
			&6Usage: &f/unregisteredCommand
			&6Aliases: &funregisteredCommand""",
			player
		);

		// Namespaced versions of commands that don't conflict also show up as usual
		assertHelpTopicCreated(
			namespace + ":unregisteredCommand", 
			"A command by the CommandAPITest plugin.", 
			"""
			A command by the CommandAPITest plugin.
			&6Usage: &f/unregisteredCommand
			&6Aliases: &fregisteredAlias""",
			player
		);
		assertHelpTopicCreated(
			namespace + ":unregisteredAlias", 
			"A command by the CommandAPITest plugin.", 
			"""
			A command by the CommandAPITest plugin.
			&6Usage: &f/registeredCommand
			&6Aliases: &fregisteredCommand""",
			player
		);
	}

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	// Test with permission normal and negated
	void testOpPermissionsHidingHelp(boolean needsOP) {
		CommandPermission permission = CommandPermission.OP;
		permission = needsOP ? permission : permission.negate();

		new CommandAPICommand("opCommand")
			.withPermission(permission)
			.executesPlayer(P_EXEC)
			.register();

		new CommandTree("opArgument")
			.then(new StringArgument("string").withPermission(permission).executesPlayer(P_EXEC))
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer("APlayer");

		// Player dose not have permission
		player.setOp(!needsOP);

		// Without permission, usage is hidden
		assertHelpTopicCreated(
			"opCommand", 
			"A command by the CommandAPITest plugin.", 
			"""
			A command by the CommandAPITest plugin.""", 
			player
		);
		assertHelpTopicCreated(
			"opArgument", 
			"A command by the CommandAPITest plugin.", 
			"""
			A command by the CommandAPITest plugin.
			&6Usage: &f/opArgument""", 
			player
		);

		// `canSee` just checks the root node
		assertFalse(server.getHelpMap().getHelpTopic("/opCommand").canSee(player));
		assertTrue(server.getHelpMap().getHelpTopic("/opArgument").canSee(player));

		// Player has permission
		player.setOp(needsOP);

		// With permission, usage is visible
		assertHelpTopicCreated(
			"opCommand", 
			"A command by the CommandAPITest plugin.", 
			"""
			A command by the CommandAPITest plugin.
			&6Usage: &f/opCommand""", 
			player
		);
		assertHelpTopicCreated(
			"opArgument", 
			"A command by the CommandAPITest plugin.", 
			"""
			A command by the CommandAPITest plugin.
			&6Usage: &f
			- /opArgument
			- /opArgument <string>""", 
			player
		);

		assertTrue(server.getHelpMap().getHelpTopic("/opCommand").canSee(player));
		assertTrue(server.getHelpMap().getHelpTopic("/opArgument").canSee(player));
	}

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	// Test with permission normal and negated
	void testStringPermissionsHidingHelp(boolean needsPermission) {
		CommandPermission permission = CommandPermission.fromString("permission");
		permission = needsPermission ? permission : permission.negate();

		new CommandAPICommand("permissionCommand")
			.withPermission(permission)
			.executesPlayer(P_EXEC)
			.register();

		new CommandTree("permissionArgument")
			.then(new StringArgument("string").withPermission(permission).executesPlayer(P_EXEC))
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer("APlayer");
		PermissionAttachment permissionAttachment = player.addAttachment(super.plugin);

		// Player dose not have permission
		permissionAttachment.setPermission("permission", !needsPermission);
		player.recalculatePermissions();

		// Without permission, usage is hidden
		assertHelpTopicCreated(
			"permissionCommand", 
			"A command by the CommandAPITest plugin.", 
			"""
			A command by the CommandAPITest plugin.""", 
			player
		);
		assertHelpTopicCreated(
			"permissionArgument", 
			"A command by the CommandAPITest plugin.", 
			"""
			A command by the CommandAPITest plugin.
			&6Usage: &f/permissionArgument""", 
			player
		);

		// `canSee` just checks the root node
		assertFalse(server.getHelpMap().getHelpTopic("/permissionCommand").canSee(player));
		assertTrue(server.getHelpMap().getHelpTopic("/permissionArgument").canSee(player));

		// Player has permission
		permissionAttachment.setPermission("permission", needsPermission);
		player.recalculatePermissions();

		// With permission, usage is visible
		assertHelpTopicCreated(
			"permissionCommand", 
			"A command by the CommandAPITest plugin.", 
			"""
			A command by the CommandAPITest plugin.
			&6Usage: &f/permissionCommand""", 
			player
		);
		assertHelpTopicCreated(
			"permissionArgument", 
			"A command by the CommandAPITest plugin.", 
			"""
			A command by the CommandAPITest plugin.
			&6Usage: &f
			- /permissionArgument
			- /permissionArgument <string>""", 
			player
		);

		assertTrue(server.getHelpMap().getHelpTopic("/permissionCommand").canSee(player));
		assertTrue(server.getHelpMap().getHelpTopic("/permissionArgument").canSee(player));
	}
}
