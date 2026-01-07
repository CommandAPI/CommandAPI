package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.exceptions.InvalidRangeException;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the primitive arguments {@link BooleanArgument},
 * {@link IntegerArgument} etc.
 */
@SuppressWarnings("null")
class ArgumentPrimitiveTests extends TestBase {

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
	void executionTestWithBooleanArgument() {
		Mut<Boolean> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new BooleanArgument("value"))
			.executesPlayer((player, args) -> {
				results.set((boolean) args.get(0));
			})
			.register();

		Player player = addPlayer();

		// /test true
		assertStoresResult(player, "test true", results, true);

		// /test false
		assertStoresResult(player, "test false", results, false);

		// /test aaaaa
		assertCommandFailsWith(player, "test aaaaa", "Invalid boolean, expected 'true' or 'false' but found 'aaaaa' at position 5: test <--[HERE]");

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithIntegerArgument() {
		Mut<Integer> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new IntegerArgument("value"))
			.executesPlayer((player, args) -> {
				results.set((int) args.get(0));
			})
			.register();

		Player player = addPlayer();

		// /test 10
		assertStoresResult(player, "test 10", results, 10);

		// /test -10
		assertStoresResult(player, "test -10", results, -10);

		// /test 0
		assertStoresResult(player, "test 0", results, 0);

		// /test 2147483647
		assertStoresResult(player, "test 2147483647", results, Integer.MAX_VALUE);

		// /test -2147483648
		assertStoresResult(player, "test -2147483648", results, Integer.MIN_VALUE);

		// /test 123hello
		assertCommandFailsWith(player, "test 123hello", "Expected whitespace to end one argument, but found trailing data at position 8: test 123<--[HERE]");

		// /test hello123
		assertCommandFailsWith(player, "test hello123", "Expected integer at position 5: test <--[HERE]");

		// /test 2147483648
		assertCommandFailsWith(player, "test 2147483648", "Invalid integer '2147483648' at position 5: test <--[HERE]");

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithBoundedIntegerArgument() {
		Mut<Integer> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new IntegerArgument("value", 10))
			.executesPlayer((player, args) -> {
				results.set((int) args.get(0));
			})
			.register();

		Player player = addPlayer();

		// /test 10
		assertStoresResult(player, "test 10", results, 10);

		// /test 20
		assertStoresResult(player, "test 20", results, 20);

		// /test 0
		assertCommandFailsWith(player, "test 0", "Integer must not be less than 10, found 0 at position 5: test <--[HERE]");

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithDoubleBoundedIntegerArgument() {
		Mut<Integer> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new IntegerArgument("value", 10, 20))
			.executesPlayer((player, args) -> {
				results.set((int) args.get(0));
			})
			.register();

		Player player = addPlayer();

		// /test 10
		assertStoresResult(player, "test 10", results, 10);

		// /test 15
		assertStoresResult(player, "test 15", results, 15);

		// /test 20
		assertStoresResult(player, "test 20", results, 20);

		// /test 0
		assertCommandFailsWith(player, "test 0", "Integer must not be less than 10, found 0 at position 5: test <--[HERE]");

		// /test 30
		assertCommandFailsWith(player, "test 30", "Integer must not be more than 20, found 30 at position 5: test <--[HERE]");

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithFloatArgument() {
		Mut<Float> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new FloatArgument("value"))
			.executesPlayer((player, args) -> {
				results.set((float) args.get(0));
			})
			.register();

		Player player = addPlayer();

		// /test 10
		assertStoresResult(player, "test 10.0", results, 10.0F);

		// /test -10
		assertStoresResult(player, "test -10.0", results, -10.0F);

		// /test 0
		assertStoresResult(player, "test 0.0", results, 0.0F);

		// /test Float.MAX_VALUE
		String floatMaxValue = String.format(Locale.ENGLISH, "%f", Float.MAX_VALUE);
		assertStoresResult(player, "test " + floatMaxValue, results, Float.MAX_VALUE);

