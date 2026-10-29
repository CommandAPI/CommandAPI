package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CommandArgument;
import dev.jorel.commandapi.arguments.SuggestionsBranch;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import dev.jorel.commandapi.wrappers.CommandResult;

/**
 * Tests for the {@link CommandArgument}
 */
class ArgumentCommandTests extends TestBase {

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
	void executionTestWithCommandArgumentBukkitCommand() {
		Mut<CommandResult> results = Mut.of();

		// Check command retrieval from CommandMap
		new CommandAPICommand("commandargument")
			.withArguments(new CommandArgument("command"))
			.executesPlayer((sender, args) -> {
				results.set((CommandResult) args.get(0));
			}).register();

		CommandMap commandMap = CommandAPIBukkit.get().getNMS().getSimpleCommandMap();

		PlayerMock player = server.addPlayer("APlayer");
		
		// /version is a Bukkit command which is always present in the Bukkit
		// command map.
		
		// commandargument version
		server.dispatchCommand(player, "commandargument version");
		assertNotNull(commandMap.getCommand("version"));
		assertEquals(new CommandResult(commandMap.getCommand("version"), new String[0]), results.get());

		assertNoMoreResults(results);
	}
	
	@Test
	void executionTestWithCommandArgumentRestrictedCommandReplaceSuggestions() {
		Mut<CommandResult> results = Mut.of();

		// Check replaceSuggestions
		new CommandAPICommand("restrictedcommand")
			.withArguments(new CommandArgument("command")
				.replaceSuggestions(
					ArgumentSuggestions.strings("give"),
					ArgumentSuggestions.strings(info -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new)),
					ArgumentSuggestions.strings("diamond", "minecraft:diamond"),
					ArgumentSuggestions.empty()
				)
			).executesPlayer((sender, args) -> {
				results.set((CommandResult) args.get(0));
			}).register();

		CommandMap commandMap = CommandAPIBukkit.get().getNMS().getSimpleCommandMap();
		registerDummyCommands(commandMap, "give", "data", "tp");

		PlayerMock player = server.addPlayer("APlayer");
		server.addPlayer("BPlayer");
		
		// /restrictedcommand give APlayer diamond
		server.dispatchCommand(player, "restrictedcommand give APlayer diamond");
		assertEquals(new CommandResult(commandMap.getCommand("give"), new String[]{ "APlayer", "diamond" }), results.get());
		
		// /restrictedcommand give BPlayer diamond
		server.dispatchCommand(player, "restrictedcommand give BPlayer diamond");
		assertEquals(new CommandResult(commandMap.getCommand("give"), new String[]{ "BPlayer", "diamond" }), results.get());
		
		// /restrictedcommand give APlayer minecraft:diamond
		server.dispatchCommand(player, "restrictedcommand give APlayer minecraft:diamond");
		assertEquals(new CommandResult(commandMap.getCommand("give"), new String[]{ "APlayer", "minecraft:diamond" }), results.get());

		// /restrictedcommand data APlayer diamond
		// Illegal command, "data" is not in the list of restricted commands
		assertCommandFailsWith(player, "restrictedcommand data APlayer diamond", "Unknown or incomplete command. See below for error at position 0: <--[HERE]");
		
		// /restrictedcommand notacommand APlayer diamond
		// Unknown command "notacommand"
		assertCommandFailsWith(player, "restrictedcommand notacommand APlayer diamond", "Unknown or incomplete command. See below for error at position 0: <--[HERE]");
		
		// /restrictedcommand give CPlayer diamond
		// Incorrect argument "CPlayer" is not a valid player
		assertCommandFailsWith(player, "restrictedcommand give CPlayer diamond", "Incorrect argument for command at position 5: give <--[HERE]");
		
		// /restrictedcommand give APlayer dirt
		// Incorrect argument "dirt", only "diamond" or "minecraft:diamond" is allowed
		assertCommandFailsWith(player, "restrictedcommand give APlayer dirt", "Incorrect argument for command at position 13: ...e APlayer <--[HERE]");
		
		// /restrictedcommand give APlayer diamond 64
		// Unexpected additional argument, nothing is allowed to come after "diamond" or "minecraft:diamond"
		assertCommandFailsWith(player, "restrictedcommand give APlayer diamond 64", "Incorrect argument for command at position 21: ...r diamond <--[HERE]");
		
		// /restrictedcommand give APlayer
		// Incomplete command, expected "diamond" or "minecraft:diamond"
		assertCommandFailsWith(player, "restrictedcommand give APlayer", "Expected more arguments at position 13: ...ve APlayer<--[HERE]");

