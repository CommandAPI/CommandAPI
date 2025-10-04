package dev.jorel.commandapi;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import dev.jorel.commandapi.commandsenders.AbstractCommandSender;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.ExecutionInfo;
import dev.jorel.commandapi.spying.ExecutionQueue;
import org.bukkit.command.CommandSender;
import org.opentest4j.AssertionFailedError;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Provides static methods to interact with CommandAPI commands registered in the test environment. This includes
 * verifying that a command runs successfully, fails with a certain error message, runs with certain arguments, or
 * suggests strings or tooltips.
 * <p>
 * Tip: The standard way to access static methods is by using the class name (e.g.
 * {@code CommandAPITestUtilities.assertCommandSucceeds(...)}). However, you can also call these methods with just the
 * method name ({@code assertCommandSucceeds(...)}), if you add a static import
 * ({@code import static dev.jorel.commandapi.CommandAPITestUtilities.assertCommandSucceeds;}) or make your test class
 * extend this class ({@code class Tests extends CommandAPITestUtilities {...}}).
 */
// In places where Assertions methods are called, the Supplier<String> option is used
//  so that the coverage report shows uncovered code if the tests do not check the scenario
//  where the assertion fails.
@SuppressWarnings("ExcessiveLambdaUsage")
public class CommandAPITestUtilities {
	///////////////////////
	// General utilities //
	///////////////////////

	/**
	 * @return The {@link MockCommandAPI} class currently loaded.
	 */
	public static MockCommandAPI getMockCommandAPI() {
		return MockCommandAPI.getInstance();
	}

	//////////////////////
	// Running commands //
	//////////////////////

	/**
	 * Attempts to run a CommandAPI command.
	 *
	 * @param sender  The {@link CommandSender} running the command.
	 * @param command The command to execute.
	 * @throws CommandSyntaxException If the command fails for any reason.
	 * @see #assertCommandSucceeds(CommandSender, String)
	 * @see #assertCommandFails(CommandSender, String, String)
	 */
	public static void dispatchCommand(CommandSender sender, String command) throws CommandSyntaxException {
		getMockCommandAPI().getCommandAPIBukkit().getBrigadierDispatcher().execute(command, new MockCommandSource(sender));
	}

	/**
	 * Runs a CommandAPI command and asserts that it succeeds.
	 *
	 * @param sender  The {@link CommandSender} running the command.
	 * @param command The command to execute.
	 * @throws AssertionFailedError If dispatching the command fails for any reason.
	 * @see #dispatchCommand(CommandSender, String)
	 */
	public static void assertCommandSucceeds(CommandSender sender, String command) {
		assertDoesNotThrow(
			() -> dispatchCommand(sender, command),
			() -> "Expected command dispatch to succeed"
		);
	}

	/**
	 * Runs a CommandAPI command as asserts that it fails.
	 *
	 * @param sender          The {@link CommandSender} running the command.
	 * @param command         The command to execute.
	 * @param expectedMessage The expected message thrown when the command fails.
	 * @return The {@link CommandSyntaxException} that resulted when the command failed.
	 * This can be ignored if no further checks are necessary.
	 * @throws AssertionFailedError If dispatching the command does not fail,
	 *                              or it fails with a different message than expected.
	 * @see #dispatchCommand(CommandSender, String)
	 */
	@CanIgnoreReturnValue
	public static CommandSyntaxException assertCommandFails(CommandSender sender, String command, String expectedMessage) {
		CommandSyntaxException exception = assertThrows(
			CommandSyntaxException.class,
			() -> dispatchCommand(sender, command),
			() -> "Expected command dispatch to fail"
		);

		String actualMessage = exception.getMessage();
		if (!Objects.equals(expectedMessage, actualMessage)) {
			throw new AssertionFailedError(
				"Expected command dispatch to fail with message <" + expectedMessage + ">, but got <" + actualMessage + ">"
			);
		}
		return exception;
	}

