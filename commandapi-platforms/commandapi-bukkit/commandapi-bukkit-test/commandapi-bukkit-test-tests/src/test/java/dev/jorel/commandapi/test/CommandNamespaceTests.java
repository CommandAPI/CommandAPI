package dev.jorel.commandapi.test;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.RootCommandNode;
import dev.jorel.commandapi.*;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;

/**
 * Tests for testing if namespaces work correctly
 */
public class CommandNamespaceTests extends TestBase {

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

	Player enableWithNamespaces() {
		// Run the CommandAPI's enable tasks, especially `fixNamespaces`
		enableServer();

		// Get a CraftPlayer for running VanillaCommandWrapper commands
		Player runCommandsPlayer = server.addCraftPlayer();

		// Ensure player can have permissions modified
		PermissibleBase perm = new PermissibleBase(runCommandsPlayer);
		Mockito.when(runCommandsPlayer.addAttachment(isA(Plugin.class))).thenAnswer(invocation ->
			perm.addAttachment(invocation.getArgument(0))
		);
		Mockito.when(runCommandsPlayer.addAttachment(isA(Plugin.class), isA(String.class), isA(Boolean.class))).thenAnswer(invocation ->
			perm.addAttachment(invocation.getArgument(0), invocation.getArgument(1), invocation.getArgument(2))
		);
		Mockito.doAnswer(invocation -> {
			perm.recalculatePermissions();
			return null;
		}).when(runCommandsPlayer).recalculatePermissions();
		Mockito.when(runCommandsPlayer.hasPermission(isA(String.class))).thenAnswer(invocation ->
			perm.hasPermission(invocation.getArgument(0, String.class))
		);

		return runCommandsPlayer;
	}

	/*********
	 * Tests *
	 *********/
	@Test
	public void testNullNamespaceWithCommandAPICommand() {
		// Registering a command using null should fail
		CommandAPICommand command = new CommandAPICommand("test").executesPlayer(P_EXEC);
		assertThrowsWithMessage(
			NullPointerException.class,
			"Parameter 'namespace' was null when registering command /test!",
			() -> command.register((String) null)
		);
	}