		// /test -Float.MAX_VALUE
		String floatMinValue = String.format(Locale.ENGLISH, "%f", -Float.MAX_VALUE);
		assertStoresResult(player, "test " + floatMinValue, results, -Float.MAX_VALUE);

		// /test 123hello
		assertCommandFailsWith(player, "test 123hello", "Expected whitespace to end one argument, but found trailing data at position 8: test 123<--[HERE]");

		// /test hello123
		assertCommandFailsWith(player, "test hello123", "Expected float at position 5: test <--[HERE]");

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithFloatBoundedArgument() {
		Mut<Float> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new FloatArgument("value", 10.0F, 20.0F))
			.executesPlayer((player, args) -> {
				results.set((Float) args.get(0));
			})
			.register();

		Player player = addPlayer();

		// /test 10
		assertStoresResult(player, "test 10.0", results, 10.0F);

		// /test 15
		assertStoresResult(player, "test 15.0", results, 15.0F);

		// /test 20
		assertStoresResult(player, "test 20.0", results, 20.0F);

		// /test 0
		assertCommandFailsWith(player, "test 0", "Float must not be less than 10.0, found 0.0 at position 5: test <--[HERE]");

		// /test 30
		assertCommandFailsWith(player, "test 30", "Float must not be more than 20.0, found 30.0 at position 5: test <--[HERE]");

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithDoubleArgument() {
		Mut<Double> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new DoubleArgument("value"))
			.executesPlayer((player, args) -> {
				results.set((double) args.get(0));
			})
			.register();

		Player player = addPlayer();

		// /test 10
		assertStoresResult(player, "test 10.0", results, 10.0);

		// /test -10
		assertStoresResult(player, "test -10.0", results, -10.0);

		// /test 0
		assertStoresResult(player, "test 0.0", results, 0.0);

		// /test Double.MAX_VALUE
		String doubleMaxValue = String.format(Locale.ENGLISH, "%f", Double.MAX_VALUE);
		assertStoresResult(player, "test " + doubleMaxValue, results, Double.MAX_VALUE);

		// /test -Double.MAX_VALUE
		String doubleMinValue = String.format(Locale.ENGLISH, "%f", -Double.MAX_VALUE);
		assertStoresResult(player, "test " + doubleMinValue, results, -Double.MAX_VALUE);

		// /test 123hello
		assertCommandFailsWith(player, "test 123hello", "Expected whitespace to end one argument, but found trailing data at position 8: test 123<--[HERE]");

		// /test hello123
		assertCommandFailsWith(player, "test hello123", "Expected double at position 5: test <--[HERE]");

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithDoubleBoundedArgument() {
		Mut<Double> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new DoubleArgument("value", 10.0, 20.0))
			.executesPlayer((player, args) -> {
				results.set((double) args.get(0));
			})
			.register();

		Player player = addPlayer();

		// /test 10
		assertStoresResult(player, "test 10.0", results, 10.0);

		// /test 15
		assertStoresResult(player, "test 15.0", results, 15.0);

		// /test 20
		assertStoresResult(player, "test 20.0", results, 20.0);

		// /test 0
		assertCommandFailsWith(player, "test 0", "Double must not be less than 10.0, found 0.0 at position 5: test <--[HERE]");

		// /test 30
		assertCommandFailsWith(player, "test 30", "Double must not be more than 20.0, found 30.0 at position 5: test <--[HERE]");

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithLongArgument() {
		Mut<Long> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new LongArgument("value"))
			.executesPlayer((player, args) -> {
				results.set((long) args.get(0));
			})
			.register();

		Player player = addPlayer();

		// /test 10
		assertStoresResult(player, "test 10", results, 10L);

		// /test -10
		assertStoresResult(player, "test -10", results, -10L);

		// /test 0
		assertStoresResult(player, "test 0", results, 0L);

		// /test 9223372036854775807
		assertStoresResult(player, "test 9223372036854775807", results, Long.MAX_VALUE);

		// /test -9223372036854775808
		assertStoresResult(player, "test -9223372036854775808", results, Long.MIN_VALUE);