	/////////////////////////
	// Verifying arguments //
	/////////////////////////
	private static ExecutionInfo<CommandSender, AbstractCommandSender<? extends CommandSender>>
	getExecutionInfo(Runnable executeCommand) {
		ExecutionQueue executions = getMockCommandAPI().getCommandAPIHandlerSpy().getExecutionQueue();
		executions.clear();

		executeCommand.run();

		ExecutionInfo<CommandSender, AbstractCommandSender<? extends CommandSender>> execution = executions.poll();
		assertNotNull(execution, () -> "No CommandAPI executor was invoked");
		executions.assertNoMoreCommandsWereRun();

		return execution;
	}

	/**
	 * Returns the {@link ExecutionInfo} used when a CommandAPI command was executed successfully.
	 *
	 * @param sender  The {@link CommandSender} running the command.
	 * @param command The command to execute.
	 * @return The {@link ExecutionInfo} used when the command was executed.
	 * @throws AssertionFailedError If the command does not succeed or a CommandAPI executor is not invoked.
	 * @see #assertCommandSucceedsWithArguments(CommandSender, String, Object...)
	 * @see #assertCommandSucceedsWithArguments(CommandSender, String, Map)
	 * @see #assertCommandSucceeds(CommandSender, String)
	 * @see #getExecutionInfoOfFailingCommand(CommandSender, String, String)
	 */
	public static ExecutionInfo<CommandSender, AbstractCommandSender<? extends CommandSender>>
	getExecutionInfoOfSuccessfulCommand(CommandSender sender, String command) {
		return getExecutionInfo(() -> assertCommandSucceeds(sender, command));
	}

	/**
	 * Asserts that the given array of argument objects was available when a command executed successfully.
	 *
	 * @param sender         The {@link CommandSender} running the command.
	 * @param command        The command to execute.
	 * @param argumentsArray The {@link CommandArguments#args()} that is expected when the command executes.
	 * @throws AssertionFailedError If the command does not succeed or the available arguments array is different from expected.
	 * @see #assertCommandSucceedsWithArguments(CommandSender, String, Map)
	 * @see #assertCommandSucceeds(CommandSender, String)
	 * @see #getExecutionInfoOfSuccessfulCommand(CommandSender, String)
	 */
	public static void assertCommandSucceedsWithArguments(CommandSender sender, String command, Object... argumentsArray) {
		assertArrayEquals(argumentsArray, getExecutionInfoOfSuccessfulCommand(sender, command).args().args(),
			() -> "Argument arrays are not equal"
		);
	}

	/**
	 * Asserts that the given map of argument objects was available when a command executed successfully.
	 *
	 * @param sender       The {@link CommandSender} running the command.
	 * @param command      The command to execute.
	 * @param argumentsMap The {@link CommandArguments#argsMap()} that is expected when the command executes.
	 * @throws AssertionFailedError If the command does not succeed or the available arguments map is different from expected.
	 * @see #assertCommandSucceedsWithArguments(CommandSender, String, Object...)
	 * @see #assertCommandSucceeds(CommandSender, String)
	 * @see #getExecutionInfoOfSuccessfulCommand(CommandSender, String)
	 */
	public static void assertCommandSucceedsWithArguments(CommandSender sender, String command, Map<String, Object> argumentsMap) {
		assertEquals(argumentsMap, getExecutionInfoOfSuccessfulCommand(sender, command).args().argsMap(),
			() -> "Argument maps are not equal"
		);
	}