		assertNoMoreResults(results);
	}
	
	@Test
	void executionTestWithCommandArgumentMultipleCommandBranchingSuggestions() {
		Mut<CommandResult> results = Mut.of();

		// Check branching suggestions
		new CommandAPICommand("multiplecommands")
			.withArguments(
				new CommandArgument("command")
					.branchSuggestions(
						SuggestionsBranch.<CommandSender>suggest(
							ArgumentSuggestions.strings("give"),
							ArgumentSuggestions.strings(info -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new))
						).branch(
							SuggestionsBranch.suggest(
								ArgumentSuggestions.strings("diamond", "minecraft:diamond"),
								ArgumentSuggestions.empty()
							),
							SuggestionsBranch.suggest(
								ArgumentSuggestions.strings("dirt", "minecraft:dirt"),
								null,
								ArgumentSuggestions.empty()
							)
						),
						SuggestionsBranch.suggest(
							ArgumentSuggestions.strings("tp"),
							ArgumentSuggestions.strings(info -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new)),
							ArgumentSuggestions.strings(info -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new))
						)
					)
			).executes((sender, args) -> {
				results.set((CommandResult) args.get(0));
			}).register();

		CommandMap commandMap = CommandAPIBukkit.get().getNMS().getSimpleCommandMap();
		registerDummyCommands(commandMap, "give", "data", "tp");

		PlayerMock player = server.addPlayer("APlayer");
		server.addPlayer("BPlayer");

		// /multiplecommands give APlayer diamond
		server.dispatchCommand(player, "multiplecommands give APlayer diamond");
		assertEquals(new CommandResult(commandMap.getCommand("give"), new String[]{"APlayer", "diamond"}), results.get());

		// /multiplecommands give BPlayer diamond
		server.dispatchCommand(player, "multiplecommands give BPlayer diamond");
		assertEquals(new CommandResult(commandMap.getCommand("give"), new String[]{"BPlayer", "diamond"}), results.get());

		// /multiplecommands give APlayer minecraft:diamond
		server.dispatchCommand(player, "multiplecommands give APlayer minecraft:diamond");
		assertEquals(new CommandResult(commandMap.getCommand("give"), new String[]{"APlayer", "minecraft:diamond"}), results.get());

		// /multiplecommands give APlayer dirt
		server.dispatchCommand(player, "multiplecommands give APlayer dirt");
		assertEquals(new CommandResult(commandMap.getCommand("give"), new String[]{"APlayer", "dirt"}), results.get());

		// /multiplecommands give APlayer dirt 64
		server.dispatchCommand(player, "multiplecommands give APlayer dirt 64");
		assertEquals(new CommandResult(commandMap.getCommand("give"), new String[]{"APlayer", "dirt", "64"}), results.get());

		// /multiplecommands tp APlayer BPlayer
		server.dispatchCommand(player, "multiplecommands tp APlayer BPlayer");
		assertEquals(new CommandResult(commandMap.getCommand("tp"), new String[]{"APlayer", "BPlayer"}), results.get());

		// /multiplecommands tp BPlayer APlayer
		server.dispatchCommand(player, "multiplecommands tp BPlayer APlayer");
		assertEquals(new CommandResult(commandMap.getCommand("tp"), new String[]{"BPlayer", "APlayer"}), results.get());

		// /multiplecommands data get entity APlayer
		// Illegal command, "data" is not in the list of restricted commands
		assertCommandFailsWith(player, "multiplecommands data get entity APlayer", "Unknown or incomplete command. See below for error at position 0: <--[HERE]");

		// /multiplecommands notacommand APlayer diamond
		// Unknown command "notacommand"
		assertCommandFailsWith(player, "multiplecommands notacommand APlayer diamond", "Unknown or incomplete command. See below for error at position 0: <--[HERE]");

		// /multiplecommands give CPlayer diamond
		// Incorrect argument "CPlayer" is not a valid player
		assertCommandFailsWith(player, "multiplecommands give CPlayer diamond", "Incorrect argument for command at position 5: give <--[HERE]");

		// /multiplecommands give APlayer stone
		// Unexpected argument "stone", expected "diamond", "minecraft:diamond", "dirt" or "minecraft:dirt"
		assertCommandFailsWith(player, "multiplecommands give APlayer stone", "Incorrect argument for command at position 13: ...e APlayer <--[HERE]");

		// /multiplecommands give APlayer diamond
		// Unexpected additional argument, nothing is allowed to come after "diamond" or "minecraft:diamond"
		assertCommandFailsWith(player, "multiplecommands give APlayer diamond 64", "Incorrect argument for command at position 21: ...r diamond <--[HERE]");

		// /multiplecommands give APlayer"
		// Incomplete command, expected "diamond", "minecraft:diamond", "dirt" or "minecraft:dirt"
		assertCommandFailsWith(player, "multiplecommands give APlayer", "Expected more arguments at position 13: ...ve APlayer<--[HERE]");

		// TODO: We don't have any tests for "dirt" and "null" that comes after it
		
		// /multiplecommands tp APlayer CPlayer
		// Incorrect argument "CPlayer" is not a valid player
		assertCommandFailsWith(player, "multiplecommands tp APlayer CPlayer", "Incorrect argument for command at position 11: ...p APlayer <--[HERE]");

		// /multiplecommands tp CPlayer APlayer
		// Incorrect argument "CPlayer" is not a valid player
		assertCommandFailsWith(player, "multiplecommands tp CPlayer APlayer", "Incorrect argument for command at position 3: tp <--[HERE]");

		// /multiplecommands tp APlayer"
		// Incomplete command, expected a target player to teleport to
		assertCommandFailsWith(player, "multiplecommands tp APlayer", "Expected more arguments at position 11: tp APlayer<--[HERE]");

		assertNoMoreResults(results);
	}

}