	@Test
	public void testNullNamespaceWithCommandTree() {
		// Registering a command using null should fail
		CommandTree commandTree = new CommandTree("test").executesPlayer(P_EXEC);
		assertThrowsWithMessage(
			NullPointerException.class,
			"Parameter 'namespace' was null when registering command /test!",
			() -> commandTree.register((String) null)
		);
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	public void testDefaultMinecraftNamespace(boolean enableBeforeRegistering) {
		Mut<String> results = Mut.of();

		Player player = null;
		if (enableBeforeRegistering) {
			player = enableWithNamespaces();
		}

		CommandAPICommand command = new CommandAPICommand("test")
			.withArguments(new StringArgument("string"))
			.executesPlayer(info -> {
				results.set(info.args().getUnchecked("string"));
			});

		// Make sure the default registration with the minecraft: namespace works
		command.register();

		if (!enableBeforeRegistering) {
			player = enableWithNamespaces();
		}

		// Check contents of Brigadier CommandDispatcher
		RootCommandNode<Object> rootNode = MockPlatform.getInstance().getBrigadierDispatcher().getRoot();
		assertNotNull(rootNode.getChild("test"));
		// Unlike custom namespaces, the minecraft namespace should NOT appear in the Brigadier dispatcher
		//  `minecraft:test` is only created when moved to Bukkit's CommandMap, or if there is a command conflict
		assertNull(rootNode.getChild("minecraft:test"));

		// Check contents of Bukkit CommandMap
		CommandMap commandMap = MockPlatform.getInstance().getSimpleCommandMap();
		assertNotNull(commandMap.getCommand("test"));
		assertNotNull(commandMap.getCommand("minecraft:test"));

		// Commands should run
		assertStoresResult(player, "test alpha", results, "alpha");
		assertStoresResult(player, "minecraft:test alpha", results, "alpha");

		assertNoMoreResults(results);
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	public void testStringNamespace(boolean enableBeforeRegistering) {
		Mut<String> results = Mut.of();

		Player player = null;
		if (enableBeforeRegistering) {
			player = enableWithNamespaces();
		}

		CommandAPICommand command = new CommandAPICommand("test")
			.withArguments(new StringArgument("string"))
			.executesPlayer(info -> {
				results.set(info.args().getUnchecked("string"));
			});

		// Test registering the command with a custom namespace
		command.register("commandtest");

		if (!enableBeforeRegistering) {
			player = enableWithNamespaces();
		}

		// Check contents of Brigadier CommandDispatcher
		RootCommandNode<Object> rootNode = MockPlatform.getInstance().getBrigadierDispatcher().getRoot();
		assertNotNull(rootNode.getChild("test"));
		assertNotNull(rootNode.getChild("commandtest:test"));
		assertNull(rootNode.getChild("minecraft:test"));

		// Check contents of Bukkit CommandMap
		CommandMap commandMap = MockPlatform.getInstance().getSimpleCommandMap();
		assertNotNull(commandMap.getCommand("test"));
		assertNotNull(commandMap.getCommand("commandtest:test"));
		assertNull(commandMap.getCommand("minecraft:test"));

		// Commands should run
		assertStoresResult(player, "test alpha", results, "alpha");
		assertStoresResult(player, "commandtest:test alpha", results, "alpha");

		// Running the command with the minecraft: namespace should fail
		assertCommandFailsWith(
			player,
			"minecraft:test alpha",
			"Unknown or incomplete command, see below for error at position 0: <--[HERE]"
		);

		assertNoMoreResults(results);
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	public void testPluginNamespace(boolean enableBeforeRegistering) {
		Mut<String> results = Mut.of();

		Player player = null;
		if (enableBeforeRegistering) {
			player = enableWithNamespaces();
		}

		CommandAPICommand command = new CommandAPICommand("test")
			.withArguments(new StringArgument("string"))
			.executesPlayer(info -> {
				results.set(info.args().getUnchecked("string"));
			});

		// Test registering the command with a plugin instance
		command.register(MockPlatform.getConfiguration().getPlugin());

		if (!enableBeforeRegistering) {
			player = enableWithNamespaces();
		}

		// Check contents of Brigadier CommandDispatcher
		RootCommandNode<Object> rootNode = MockPlatform.getInstance().getBrigadierDispatcher().getRoot();
		assertNotNull(rootNode.getChild("test"));
		assertNotNull(rootNode.getChild("commandapitest:test"));
		assertNull(rootNode.getChild("minecraft:test"));

		// Check contents of Bukkit CommandMap
		CommandMap commandMap = MockPlatform.getInstance().getSimpleCommandMap();
		assertNotNull(commandMap.getCommand("test"));
		assertNotNull(commandMap.getCommand("commandapitest:test"));
		assertNull(commandMap.getCommand("minecraft:test"));

		// Commands should run
		assertStoresResult(player, "test alpha", results, "alpha");
		assertStoresResult(player, "commandapitest:test alpha", results, "alpha");

		// Running the command with the minecraft: namespace should fail
		assertCommandFailsWith(
			player,
			"minecraft:test alpha",
			"Unknown or incomplete command, see below for error at position 0: <--[HERE]"
		);

		assertNoMoreResults(results);
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	public void testAliasesWithDefaultNamespace(boolean enableBeforeRegistering) {
		Mut<String> results = Mut.of();

		Player player = null;
		if (enableBeforeRegistering) {
			player = enableWithNamespaces();
		}

		CommandAPICommand command = new CommandAPICommand("test")
			.withAliases("alpha", "beta")
			.withArguments(new StringArgument("string"))
			.executesPlayer(info -> {
				results.set(info.args().getUnchecked("string"));
			});

		// Test aliases with the default namespace
		command.register();

		if (!enableBeforeRegistering) {
			player = enableWithNamespaces();
		}

		// Check contents of Brigadier CommandDispatcher
		RootCommandNode<Object> rootNode = MockPlatform.getInstance().getBrigadierDispatcher().getRoot();
		assertNotNull(rootNode.getChild("test"));
		// `minecraft` namespace is only created in the CommandMap since there is no command conflict
		assertNull(rootNode.getChild("minecraft:test"));
		assertNotNull(rootNode.getChild("alpha"));
		assertNull(rootNode.getChild("minecraft:alpha"));
		assertNotNull(rootNode.getChild("beta"));
		assertNull(rootNode.getChild("minecraft:beta"));

		// Check contents of Bukkit CommandMap
		CommandMap commandMap = MockPlatform.getInstance().getSimpleCommandMap();
		assertNotNull(commandMap.getCommand("test"));
		assertNotNull(commandMap.getCommand("minecraft:test"));
		assertNotNull(commandMap.getCommand("alpha"));
		assertNotNull(commandMap.getCommand("minecraft:alpha"));
		assertNotNull(commandMap.getCommand("beta"));
		assertNotNull(commandMap.getCommand("minecraft:beta"));

		// Commands should run
		assertStoresResult(player, "test discord", results, "discord");
		assertStoresResult(player, "alpha discord", results, "discord");
		assertStoresResult(player, "beta discord", results, "discord");
		assertStoresResult(player, "minecraft:test discord", results, "discord");
		assertStoresResult(player, "minecraft:alpha discord", results, "discord");
		assertStoresResult(player, "minecraft:beta discord", results, "discord");

		assertNoMoreResults(results);
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	public void testAliasesWithCustomNamespace(boolean enableBeforeRegistering) {
		Mut<String> results = Mut.of();

		Player player = null;
		if (enableBeforeRegistering) {
			player = enableWithNamespaces();
		}

		CommandAPICommand command = new CommandAPICommand("test")
			.withAliases("alpha", "beta")
			.withArguments(new StringArgument("string"))
			.executesPlayer(info -> {
				results.set(info.args().getUnchecked("string"));
			});

		// Test aliases with a custom namespace
		command.register("commandtest");

		if (!enableBeforeRegistering) {
			player = enableWithNamespaces();
		}

		// Check contents of Brigadier CommandDispatcher
		RootCommandNode<Object> rootNode = MockPlatform.getInstance().getBrigadierDispatcher().getRoot();
		assertNotNull(rootNode.getChild("test"));
		assertNotNull(rootNode.getChild("commandtest:test"));
		assertNull(rootNode.getChild("minecraft:test"));

		assertNotNull(rootNode.getChild("alpha"));
		assertNotNull(rootNode.getChild("commandtest:alpha"));
		assertNull(rootNode.getChild("minecraft:test"));

		assertNotNull(rootNode.getChild("beta"));
		assertNotNull(rootNode.getChild("commandtest:beta"));
		assertNull(rootNode.getChild("minecraft:test"));

		// Check contents of Bukkit CommandMap
		CommandMap commandMap = MockPlatform.getInstance().getSimpleCommandMap();
		assertNotNull(commandMap.getCommand("test"));
		assertNotNull(commandMap.getCommand("commandtest:test"));
		assertNull(commandMap.getCommand("minecraft:test"));

		assertNotNull(commandMap.getCommand("alpha"));
		assertNotNull(commandMap.getCommand("commandtest:alpha"));
		assertNull(commandMap.getCommand("minecraft:alpha"));

		assertNotNull(commandMap.getCommand("beta"));
		assertNotNull(commandMap.getCommand("commandtest:beta"));
		assertNull(commandMap.getCommand("minecraft:beta"));

		// Commands should run
		assertStoresResult(player, "test discord", results, "discord");
		assertStoresResult(player, "alpha discord", results, "discord");
		assertStoresResult(player, "beta discord", results, "discord");
		assertStoresResult(player, "commandtest:test discord", results, "discord");
		assertStoresResult(player, "commandtest:alpha discord", results, "discord");
		assertStoresResult(player, "commandtest:beta discord", results, "discord");

		// Running the command with the minecraft: namespace should fail
		assertCommandFailsWith(
			player,
			"minecraft:test discord",
			"Unknown or incomplete command, see below for error at position 0: <--[HERE]"
		);
		assertCommandFailsWith(
			player,
			"minecraft:alpha discord",
			"Unknown or incomplete command, see below for error at position 0: <--[HERE]"
		);
		assertCommandFailsWith(
			player,
			"minecraft:beta discord",
			"Unknown or incomplete command, see below for error at position 0: <--[HERE]"
		);

		assertNoMoreResults(results);
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	public void testAliasesWithPluginNamespace(boolean enableBeforeRegistering) {
		Mut<String> results = Mut.of();

		Player player = null;
		if (enableBeforeRegistering) {
			player = enableWithNamespaces();
		}

		CommandAPICommand command = new CommandAPICommand("test")
			.withAliases("alpha", "beta")
			.withArguments(new StringArgument("string"))
			.executesPlayer(info -> {
				results.set(info.args().getUnchecked("string"));
			});

		// Test aliases with a custom namespace
		command.register(MockPlatform.getConfiguration().getPlugin());

		if (!enableBeforeRegistering) {
			player = enableWithNamespaces();
		}

		// Check contents of Brigadier CommandDispatcher
		RootCommandNode<Object> rootNode = MockPlatform.getInstance().getBrigadierDispatcher().getRoot();
		assertNotNull(rootNode.getChild("test"));
		assertNotNull(rootNode.getChild("commandapitest:test"));
		assertNull(rootNode.getChild("minecraft:test"));

		assertNotNull(rootNode.getChild("alpha"));
		assertNotNull(rootNode.getChild("commandapitest:alpha"));
		assertNull(rootNode.getChild("minecraft:test"));

		assertNotNull(rootNode.getChild("beta"));
		assertNotNull(rootNode.getChild("commandapitest:beta"));
		assertNull(rootNode.getChild("minecraft:test"));

		// Check contents of Bukkit CommandMap
		CommandMap commandMap = MockPlatform.getInstance().getSimpleCommandMap();
		assertNotNull(commandMap.getCommand("test"));
		assertNotNull(commandMap.getCommand("commandapitest:test"));
		assertNull(commandMap.getCommand("minecraft:test"));

		assertNotNull(commandMap.getCommand("alpha"));
		assertNotNull(commandMap.getCommand("commandapitest:alpha"));
		assertNull(commandMap.getCommand("minecraft:test"));

		assertNotNull(commandMap.getCommand("beta"));
		assertNotNull(commandMap.getCommand("commandapitest:beta"));
		assertNull(commandMap.getCommand("minecraft:test"));

		// Commands should run
		assertStoresResult(player, "test discord", results, "discord");
		assertStoresResult(player, "alpha discord", results, "discord");
		assertStoresResult(player, "beta discord", results, "discord");
		assertStoresResult(player, "commandapitest:test discord", results, "discord");
		assertStoresResult(player, "commandapitest:alpha discord", results, "discord");
		assertStoresResult(player, "commandapitest:beta discord", results, "discord");

		// Running the command with the minecraft: namespace should fail
		assertCommandFailsWith(
			player,
			"minecraft:test discord",
			"Unknown or incomplete command, see below for error at position 0: <--[HERE]"
		);
		assertCommandFailsWith(
			player,
			"minecraft:alpha discord",
			"Unknown or incomplete command, see below for error at position 0: <--[HERE]"
		);
		assertCommandFailsWith(
			player,
			"minecraft:beta discord",
			"Unknown or incomplete command, see below for error at position 0: <--[HERE]"
		);

		assertNoMoreResults(results);
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	public void testCommandNameConflictButDifferentNamespace(boolean enableBeforeRegistering) {
		Mut<String> results = Mut.of();

		Player tempPlayer = null;
		if (enableBeforeRegistering) {
			tempPlayer = enableWithNamespaces();
		}

		CommandAPICommand a = new CommandAPICommand("test")
			.withArguments(LiteralArgument.of("a"))
			.executesPlayer(info -> {
				results.set("a");
			});

		CommandAPICommand b = new CommandAPICommand("test")
			.withArguments(LiteralArgument.of("b"))
			.executesPlayer(info -> {
				results.set("b");
			});

		a.register("a");
		b.register("b");

		if (!enableBeforeRegistering) {
			tempPlayer = enableWithNamespaces();
		}
		final Player player = tempPlayer; // Since we use `player` in a lambda it needs to be final

		// The two branches should have merged in the un-namespaced version
		assertStoresResult(player, "test a", results, "a");
		assertStoresResult(player, "test b", results, "b");

		// The branches should be separated in the namespaced versions
		assertStoresResult(player, "a:test a", results, "a");
		// When `server.dispatchThrowableCommand` (which is used by assertCommandFailsWith) is used here,
		//  Bukkit catches the exception and sends the exception message to the sender. Instead, we execute the
		//  command directly with Brigadier rather than via `VanillaCommandWrapper` to inspect the failure message.
		assertThrowsWithMessage(
			CommandSyntaxException.class,
			"Incorrect argument for command at position 7: a:test <--[HERE]",
			() -> server.dispatchThrowableBrigadierCommand(player, "a:test b")
		);

		assertThrowsWithMessage(
			CommandSyntaxException.class,
			"Incorrect argument for command at position 7: b:test <--[HERE]",
			() -> server.dispatchThrowableBrigadierCommand(player, "b:test a")
		);
		assertStoresResult(player, "b:test b", results, "b");

		assertNoMoreResults(results);
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	public void testSameCommandNameConflictWithDefaultNamespaceAndCustomNamespace(boolean enableBeforeRegistering) {
		Mut<String> results = Mut.of();

		Player tempPlayer = null;
		if (enableBeforeRegistering) {
			tempPlayer = enableWithNamespaces();
		}

		CommandAPICommand defaultNamespace = new CommandAPICommand("test")
			.withArguments(LiteralArgument.of("a"))
			.executesPlayer(info -> {
				results.set("a");
			});

		CommandAPICommand customNamespace = new CommandAPICommand("test")
			.withArguments(LiteralArgument.of("b"))
			.executesPlayer(info -> {
				results.set("b");
			});

		defaultNamespace.register();
		customNamespace.register("custom");

		if (!enableBeforeRegistering) {
			tempPlayer = enableWithNamespaces();
		}
		final Player player = tempPlayer;

		RootCommandNode<Object> root = MockPlatform.getInstance().getBrigadierDispatcher().getRoot();
		assertNotNull(root.getChild("test"));
		assertNotNull(root.getChild("custom:test"));
		// The `minecraft:test` node should exist in the Brigadier map so the `minecraft:test` VanillaCommandWrapper
		//  can properly execute the command separately from the `custom:test` b branch
		assertNotNull(root.getChild("minecraft:test"));

		CommandMap commandMap = MockPlatform.getInstance().getSimpleCommandMap();
		assertNotNull(commandMap.getCommand("test"));
		assertNotNull(commandMap.getCommand("minecraft:test"));
		assertNotNull(commandMap.getCommand("custom:test"));
		assertNull(commandMap.getCommand("minecraft:custom:test"));

		assertStoresResult(player, "test a", results, "a");
		assertStoresResult(player, "test b", results, "b");
		assertStoresResult(player, "minecraft:test a", results, "a");
		assertStoresResult(player, "custom:test b", results, "b");

		assertThrowsWithMessage(
			CommandSyntaxException.class,
			"Incorrect argument for command at position 12: ...stom:test <--[HERE]",
			() -> server.dispatchThrowableBrigadierCommand(player, "custom:test a")
		);
		assertThrowsWithMessage(
			CommandSyntaxException.class,
			"Incorrect argument for command at position 15: ...raft:test <--[HERE]",
			() -> server.dispatchThrowableBrigadierCommand(player, "minecraft:test b")
		);

		assertNoMoreResults(results);
	}

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	public void testCommandTreeRegistrationDefaultNamespace(boolean enableBeforeRegistering) {
		Mut<String> results = Mut.of();

		Player player = null;
		if (enableBeforeRegistering) {
			player = enableWithNamespaces();
		}

		CommandTree command = new CommandTree("test")
			.then(new LiteralArgument("a")
				.then(new StringArgument("string")
					.executesPlayer(info -> {
						results.set("a");
						results.set(info.args().getUnchecked("string"));
					})
				)
			)
			.then(new LiteralArgument("b")
				.then(new IntegerArgument("integer")
					.executesPlayer(info -> {
						results.set("b");
						results.set(String.valueOf(info.args().get("integer")));
					})
				)
			);

		// Make sure the default registration with the minecraft: namespace works
		command.register();

		if (!enableBeforeRegistering) {
			player = enableWithNamespaces();
		}

		server.dispatchCommand(player, "test a alpha");
		assertEquals("a", results.get());
		assertEquals("alpha", results.get());

		server.dispatchCommand(player, "minecraft:test a alpha");
		assertEquals("a", results.get());
		assertEquals("alpha", results.get());

		server.dispatchCommand(player, "test b 123");
		assertEquals("b", results.get());
		assertEquals("123", results.get());

		server.dispatchCommand(player, "minecraft:test b 123");
		assertEquals("b", results.get());
		assertEquals("123", results.get());

		assertNoMoreResults(results);
	}

	@ParameterizedTest
	@ValueSource(booleans = {false,true})
	public void testCommandTreeRegistrationCustomNamespace(boolean enableBeforeRegistering) {
		Mut<String> results = Mut.of();

		Player player = null;
		if (enableBeforeRegistering) {
			player = enableWithNamespaces();
		}

		CommandTree command = new CommandTree("test")
			.then(new LiteralArgument("a")
				.then(new StringArgument("string")
					.executesPlayer(info -> {
						results.set("a");
						results.set(info.args().getUnchecked("string"));
					})
				)
			)
			.then(new LiteralArgument("b")
				.then(new IntegerArgument("integer")
					.executesPlayer(info -> {
						results.set("b");
						results.set(String.valueOf(info.args().get("integer")));
					})
				)
			);

		command.register("namespace");

		if (!enableBeforeRegistering) {
			player = enableWithNamespaces();
		}

		server.dispatchCommand(player, "test a alpha");
		assertEquals("a", results.get());
		assertEquals("alpha", results.get());

		server.dispatchCommand(player, "namespace:test a alpha");
		assertEquals("a", results.get());
		assertEquals("alpha", results.get());

		server.dispatchCommand(player, "test b 123");
		assertEquals("b", results.get());
		assertEquals("123", results.get());

		server.dispatchCommand(player, "namespace:test b 123");
		assertEquals("b", results.get());
		assertEquals("123", results.get());

		// Running the command with the minecraft: namespace should fail
		assertCommandFailsWith(
			player,
			"minecraft:test a alpha",
			"Unknown or incomplete command, see below for error at position 0: <--[HERE]"
		);
		assertCommandFailsWith(
			player,
			"minecraft:test b 123",
			"Unknown or incomplete command, see below for error at position 0: <--[HERE]"
		);

		assertNoMoreResults(results);
	}

	@ParameterizedTest
	@ValueSource(booleans = {false,true})
	public void testCommandTreeRegistrationPluginNamespace(boolean enableBeforeRegistering) {
		Mut<String> results = Mut.of();

		Player player = null;
		if (enableBeforeRegistering) {
			player = enableWithNamespaces();
		}

		CommandTree command = new CommandTree("test")
			.then(new LiteralArgument("a")
				.then(new StringArgument("string")
					.executesPlayer(info -> {
						results.set("a");
						results.set(info.args().getUnchecked("string"));
					})
				)
			)
			.then(new LiteralArgument("b")
				.then(new IntegerArgument("integer")
					.executesPlayer(info -> {
						results.set("b");
						results.set(String.valueOf(info.args().get("integer")));
					})
				)
			);

		command.register(MockPlatform.getConfiguration().getPlugin());

		if (!enableBeforeRegistering) {
			player = enableWithNamespaces();
		}

		server.dispatchCommand(player, "test a alpha");
		assertEquals("a", results.get());
		assertEquals("alpha", results.get());

		server.dispatchCommand(player, "commandapitest:test a alpha");
		assertEquals("a", results.get());
		assertEquals("alpha", results.get());

		server.dispatchCommand(player, "test b 123");
		assertEquals("b", results.get());
		assertEquals("123", results.get());

		server.dispatchCommand(player, "commandapitest:test b 123");
		assertEquals("b", results.get());
		assertEquals("123", results.get());

		// Running the command with the minecraft: namespace should fail
		assertCommandFailsWith(
			player,
			"minecraft:test a alpha",
			"Unknown or incomplete command, see below for error at position 0: <--[HERE]"
		);
		assertCommandFailsWith(
			player,
			"minecraft:test b 123",
			"Unknown or incomplete command, see below for error at position 0: <--[HERE]"
		);

		assertNoMoreResults(results);
	}

	void assertPermissionCheckFails(CommandSender sender, String commandLine) {
		// When the player executes a Brigadier command, Brigadier fully handles the permission check and throws an
		// exception. However, when a Brigadier command is executed server side (for example with `Bukkit#dispatchCommand`),
		// VanillaCommandWrapper handles the permission check and sends a message to the sender. We want to make sure the
		// permission works in both cases.
		assertThrowsWithMessage(
			CommandSyntaxException.class,
			"Unknown or incomplete command, see below for error at position 0: <--[HERE]",
			() -> server.dispatchThrowableBrigadierCommand(sender, commandLine)
		);

		Mockito.clearInvocations(sender);
		server.dispatchCommand(sender, commandLine);
		Mockito.verify(sender).sendMessage(ChatColor.RED + "I'm sorry, but you do not have permission to perform this " +
			"command. Please contact the server administrators if you believe that this is a mistake.");
	}

	@ParameterizedTest
	@ValueSource(booleans = {false,true})
	public void testPermissionsWithDefaultNamespace(boolean enableBeforeRegistering) {
		Mut<String> commandRan = Mut.of();

		Player player = null;
		if (enableBeforeRegistering) {
			player = enableWithNamespaces();
		}

		CommandAPICommand command = new CommandAPICommand("test")
			.withPermission("permission")
			.executesPlayer(info -> {
				commandRan.set("ran");
			});

		// Test with default minecraft: namespace
		command.register();

		if (!enableBeforeRegistering) {
			player = enableWithNamespaces();
		}

		assertPermissionCheckFails(player, "test");
		assertPermissionCheckFails(player, "minecraft:test");

		player.addAttachment(super.plugin, "permission", true);

		assertStoresResult(player, "test", commandRan, "ran");
		assertStoresResult(player, "minecraft:test", commandRan, "ran");

		assertNoMoreResults(commandRan);
	}

	@ParameterizedTest
	@ValueSource(booleans = {false,true})
	public void testPermissionsWithCustomNamespace(boolean enableBeforeRegistering) {
		Mut<String> commandRan = Mut.of();

		Player player = null;
		if (enableBeforeRegistering) {
			player = enableWithNamespaces();
		}

		CommandAPICommand command = new CommandAPICommand("test")
			.withPermission("permission")
			.executesPlayer(info -> {
				commandRan.set("ran");
			});

		// Test with custom namespace (same as with a plugins)
		command.register("custom");

		if (!enableBeforeRegistering) {
			player = enableWithNamespaces();
		}

		assertPermissionCheckFails(player, "test");
		assertPermissionCheckFails(player, "custom:test");

		player.addAttachment(super.plugin, "permission", true);

		assertStoresResult(player, "test", commandRan, "ran");
		assertStoresResult(player, "custom:test", commandRan, "ran");

		assertNoMoreResults(commandRan);
	}

	@ParameterizedTest
	@ValueSource(booleans = {false,true})
	public void testPermissionsWithCommandNameConflictButDifferentNamespace(boolean enableBeforeRegistering) {
		Mut<String> commandRan = Mut.of();

		Player tempPlayer = null;
		if (enableBeforeRegistering) {
			tempPlayer = enableWithNamespaces();
		}

		CommandAPICommand first = new CommandAPICommand("test")
			.withArguments(new LiteralArgument("first").withPermission("first"))
			.withPermission("first")
			.executesPlayer(info -> {
				commandRan.set("first");
			});

		CommandAPICommand second = new CommandAPICommand("test")
			.withArguments(new LiteralArgument("second").withPermission("second"))
			.withPermission("second")
			.executesPlayer(info -> {
				commandRan.set("second");
			});

		first.register("first");
		second.register("second");

		if (!enableBeforeRegistering) {
			tempPlayer = enableWithNamespaces();
		}
		final Player player = tempPlayer;

		PermissionAttachment permissions = player.addAttachment(super.plugin);

		// At least one permission is required
		permissions.setPermission("first", false);
		permissions.setPermission("second", false);
		player.recalculatePermissions();

		assertPermissionCheckFails(player, "test first");
		assertPermissionCheckFails(player, "test second");
		assertPermissionCheckFails(player, "first:test first");
		assertPermissionCheckFails(player, "second:test second");

		// First permission also applies to the unnamespaced command literal, but not the 'second' literal
		permissions.setPermission("first", true);
		permissions.setPermission("second", false);
		player.recalculatePermissions();

		assertStoresResult(player, "test first", commandRan, "first");
		assertThrowsWithMessage(
			CommandSyntaxException.class,
			"Incorrect argument for command at position 5: test <--[HERE]",
			() -> server.dispatchThrowableBrigadierCommand(player, "test second")
		);
		assertStoresResult(player, "first:test first", commandRan, "first");
		assertPermissionCheckFails(player, "second:test second");

		// Second permission only applies to `second:test`
		permissions.setPermission("first", false);
		permissions.setPermission("second", true);
		player.recalculatePermissions();

		assertPermissionCheckFails(player, "test first");
		assertPermissionCheckFails(player, "test second");
		assertPermissionCheckFails(player, "first:test first");
		assertStoresResult(player, "second:test second", commandRan, "second");

		// All permissions allows all commands
		permissions.setPermission("first", true);
		permissions.setPermission("second", true);
		player.recalculatePermissions();

		assertStoresResult(player, "test first", commandRan, "first");
		assertStoresResult(player, "test second", commandRan, "second");
		assertStoresResult(player, "first:test first", commandRan, "first");
		assertStoresResult(player, "second:test second", commandRan, "second");


		assertNoMoreResults(commandRan);
	}

	@Test
	public void testConfigNamespace() {
		CommandAPIBukkitConfig config = new CommandAPIBukkitConfig(MockPlatform.getConfiguration().getPlugin());
		InternalBukkitConfig internalConfig = new InternalBukkitConfig(config);

		// The namespace wasn't changed so it should default to minecraft
		assertEquals("minecraft", internalConfig.getNamespace());

		config = new CommandAPIBukkitConfig(MockPlatform.getConfiguration().getPlugin());
		CommandAPIBukkitConfig finalConfig = config;

		// The namespace is set to null, this should throw a NPE
		assertThrows(NullPointerException.class, () -> finalConfig.setNamespace(null));

		config = new CommandAPIBukkitConfig(MockPlatform.getConfiguration().getPlugin())
			.setNamespace("");
		internalConfig = new InternalBukkitConfig(config);

		// The namespace was set to an empty namespace so this should result in the default minecraft namespace
		assertEquals("minecraft", internalConfig.getNamespace());

		config = new CommandAPIBukkitConfig(MockPlatform.getConfiguration().getPlugin())
			.setNamespace("custom");
		internalConfig = new InternalBukkitConfig(config);

		// The namespace was set to a non-empty, non-null custom namespace, this should be valid
		assertEquals("custom", internalConfig.getNamespace());

		config = new CommandAPIBukkitConfig(MockPlatform.getConfiguration().getPlugin())
			.usePluginNamespace();
		internalConfig = new InternalBukkitConfig(config);

		// The namespace was set to use the plugin name as the namespace, this should be valid
		assertEquals("commandapitest", internalConfig.getNamespace());

		config = new CommandAPIBukkitConfig(MockPlatform.getConfiguration().getPlugin())
			.setNamespace("custom")
			.usePluginNamespace();
		internalConfig = new InternalBukkitConfig(config);

		// The namespace was first set to a custom one, then was set to use the plugin name. This should be valid and the plugin name should be the namespace
		assertEquals("commandapitest", internalConfig.getNamespace());

		config = new CommandAPIBukkitConfig(MockPlatform.getConfiguration().getPlugin())
			.setNamespace("custom")
			.usePluginNamespace()
			.setNamespace("custom");
		internalConfig = new InternalBukkitConfig(config);

		// The namespace was first set to a custom one, then was set to use the plugin name, then was set to use a custom one
		// usePluginNamespace() should take priority and the plugin name should be the namespace
		assertEquals("commandapitest", internalConfig.getNamespace());

		config = new CommandAPIBukkitConfig(MockPlatform.getConfiguration().getPlugin())
			.setNamespace("Custom");
		internalConfig = new InternalBukkitConfig(config);

		// The namespace uses invalid characters so the namespace should default to minecraft
		assertEquals("minecraft", internalConfig.getNamespace());

		Player player = enableWithNamespaces();

		// Here, it doesn't really matter when we're registering the command, we only care for namespaces
		CommandAPICommand command = new CommandAPICommand("test")
			.executesPlayer(P_EXEC);

		command.register("");

		// The command should be registered with the minecraft namespace because the namespace was empty
		assertTrue(server.dispatchCommand(player, "test"));
		assertTrue(server.dispatchCommand(player, "minecraft:test"));

		CommandAPI.unregister("test", true);

		command.register("Command");

		// The command should be registered with the minecraft namespace because the namespace was invalid
		assertTrue(server.dispatchCommand(player, "test"));
		assertTrue(server.dispatchCommand(player, "minecraft:test"));

		CommandAPI.unregister("test", true);

		command.register("test_123.-");

		// The command should be registered with the custom provided namespace
		assertTrue(server.dispatchCommand(player, "test"));
		assertTrue(server.dispatchCommand(player, "test_123.-:test"));
	}

}