	/**
	 * Returns the {@link ExecutionInfo} used when a CommandAPI command was executed and threw an exception.
	 * Note that if the command dispatch fails because the input was not parsed properly, then a CommandAPI
	 * executor will not be invoked, and this assertion will fail.
	 *
	 * @param sender                 The {@link CommandSender} running the command.
	 * @param command                The command to execute.
	 * @param expectedFailureMessage The expected message thrown when the command fails.
	 * @return The {@link ExecutionInfo} used when the command was executed.
	 * @throws AssertionFailedError If dispatching the command does not fail, it fails with a different message than
	 *                              expected, or a CommandAPI executor is not invoked.
	 * @see #assertCommandFailsWithArguments(CommandSender, String, String, Object...)
	 * @see #assertCommandFailsWithArguments(CommandSender, String, String, Map)
	 * @see #assertCommandFails(CommandSender, String, String)
	 * @see #getExecutionInfoOfSuccessfulCommand(CommandSender, String)
	 */
	public static ExecutionInfo<CommandSender, AbstractCommandSender<? extends CommandSender>>
	getExecutionInfoOfFailingCommand(CommandSender sender, String command, String expectedFailureMessage) {
		return getExecutionInfo(() -> assertCommandFails(sender, command, expectedFailureMessage));
	}

	/**
	 * Asserts that the given array of argument objects was available when a command executed and threw an exception.
	 * Note that if the command dispatch fails because the input was not parsed properly, then a CommandAPI
	 * executor will not be invoked, and this assertion will fail.
	 *
	 * @param sender                 The {@link CommandSender} running the command.
	 * @param command                The command to execute.
	 * @param expectedFailureMessage The expected message thrown when the command fails.
	 * @param argumentsArray         The {@link CommandArguments#args()} that is expected when the command executes.
	 * @throws AssertionFailedError If the command does not fail or the available arguments array is different from expected.
	 * @see #assertCommandFailsWithArguments(CommandSender, String, String, Map)
	 * @see #assertCommandFails(CommandSender, String, String)
	 * @see #getExecutionInfoOfFailingCommand(CommandSender, String, String)
	 */
	public static void assertCommandFailsWithArguments(
		CommandSender sender, String command, String expectedFailureMessage,
		Object... argumentsArray
	) {
		assertArrayEquals(argumentsArray, getExecutionInfoOfFailingCommand(sender, command, expectedFailureMessage).args().args(),
			() -> "Argument arrays are not equal"
		);
	}

	/**
	 * Asserts that the given map of argument objects was available when a command executed successfully.
	 *
	 * @param sender                 The {@link CommandSender} running the command.
	 * @param command                The command to execute.
	 * @param expectedFailureMessage The expected message thrown when the command fails.
	 * @param argumentsMap           The {@link CommandArguments#argsMap()} that is expected when the command executes.
	 * @throws AssertionFailedError If the command does not fail or the available arguments map is different from expected.
	 * @see #assertCommandFailsWithArguments(CommandSender, String, String, Object...)
	 * @see #assertCommandFails(CommandSender, String, String)
	 * @see #getExecutionInfoOfFailingCommand(CommandSender, String, String)
	 */
	public static void assertCommandFailsWithArguments(
		CommandSender sender, String command, String expectedFailureMessage,
		Map<String, Object> argumentsMap
	) {
		assertEquals(argumentsMap, getExecutionInfoOfFailingCommand(sender, command, expectedFailureMessage).args().argsMap(),
			() -> "Argument maps are not equal"
		);
	}

	///////////////////////////
	// Verifying suggestions //
	///////////////////////////
	// Helper methods

	/**
	 * Returns the Brigadier {@link Suggestions} provided for the given command.
	 *
	 * @param sender  The {@link CommandSender} requesting suggestions.
	 * @param command The command to complete.
	 * @return The {@link Suggestions} provided for this command.
	 * @see #assertCommandSuggests(CommandSender, String, String...)
	 * @see #assertCommandSuggests(CommandSender, String, int, String...)
	 * @see #assertCommandSuggestsTooltips(CommandSender, String, Suggestion...)
	 * @see #assertCommandSuggestsTooltips(CommandSender, String, int, Suggestion...)
	 */
	public static Suggestions getSuggestions(CommandSender sender, String command) {
		CommandDispatcher<MockCommandSource> dispatcher = getMockCommandAPI().getCommandAPIBukkit().getBrigadierDispatcher();
		ParseResults<MockCommandSource> parse = dispatcher.parse(command, new MockCommandSource(sender));
		return dispatcher.getCompletionSuggestions(parse).join();
	}

