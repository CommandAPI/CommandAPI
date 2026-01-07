package dev.jorel.commandapi.test;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.StringTooltip;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.MathOperationArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ArgumentSuggestionTests extends TestBase {

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

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void testReplaceSuggestionsConstants() {
		new CommandAPICommand("test")
			.withArguments(new StringArgument("arg").replaceSuggestions(ArgumentSuggestions.strings("cat", "apple", "wolf")))
			.executes((sender, args) -> {
			})
			.register();

		Player player = addPlayer("APlayer");

		// /test
		assertCommandSuggests(player, "test ", "apple", "cat", "wolf");
	}

	@Test
	void testReplaceSuggestionsConstantsCollection() {
		new CommandAPICommand("test")
			.withArguments(new StringArgument("arg").replaceSuggestions(ArgumentSuggestions.strings(List.of("cat", "apple", "wolf"))))
			.executes((sender, args) -> {
			})
			.register();

		Player player = addPlayer("APlayer");

		// /test
		assertCommandSuggests(player, "test ", "apple", "cat", "wolf");
	}

	@Test
	void testIncludeSuggestionsConstants() {
		// Our test has to involve an argument which already has some suggestions, so we'll
		// piggy-back on the MathOperationArgument because its suggestions are constant and
		// are easier to test (despite having a mysterious sort order)
		new CommandAPICommand("test")
			.withArguments(new MathOperationArgument("arg").includeSuggestions(ArgumentSuggestions.strings("^^")))
			.executes((sender, args) -> {
			})
			.register();

		Player player = addPlayer("APlayer");

		// /test
		assertCommandSuggests(player, "test ", "%=", "*=", "+=", "-=", "/=", "<", "=", ">", "><", "^^");
	}

	/**********************************
	 * Suggestion tests with tooltips *
	 **********************************/

	@Test
	void testReplaceSuggestionsConstantsTooltips() {
		new CommandAPICommand("test")
			.withArguments(new StringArgument("arg")
				.replaceSuggestions(ArgumentSuggestions.stringsWithTooltips(
					StringTooltip.ofString("cat", "a cat"),
					StringTooltip.ofString("apple", "an apple"),
					StringTooltip.ofString("wolf", "a wolf")
				)))
			.executes((sender, args) -> {
			})
			.register();

		Player player = addPlayer("APlayer");

		// /test
		assertCommandSuggestsTooltips(player, "test ",
			List.of(
				mkSuggestion("apple", "an apple"),
				mkSuggestion("cat", "a cat"),
				mkSuggestion("wolf", "a wolf")
			)
		);
	}

	@Test
	void testReplaceSuggestionsConstantsCollectionTooltips() {
		new CommandAPICommand("test")
			.withArguments(new StringArgument("arg")
				.replaceSuggestions(ArgumentSuggestions.stringsWithTooltips(
					List.of(
						StringTooltip.ofString("cat", "a cat"),
						StringTooltip.ofString("apple", "an apple"),
						StringTooltip.ofString("wolf", "a wolf")
					)
				)))
			.executes((sender, args) -> {
			})
			.register();

		Player player = addPlayer("APlayer");

		// /test
		assertCommandSuggestsTooltips(player, "test ",
			List.of(
				mkSuggestion("apple", "an apple"),
				mkSuggestion("cat", "a cat"),
				mkSuggestion("wolf", "a wolf")
			)
		);
	}
}
