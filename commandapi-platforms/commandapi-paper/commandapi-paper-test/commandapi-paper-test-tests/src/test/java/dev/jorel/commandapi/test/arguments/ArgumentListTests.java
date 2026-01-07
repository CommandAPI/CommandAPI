package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ListArgument;
import dev.jorel.commandapi.arguments.ListArgumentBuilder;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@link ListArgument}
 */
@SuppressWarnings("unchecked")
class ArgumentListTests extends TestBase {

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
	void executionTestWithListArgument() {
		Mut<List<String>> results = Mut.of();

		new CommandAPICommand("list")
			.withArguments(new ListArgumentBuilder<>("values", ", ")
				.withList(() -> List.of("cat", "wolf", "axolotl"))
				.withStringMapper()
				.buildGreedy())
			.executesPlayer((player, args) -> {
				results.set((List<String>) args.get(0));
			})
			.register();

		Player player = addPlayer("APlayer");

		// /list cat, wolf, axolotl
		assertStoresResult(player, "list cat, wolf, axolotl", results, List.of("cat", "wolf", "axolotl"));

		// /list cat, wolf, axolotl, wolf
		// No duplicates allowed
		assertCommandFailsWith(player, "list cat, wolf, axolotl, wolf", "Duplicate arguments are not allowed at position 20: ... axolotl, <--[HERE]");

		// /list axolotl, wolf, chicken, cat
		// Can't have unspecified "chicken" in the list
		assertCommandFailsWith(player, "list axolotl, wolf, chicken, cat", "Item is not allowed in list at position 15: ...tl, wolf, <--[HERE]");

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithListArgumentWithDuplicates() {
		Mut<List<String>> results = Mut.of();

		new CommandAPICommand("list")
			.withArguments(new ListArgumentBuilder<>("values", ", ")
				.allowDuplicates(true)
				.withList(() -> List.of("cat", "wolf", "axolotl"))
				.withStringMapper()
				.buildGreedy())
			.executesPlayer((player, args) -> {
				results.set((List<String>) args.get(0));
			})
			.register();

		Player player = addPlayer("APlayer");

		// /list cat, wolf, axolotl
		assertStoresResult(player, "list cat, wolf, axolotl", results, List.of("cat", "wolf", "axolotl"));

		// /list cat, wolf, axolotl, cat, wolf
		assertStoresResult(player, "list cat, wolf, axolotl, cat, wolf", results, List.of("cat", "wolf", "axolotl", "cat", "wolf"));

		// /list cat, wolf, axolotl, chicken, cat
		// Can't have unspecified "chicken" in the list
		assertCommandFailsWith(player, "list cat, wolf, axolotl, chicken, cat", "Item is not allowed in list at position 20: ... axolotl, <--[HERE]");

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithListArgumentWithConstantList() {
		Mut<List<String>> results = Mut.of();

		new CommandAPICommand("list")
			.withArguments(new ListArgumentBuilder<>("values", ", ")
				.withList(List.of("cat", "wolf", "axolotl"))
				.withStringMapper()
				.buildGreedy())
			.executesPlayer((player, args) -> {
				results.set((List<String>) args.get(0));
			})
			.register();

		Player player = addPlayer("APlayer");

		// /list cat, wolf, axolotl
		assertStoresResult(player, "list cat, wolf, axolotl", results, List.of("cat", "wolf", "axolotl"));

		// /list cat, wolf, axolotl, wolf
		// No duplicates allowed
		assertCommandFailsWith(player, "list cat, wolf, axolotl, wolf", "Duplicate arguments are not allowed at position 20: ... axolotl, <--[HERE]");

		// /list axolotl, wolf, chicken, cat
		// Can't have unspecified "chicken" in the list
		assertCommandFailsWith(player, "list axolotl, wolf, chicken, cat", "Item is not allowed in list at position 15: ...tl, wolf, <--[HERE]");

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithListArgumentWithConstantListArray() {
		Mut<List<String>> results = Mut.of();

		new CommandAPICommand("list")
			.withArguments(new ListArgumentBuilder<>("values", ", ")
				.withList("cat", "wolf", "axolotl")
				.withStringMapper()
				.buildGreedy())
			.executesPlayer((player, args) -> {
				results.set((List<String>) args.get(0));
			})
			.register();

		Player player = addPlayer("APlayer");

		// /list cat, wolf, axolotl
		assertStoresResult(player, "list cat, wolf, axolotl", results, List.of("cat", "wolf", "axolotl"));

		// /list cat, wolf, axolotl, wolf
		// No duplicates allowed
		assertCommandFailsWith(player, "list cat, wolf, axolotl, wolf", "Duplicate arguments are not allowed at position 20: ... axolotl, <--[HERE]");

		// /list axolotl, wolf, chicken, cat
		// Can't have unspecified "chicken" in the list
		assertCommandFailsWith(player, "list axolotl, wolf, chicken, cat", "Item is not allowed in list at position 15: ...tl, wolf, <--[HERE]");

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithListArgumentWithFunctionList() {
		Mut<List<String>> results = Mut.of();

		new CommandAPICommand("list")
			.withArguments(new ListArgumentBuilder<>("values", ", ")
				.withList(info -> List.of("cat", "wolf", "axolotl", info.sender().getName()))
				.withStringMapper()
				.buildGreedy())
			.executesPlayer((player, args) -> {
				results.set((List<String>) args.get(0));
			})
			.register();

		Player player = addPlayer("APlayer");

		// /list cat, wolf, axolotl
		assertStoresResult(player, "list cat, wolf, axolotl", results, List.of("cat", "wolf", "axolotl"));

		// /list axolotl, wolf, APlayer
		assertStoresResult(player, "list axolotl, wolf, APlayer", results, List.of("axolotl", "wolf", "APlayer"));

		// /list cat, wolf, axolotl, wolf
		// No duplicates allowed
		assertCommandFailsWith(player, "list cat, wolf, axolotl, wolf", "Duplicate arguments are not allowed at position 20: ... axolotl, <--[HERE]");

		// /list axolotl, wolf, chicken, cat
		// Can't have unspecified "chicken" in the list
		assertCommandFailsWith(player, "list axolotl, wolf, chicken, cat", "Item is not allowed in list at position 15: ...tl, wolf, <--[HERE]");

		assertNoMoreResults(results);
	}

	/***************************************
	 * Test ListArgument with .buildText() *
	 ***************************************/

	@Test
	void executionTestWithListTextArgument() {
		Mut<List<String>> results = Mut.of();

		new CommandAPICommand("list")
			.withArguments(new ListArgumentBuilder<>("values", ", ")
				.withList(() -> List.of("cat", "wolf", "axolotl"))
				.withStringMapper()
				.buildText())
			.executesPlayer((player, args) -> {
				results.set((List<String>) args.get(0));
			})
			.register();

		Player player = addPlayer("APlayer");

		// /list "cat, wolf, axolotl"
		assertStoresResult(player, "list \"cat, wolf, axolotl\"", results, List.of("cat", "wolf", "axolotl"));

		// /list "cat, wolf, axolotl, wolf"
		// No duplicates allowed
		assertCommandFailsWith(player, "list \"cat, wolf, axolotl, wolf\"", "Duplicate arguments are not allowed at position 20: ... axolotl, <--[HERE]");

		// /list "axolotl, wolf, chicken, cat"
		// Can't have unspecified "chicken" in the list
		assertCommandFailsWith(player, "list \"axolotl, wolf, chicken, cat\"", "Item is not allowed in list at position 15: ...tl, wolf, <--[HERE]");

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithListTextArgumentWithDuplicates() {
		Mut<List<String>> results = Mut.of();

		new CommandAPICommand("list")
			.withArguments(new ListArgumentBuilder<>("values", ", ")
				.allowDuplicates(true)
				.withList(() -> List.of("cat", "wolf", "axolotl"))
				.withStringMapper()
				.buildText())
			.executesPlayer((player, args) -> {
				results.set((List<String>) args.get(0));
			})
			.register();

		Player player = addPlayer("APlayer");

		// /list "cat, wolf, axolotl"
		assertStoresResult(player, "list \"cat, wolf, axolotl\"", results, List.of("cat", "wolf", "axolotl"));

		// /list "cat, wolf, axolotl, cat, wolf"
		assertStoresResult(player, "list \"cat, wolf, axolotl, cat, wolf\"", results, List.of("cat", "wolf", "axolotl", "cat", "wolf"));

		// /list "cat, wolf, axolotl, chicken, cat"
		// Can't have unspecified "chicken" in the list
		assertCommandFailsWith(player, "list \"cat, wolf, axolotl, chicken, cat\"", "Item is not allowed in list at position 20: ... axolotl, <--[HERE]");

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithListTextArgumentWithConstantList() {
		Mut<List<String>> results = Mut.of();

		new CommandAPICommand("list")
			.withArguments(new ListArgumentBuilder<>("values", ", ")
				.withList(List.of("cat", "wolf", "axolotl"))
				.withStringMapper()
				.buildText())
			.executesPlayer((player, args) -> {
				results.set((List<String>) args.get(0));
			})
			.register();

		Player player = addPlayer("APlayer");

		// /list "cat, wolf, axolotl"
		assertStoresResult(player, "list \"cat, wolf, axolotl\"", results, List.of("cat", "wolf", "axolotl"));

		// /list "cat, wolf, axolotl, wolf"
		// No duplicates allowed
		assertCommandFailsWith(player, "list \"cat, wolf, axolotl, wolf\"", "Duplicate arguments are not allowed at position 20: ... axolotl, <--[HERE]");

		// /list "axolotl, wolf, chicken, cat"
		// Can't have unspecified "chicken" in the list
		assertCommandFailsWith(player, "list \"axolotl, wolf, chicken, cat\"", "Item is not allowed in list at position 15: ...tl, wolf, <--[HERE]");

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithListTextArgumentWithFunctionList() {
		Mut<List<String>> results = Mut.of();

		new CommandAPICommand("list")
			.withArguments(new ListArgumentBuilder<>("values", ", ")
				.withList(info -> List.of("cat", "wolf", "axolotl", info.sender().getName()))
				.withStringMapper()
				.buildText())
			.executesPlayer((player, args) -> {
				results.set((List<String>) args.get(0));
			})
			.register();

		Player player = addPlayer("APlayer");

		// /list "cat, wolf, axolotl"
		assertStoresResult(player, "list \"cat, wolf, axolotl\"", results, List.of("cat", "wolf", "axolotl"));

		// /list "axolotl, wolf, APlayer"
		assertStoresResult(player, "list \"axolotl, wolf, APlayer\"", results, List.of("axolotl", "wolf", "APlayer"));

		// /list "cat, wolf, axolotl, wolf"
		// No duplicates allowed
		assertCommandFailsWith(player, "list \"cat, wolf, axolotl, wolf\"", "Duplicate arguments are not allowed at position 20: ... axolotl, <--[HERE]");

		// /list "axolotl, wolf, chicken, cat"
		// Can't have unspecified "chicken" in the list
		assertCommandFailsWith(player, "list \"axolotl, wolf, chicken, cat\"", "Item is not allowed in list at position 15: ...tl, wolf, <--[HERE]");

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithListTextArgumentAfterListTextArgument() {
		Mut<List<String>> results = Mut.of();

		new CommandAPICommand("list")
			.withArguments(new ListArgumentBuilder<>("values", ", ")
				.withList(player -> List.of("cat", "wolf", "axolotl"))
				.withStringMapper()
				.buildText())
			.withArguments(new ListArgumentBuilder<>("morevalues", ", ")
				.withList(player -> List.of("pumpkin", "melon", "cake"))
				.withStringMapper()
				.buildText())
			.executesPlayer((player, args) -> {
				results.set((List<String>) args.get(0));
				results.set((List<String>) args.get(1));
			})
			.register();

		Player player = addPlayer("APlayer");

		// /list "cat, wolf, axolotl" "pumpkin melon"
		getServer().dispatchCommand(player, "list \"cat, wolf, axolotl\" \"pumpkin, melon\"");
		assertEquals(List.of("cat", "wolf", "axolotl"), results.get());
		assertEquals(List.of("pumpkin", "melon"), results.get());

		assertNoMoreResults(results);
	}

	/****************************
	 * Other ListArgument tests *
	 ****************************/

	@Test
	void executionTestWithListArgumentWithNonStringMapper() {
		Mut<List<Material>> results = Mut.of();

		new CommandAPICommand("list")
			.withArguments(new ListArgumentBuilder<Material>("values", ", ")
				.withList(() -> List.of(Material.DIRT, Material.GRASS_BLOCK, Material.DIAMOND))
				.withMapper(material -> material.getKey().getKey())
				.buildGreedy())
			.executesPlayer((player, args) -> {
				results.set((List<Material>) args.get(0));
			})
			.register();

		Player player = addPlayer("APlayer");

		// /list dirt, grass_block
		assertStoresResult(player, "list dirt, grass_block", results, List.of(Material.DIRT, Material.GRASS_BLOCK));

		// /list dirt, dirt
		// No duplicates allowed
		assertCommandFailsWith(player, "list dirt, dirt", "Duplicate arguments are not allowed at position 6: dirt, <--[HERE]");

		// /list iron_ingot
		// Can't have unspecified "iron_ingot" in the list
		assertCommandFailsWith(player, "list iron_ingot", "Item is not allowed in list at position 0: <--[HERE]");

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithListArgumentWithDefaultDelimiter() {
		Mut<List<String>> results = Mut.of();

		new CommandAPICommand("list")
			.withArguments(new ListArgumentBuilder<>("values")
				.withList(() -> List.of("cat", "wolf", "axolotl"))
				.withStringMapper()
				.buildGreedy())
			.executesPlayer((player, args) -> {
				results.set((List<String>) args.get(0));
			})
			.register();

		Player player = addPlayer("APlayer");

		// /list cat wolf axolotl
		assertStoresResult(player, "list cat wolf axolotl", results, List.of("cat", "wolf", "axolotl"));

		// /list cat wolf axolotl wolf
		// No duplicates allowed
		assertCommandFailsWith(player, "list cat wolf axolotl wolf", "Duplicate arguments are not allowed at position 17: ...f axolotl <--[HERE]");

		// /list axolotl wolf chicken, cat
		// Can't have unspecified "chicken" in the list
		assertCommandFailsWith(player, "list axolotl wolf chicken cat", "Item is not allowed in list at position 13: ...lotl wolf <--[HERE]");

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithListArgument() {
		new CommandAPICommand("list")
			.withArguments(new ListArgumentBuilder<>("values")
				.withList(() -> List.of("cat", "wolf", "axolotl"))
				.withStringMapper()
				.buildGreedy())
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// /list
		// All values should be suggested
		assertCommandSuggests(player, "list ", List.of("axolotl", "cat", "wolf"));

		// /list cat 
		// All values should be suggested except for "cat"
		assertCommandSuggests(player, "list cat ", List.of("axolotl", "wolf"));
	}

	@Test
	void suggestionTestWithListArgumentWithDuplicates() {
		new CommandAPICommand("list")
			.withArguments(new ListArgumentBuilder<>("values")
				.allowDuplicates(true)
				.withList(() -> List.of("cat", "wolf", "axolotl"))
				.withStringMapper()
				.buildGreedy())
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// /list
		// All values should be suggested
		assertCommandSuggests(player, "list ", List.of("axolotl", "cat", "wolf"));

		// /list cat 
		// All values should also be suggested
		assertCommandSuggests(player, "list cat ", List.of("axolotl", "cat", "wolf"));
	}

	@Test
	void suggestionTestWithListArgumentDelimiter() {
		new CommandAPICommand("list")
			.withArguments(new ListArgumentBuilder<>("values", ", ")
				.withList(() -> List.of("cat", "wolf", "axolotl"))
				.withStringMapper()
				.buildGreedy())
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// /list
		// The delimiter should be suggested
		assertCommandSuggests(player, "list cat", List.of("cat, "));
	}

	@Test
	void suggestionTestWithListArgumentText() {
		new CommandAPICommand("list")
			.withArguments(new ListArgumentBuilder<>("values")
				.withList(() -> List.of("cat", "wolf", "axolotl"))
				.withStringMapper()
				.buildText())
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// /list "cat
		// All values should be suggested except for "cat"
		assertCommandSuggests(player, "list \"cat ", List.of("axolotl", "wolf"));
	}
}