	/**
	 * Asserts that all {@link Suggestion}s in the given list start at the given index.
	 *
	 * @param startingAt  The index that the suggestions should start at.
	 * @param suggestions A {@link List} of {@link Suggestion}s to check.
	 * @throws AssertionFailedError If any of the given {@link Suggestion}s do not start at the given index.
	 * @see #assertCommandSuggests(CommandSender, String, int, String...)
	 * @see #assertCommandSuggestsTooltips(CommandSender, String, int, Suggestion...)
	 */
	public static void assertSuggestionsStartAt(int startingAt, List<Suggestion> suggestions) {
		for (int i = 0; i < suggestions.size(); i++) {
			Suggestion suggestion = suggestions.get(i);

			int finalI = i;
			assertEquals(startingAt, suggestion.getRange().getStart(),
				() -> "Suggestion #" + finalI + " <" + suggestion + "> started at wrong index"
			);
		}
	}

	/**
	 * Asserts that the {@link Suggestion#getText()} of each given {@link Suggestion} matches an expected {@link String}.
	 *
	 * @param expectedSuggestions A {@link List} of {@link String}s that are expected to be suggested.
	 * @param actualSuggestions   The {@link List} of {@link Suggestion}s that was actually produced.
	 * @throws AssertionFailedError If any of the actual {@link Suggestion}s do not have the expected text.
	 * @see #assertCommandSuggests(CommandSender, String, String...)
	 * @see #assertCommandSuggests(CommandSender, String, int, String...)
	 */
	public static void assertSuggestionEquality(List<String> expectedSuggestions, List<Suggestion> actualSuggestions) {
		List<String> actualSuggestionStrings = new ArrayList<>(actualSuggestions.size());
		actualSuggestions.forEach(suggestion -> actualSuggestionStrings.add(suggestion.getText()));

		assertEquals(expectedSuggestions, actualSuggestionStrings, () -> "Suggestions did not match");
	}

	/**
	 * Asserts that the {@link Suggestion#getText()} and {@link Suggestion#getTooltip()} of each given
	 * {@link Suggestion} matches an expected {@link Suggestion}.
	 *
	 * @param expectedSuggestions A {@link List} of {@link Suggestion}s that are expected to be suggested.
	 * @param actualSuggestions   The {@link List} of {@link Suggestion}s that was actually produced.
	 * @throws AssertionFailedError If any of the actual {@link Suggestion}s do not have the expected text or tooltip.
	 * @see #makeTooltip(String, String)
	 * @see #assertCommandSuggestsTooltips(CommandSender, String, Suggestion...)
	 * @see #assertCommandSuggestsTooltips(CommandSender, String, int, Suggestion...)
	 */
	public static void assertSuggestionEqualityTooltips(List<Suggestion> expectedSuggestions, List<Suggestion> actualSuggestions) {
		List<String> actualSuggestionStrings = new ArrayList<>(actualSuggestions.size());
		List<String> actualSuggestionTooltips = new ArrayList<>(actualSuggestions.size());
		actualSuggestions.forEach(suggestion -> {
			actualSuggestionStrings.add(suggestion.getText());

			Message tooltip = suggestion.getTooltip();
			actualSuggestionTooltips.add(tooltip == null ? null : tooltip.getString());
		});

		List<String> expectedSuggestionStrings = new ArrayList<>(expectedSuggestions.size());
		List<String> expectedSuggestionTooltips = new ArrayList<>(expectedSuggestions.size());
		expectedSuggestions.forEach(suggestion -> {
			expectedSuggestionStrings.add(suggestion.getText());

			Message tooltip = suggestion.getTooltip();
			expectedSuggestionTooltips.add(tooltip == null ? null : tooltip.getString());
		});

		assertEquals(expectedSuggestionStrings, actualSuggestionStrings, () -> "Suggestions did not match");
		assertEquals(expectedSuggestionTooltips, actualSuggestionTooltips, () -> "Tooltips did not match");
	}