		// /test 123hello
		assertCommandFailsWith(player, "test 123hello", "Expected whitespace to end one argument, but found trailing data at position 8: test 123<--[HERE]");

		// /test hello123
		assertCommandFailsWith(player, "test hello123", "Expected long at position 5: test <--[HERE]");

		// /test 9223372036854775808
		assertCommandFailsWith(player, "test 9223372036854775808", "Invalid long '9223372036854775808' at position 5: test <--[HERE]");

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithBoundedLongArgument() {
		Mut<Long> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new LongArgument("value", 10))
			.executesPlayer((player, args) -> {
				results.set((long) args.get(0));
			})
			.register();

		Player player = addPlayer();

		// /test 10
		assertStoresResult(player, "test 10", results, 10L);

		// /test 20
		assertStoresResult(player, "test 20", results, 20L);

		// /test 0
		assertCommandFailsWith(player, "test 0", "Long must not be less than 10, found 0 at position 5: test <--[HERE]");

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithDoubleBoundedLongArgument() {
		Mut<Long> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new LongArgument("value", 10, 20))
			.executesPlayer((player, args) -> {
				results.set((long) args.get(0));
			})
			.register();

		Player player = addPlayer();

		// /test 10
		assertStoresResult(player, "test 10", results, 10L);

		// /test 15
		assertStoresResult(player, "test 15", results, 15L);

		// /test 20
		assertStoresResult(player, "test 20", results, 20L);

		// /test 0
		assertCommandFailsWith(player, "test 0", "Long must not be less than 10, found 0 at position 5: test <--[HERE]");

		// /test 30
		assertCommandFailsWith(player, "test 30", "Long must not be more than 20, found 30 at position 5: test <--[HERE]");

		assertNoMoreResults(results);
	}

	/*********************************
	 * Instantiation exception tests *
	 *********************************/

	@Test
	void exceptionTestWithIntegerArgumentInvalid() {
		// Test with max value less than min value
		assertThrows(InvalidRangeException.class, () -> new IntegerArgument("value", 20, 10));
	}

	@Test
	void exceptionTestWithLongArgumentInvalid() {
		// Test with max value less than min value
		assertThrows(InvalidRangeException.class, () -> new LongArgument("value", 20L, 10L));
	}

	@Test
	void exceptionTestWithFloatArgumentInvalid() {
		// Test with max value less than min value
		assertThrows(InvalidRangeException.class, () -> new FloatArgument("value", 20.0f, 10.0f));
	}

	@Test
	void exceptionTestWithDoubleArgumentInvalid() {
		// Test with max value less than min value
		assertThrows(InvalidRangeException.class, () -> new DoubleArgument("value", 20.0, 10.0));
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithBooleanArgument() {
		new CommandAPICommand("test")
			.withArguments(new BooleanArgument("value"))
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// /test
		// Both values should be suggested
		assertCommandSuggests(player, "test ", "false", "true");

		// /test f
		// Only "false" should be suggested
		assertCommandSuggests(player, "test f", "false");

		// /test t
		// Only "true" should be suggested
		assertCommandSuggests(player, "test t", "true");

		// /test x
		// Nothing should be suggested
		assertNoSuggestions(player, "test x");
	}

	@Test
	void suggestionTestWithIntegerArgument() {
		new CommandAPICommand("test")
			.withArguments(new IntegerArgument("value"))
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// /test
		assertNoSuggestions(player, "test ");
	}

	@Test
	void suggestionTestWithLongArgument() {
		new CommandAPICommand("test")
			.withArguments(new LongArgument("value"))
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// /test
		assertNoSuggestions(player, "test ");
	}

	@Test
	void suggestionTestWithFloatArgument() {
		new CommandAPICommand("test")
			.withArguments(new FloatArgument("value"))
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// /test
		assertNoSuggestions(player, "test ");
	}

	@Test
	void suggestionTestWithDoubleArgument() {
		new CommandAPICommand("test")
			.withArguments(new DoubleArgument("value"))
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// /test
		assertNoSuggestions(player, "test ");
	}
}