	// Public assertions

	/**
	 * Asserts that the suggestions provided for the given command have the given texts.
	 *
	 * @param sender              The {@link CommandSender} requesting suggestions.
	 * @param command             The command to complete.
	 * @param expectedSuggestions The suggestion texts that are expected.
	 * @throws AssertionFailedError If any of the actual suggestions do not have the expected text.
	 * @see #getSuggestions(CommandSender, String)
	 * @see #assertCommandSuggests(CommandSender, String, int, String...)
	 * @see #assertCommandSuggests(CommandSender, String, List)
	 */
	public static void assertCommandSuggests(CommandSender sender, String command, String... expectedSuggestions) {
		assertCommandSuggests(sender, command, Arrays.asList(expectedSuggestions));
	}

	/**
	 * Asserts that the suggestions provided for the given command start at the given index and have the given texts.
	 *
	 * @param sender              The {@link CommandSender} requesting suggestions.
	 * @param command             The command to complete.
	 * @param startingAt          The location in the command where the suggestions are expected.
	 * @param expectedSuggestions The suggestion texts that are expected.
	 * @throws AssertionFailedError If any of the actual suggestions do not start or the given index or have the expected text.
	 * @see #getSuggestions(CommandSender, String)
	 * @see #assertCommandSuggests(CommandSender, String, String...)
	 * @see #assertCommandSuggests(CommandSender, String, int, List)
	 */
	public static void assertCommandSuggests(CommandSender sender, String command, int startingAt, String... expectedSuggestions) {
		assertCommandSuggests(sender, command, startingAt, Arrays.asList(expectedSuggestions));
	}

	/**
	 * Asserts that the suggestions provided for the given command have the given texts.
	 *
	 * @param sender              The {@link CommandSender} requesting suggestions.
	 * @param command             The command to complete.
	 * @param expectedSuggestions The suggestion texts that are expected.
	 * @throws AssertionFailedError If any of the actual suggestions do not have the expected text.
	 * @see #getSuggestions(CommandSender, String)
	 * @see #assertCommandSuggests(CommandSender, String, int, List)
	 * @see #assertCommandSuggests(CommandSender, String, String...)
	 */
	public static void assertCommandSuggests(CommandSender sender, String command, List<String> expectedSuggestions) {
		assertSuggestionEquality(expectedSuggestions, getSuggestions(sender, command).getList());
	}

	/**
	 * Asserts that the suggestions provided for the given command start at the given index and have the given texts.
	 *
	 * @param sender              The {@link CommandSender} requesting suggestions.
	 * @param command             The command to complete.
	 * @param startingAt          The location in the command where the suggestions are expected.
	 * @param expectedSuggestions The suggestion texts that are expected.
	 * @throws AssertionFailedError If any of the actual suggestions do not start or the given index or have the expected text.
	 * @see #getSuggestions(CommandSender, String)
	 * @see #assertCommandSuggests(CommandSender, String, String...)
	 * @see #assertCommandSuggests(CommandSender, String, int, List)
	 */
	public static void assertCommandSuggests(CommandSender sender, String command, int startingAt, List<String> expectedSuggestions) {
		List<Suggestion> actualSuggestions = getSuggestions(sender, command).getList();
		assertSuggestionsStartAt(startingAt, actualSuggestions);
		assertSuggestionEquality(expectedSuggestions, actualSuggestions);
	}

	/**
	 * Creates a {@link Suggestion} with the given {@link String}s as its text and tooltip.
	 *
	 * @param text    The {@link String} for {@link Suggestion#getText()}.
	 * @param tooltip The {@link String} for {@link Suggestion#getTooltip()}. This may be null to indicate no tooltip.
	 * @return A new {@link Suggestion}.
	 * @see #assertCommandSuggestsTooltips(CommandSender, String, Suggestion...)
	 * @see #assertCommandSuggestsTooltips(CommandSender, String, int, Suggestion...)
	 */
	public static Suggestion makeTooltip(String text, String tooltip) {
		return new Suggestion(StringRange.at(0), text, tooltip == null ? null : () -> tooltip);
	}

	/**
	 * Asserts that the suggestions provided for the given command have the given texts and tooltips.
	 *
	 * @param sender              The {@link CommandSender} requesting suggestions.
	 * @param command             The command to complete.
	 * @param expectedSuggestions The suggestions that are expected.
	 * @throws AssertionFailedError If any of the actual suggestions do not have the expected text or tooltip.
	 * @see #makeTooltip(String, String)
	 * @see #getSuggestions(CommandSender, String)
	 * @see #assertCommandSuggestsTooltips(CommandSender, String, int, Suggestion...)
	 * @see #assertCommandSuggestsTooltips(CommandSender, String, List)
	 */
	public static void assertCommandSuggestsTooltips(CommandSender sender, String command, Suggestion... expectedSuggestions) {
		assertCommandSuggestsTooltips(sender, command, Arrays.asList(expectedSuggestions));
	}

	/**
	 * Asserts that the suggestions provided for the given command start at the given index and have the given texts and tooltips.
	 *
	 * @param sender              The {@link CommandSender} requesting suggestions.
	 * @param command             The command to complete.
	 * @param startingAt          The location in the command where the suggestions are expected.
	 * @param expectedSuggestions The suggestions that are expected.
	 * @throws AssertionFailedError If any of the actual suggestions do not start or the given index or have the expected text or tooltip.
	 * @see #makeTooltip(String, String)
	 * @see #getSuggestions(CommandSender, String)
	 * @see #assertCommandSuggestsTooltips(CommandSender, String, Suggestion...)
	 * @see #assertCommandSuggestsTooltips(CommandSender, String, int, List)
	 */
	public static void assertCommandSuggestsTooltips(CommandSender sender, String command, int startingAt, Suggestion... expectedSuggestions) {
		assertCommandSuggestsTooltips(sender, command, startingAt, Arrays.asList(expectedSuggestions));
	}

	/**
	 * Asserts that the suggestions provided for the given command have the given texts and tooltips.
	 *
	 * @param sender              The {@link CommandSender} requesting suggestions.
	 * @param command             The command to complete.
	 * @param expectedSuggestions The suggestions that are expected.
	 * @throws AssertionFailedError If any of the actual suggestions do not have the expected text or tooltip.
	 * @see #makeTooltip(String, String)
	 * @see #getSuggestions(CommandSender, String)
	 * @see #assertCommandSuggestsTooltips(CommandSender, String, int, List)
	 * @see #assertCommandSuggestsTooltips(CommandSender, String, Suggestion...)
	 */
	public static void assertCommandSuggestsTooltips(CommandSender sender, String command, List<Suggestion> expectedSuggestions) {
		assertSuggestionEqualityTooltips(expectedSuggestions, getSuggestions(sender, command).getList());
	}

	/**
	 * Asserts that the suggestions provided for the given command start at the given index and have the given texts and tooltips.
	 *
	 * @param sender              The {@link CommandSender} requesting suggestions.
	 * @param command             The command to complete.
	 * @param startingAt          The location in the command where the suggestions are expected.
	 * @param expectedSuggestions The suggestions that are expected.
	 * @throws AssertionFailedError If any of the actual suggestions do not start or the given index or have the expected text or tooltip.
	 * @see #makeTooltip(String, String)
	 * @see #getSuggestions(CommandSender, String)
	 * @see #assertCommandSuggestsTooltips(CommandSender, String, List)
	 * @see #assertCommandSuggestsTooltips(CommandSender, String, int, Suggestion...)
	 */
	public static void assertCommandSuggestsTooltips(CommandSender sender, String command, int startingAt, List<Suggestion> expectedSuggestions) {
		List<Suggestion> actualSuggestions = getSuggestions(sender, command).getList();
		assertSuggestionsStartAt(startingAt, actualSuggestions);
		assertSuggestionEqualityTooltips(expectedSuggestions, actualSuggestions);
	}
}
