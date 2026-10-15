package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.FloatArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.LongArgument;
import dev.jorel.commandapi.exceptions.BadLiteralException;
import dev.jorel.commandapi.exceptions.InvalidRangeException;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;

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

		PlayerMock player = server.addPlayer();

		// /test true
		server.dispatchCommand(player, "test true");
		assertEquals(true, results.get());

		// /test false
		server.dispatchCommand(player, "test false");
		assertEquals(false, results.get());

		// /test aaaaa
		assertCommandFailsWith(player, "test aaaaa", "Invalid boolean: expected 'true' or 'false' but found 'aaaaa' at position 5: test <--[HERE]");

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

		PlayerMock player = server.addPlayer();

		// /test 10
		server.dispatchCommand(player, "test 10");
		assertEquals(10, results.get());

		// /test -10
		server.dispatchCommand(player, "test -10");
		assertEquals(-10, results.get());

		// /test 0
		server.dispatchCommand(player, "test 0");
		assertEquals(0, results.get());

		// /test 2147483647
		server.dispatchCommand(player, "test 2147483647");
		assertEquals(Integer.MAX_VALUE, results.get());

		// /test -2147483648
		server.dispatchCommand(player, "test -2147483648");
		assertEquals(Integer.MIN_VALUE, results.get());

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

		PlayerMock player = server.addPlayer();

		// /test 10
		server.dispatchCommand(player, "test 10");
		assertEquals(10, results.get());

		// /test 20
		server.dispatchCommand(player, "test 20");
		assertEquals(20, results.get());

		// /test 0
		assertCommandFailsWith(player, "test 0", "Integer must not be less than 10: found 0 at position 5: test <--[HERE]");

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

		PlayerMock player = server.addPlayer();

		// /test 10
		server.dispatchCommand(player, "test 10");
		assertEquals(10, results.get());

		// /test 15
		server.dispatchCommand(player, "test 15");
		assertEquals(15, results.get());

		// /test 20
		server.dispatchCommand(player, "test 20");
		assertEquals(20, results.get());

		// /test 0
		assertCommandFailsWith(player, "test 0", "Integer must not be less than 10: found 0 at position 5: test <--[HERE]");

		// /test 30
		assertCommandFailsWith(player, "test 30", "Integer must not be more than 20: found 30 at position 5: test <--[HERE]");

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

		PlayerMock player = server.addPlayer();

		// /test 10
		server.dispatchCommand(player, "test 10.0");
		assertEquals(10.0F, results.get());

		// /test -10
		server.dispatchCommand(player, "test -10.0");
		assertEquals(-10.0F, results.get());

		// /test 0
		server.dispatchCommand(player, "test 0.0");
		assertEquals(0.0F, results.get());

		// /test Float.MAX_VALUE
		String floatMaxValue = String.format(Locale.ENGLISH, "%f", Float.MAX_VALUE);
		server.dispatchCommand(player, "test " + floatMaxValue);
		assertEquals(Float.MAX_VALUE, results.get());

		// /test -Float.MAX_VALUE
		String floatMinValue = String.format(Locale.ENGLISH, "%f", -Float.MAX_VALUE);
		server.dispatchCommand(player, "test " + floatMinValue);
		assertEquals(-Float.MAX_VALUE, results.get());

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

		PlayerMock player = server.addPlayer();

		// /test 10
		server.dispatchCommand(player, "test 10.0");
		assertEquals(10.0F, results.get());

		// /test 15
		server.dispatchCommand(player, "test 15.0");
		assertEquals(15.0F, results.get());

		// /test 20
		server.dispatchCommand(player, "test 20.0");
		assertEquals(20.0F, results.get());

		// /test 0
		assertCommandFailsWith(player, "test 0", "Float must not be less than 10.0: found 0.0 at position 5: test <--[HERE]");

		// /test 30
		assertCommandFailsWith(player, "test 30", "Float must not be more than 20.0: found 30.0 at position 5: test <--[HERE]");

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

		PlayerMock player = server.addPlayer();

		// /test 10
		server.dispatchCommand(player, "test 10.0");
		assertEquals(10.0, results.get());

		// /test -10
		server.dispatchCommand(player, "test -10.0");
		assertEquals(-10.0, results.get());

		// /test 0
		server.dispatchCommand(player, "test 0.0");
		assertEquals(0.0, results.get());

		// /test Double.MAX_VALUE
		String doubleMaxValue = String.format(Locale.ENGLISH, "%f", Double.MAX_VALUE);
		server.dispatchCommand(player, "test " + doubleMaxValue);
		assertEquals(Double.MAX_VALUE, results.get());

		// /test -Double.MAX_VALUE
		String doubleMinValue = String.format(Locale.ENGLISH, "%f", -Double.MAX_VALUE);
		server.dispatchCommand(player, "test " + doubleMinValue);
		assertEquals(-Double.MAX_VALUE, results.get());

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

		PlayerMock player = server.addPlayer();

		// /test 10
		server.dispatchCommand(player, "test 10.0");
		assertEquals(10.0, results.get());

		// /test 15
		server.dispatchCommand(player, "test 15.0");
		assertEquals(15.0, results.get());

		// /test 20
		server.dispatchCommand(player, "test 20.0");
		assertEquals(20.0, results.get());

		// /test 0
		assertCommandFailsWith(player, "test 0", "Double must not be less than 10.0: found 0.0 at position 5: test <--[HERE]");

		// /test 30
		assertCommandFailsWith(player, "test 30", "Double must not be more than 20.0: found 30.0 at position 5: test <--[HERE]");

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

		PlayerMock player = server.addPlayer();

		// /test 10
		server.dispatchCommand(player, "test 10");
		assertEquals(10, results.get());

		// /test -10
		server.dispatchCommand(player, "test -10");
		assertEquals(-10, results.get());

		// /test 0
		server.dispatchCommand(player, "test 0");
		assertEquals(0, results.get());

		// /test 9223372036854775807
		server.dispatchCommand(player, "test 9223372036854775807");
		assertEquals(Long.MAX_VALUE, results.get());

		// /test -9223372036854775808
		server.dispatchCommand(player, "test -9223372036854775808");
		assertEquals(Long.MIN_VALUE, results.get());

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

		PlayerMock player = server.addPlayer();

		// /test 10
		server.dispatchCommand(player, "test 10");
		assertEquals(10, results.get());

		// /test 20
		server.dispatchCommand(player, "test 20");
		assertEquals(20, results.get());

		// /test 0
		assertCommandFailsWith(player, "test 0", "Long must not be less than 10: found 0 at position 5: test <--[HERE]");

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

		PlayerMock player = server.addPlayer();

		// /test 10
		server.dispatchCommand(player, "test 10");
		assertEquals(10, results.get());

		// /test 15
		server.dispatchCommand(player, "test 15");
		assertEquals(15, results.get());

		// /test 20
		server.dispatchCommand(player, "test 20");
		assertEquals(20, results.get());

		// /test 0
		assertCommandFailsWith(player, "test 0", "Long must not be less than 10: found 0 at position 5: test <--[HERE]");

		// /test 30
		assertCommandFailsWith(player, "test 30", "Long must not be more than 20: found 30 at position 5: test <--[HERE]");

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
		assertThrows(InvalidRangeException.class, () -> new LongArgument("value", 20l, 10l));
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

		PlayerMock player = server.addPlayer();

		// /test
		// Both values should be suggested
		assertEquals(List.of("false", "true"), server.getSuggestions(player, "test "));

		// /test f
		// Only "false" should be suggested
		assertEquals(List.of("false"), server.getSuggestions(player, "test f"));

		// /test t
		// Only "true" should be suggested
		assertEquals(List.of("true"), server.getSuggestions(player, "test t"));

		// /test x
		// Nothing should be suggested
		assertEquals(List.of(), server.getSuggestions(player, "test x"));
	}

	@Test
	void suggestionTestWithIntegerArgument() {
		new CommandAPICommand("test")
			.withArguments(new IntegerArgument("value"))
			.executesPlayer(P_EXEC)
			.register();

		PlayerMock player = server.addPlayer();

		// /test
		assertEquals(List.of(), server.getSuggestions(player, "test "));
	}

	@Test
	void suggestionTestWithLongArgument() {
		new CommandAPICommand("test")
			.withArguments(new LongArgument("value"))
			.executesPlayer(P_EXEC)
			.register();

		PlayerMock player = server.addPlayer();

		// /test
		assertEquals(List.of(), server.getSuggestions(player, "test "));
	}

	@Test
	void suggestionTestWithFloatArgument() {
		new CommandAPICommand("test")
			.withArguments(new FloatArgument("value"))
			.executesPlayer(P_EXEC)
			.register();

		PlayerMock player = server.addPlayer();

		// /test
		assertEquals(List.of(), server.getSuggestions(player, "test "));
	}

	@Test
	void suggestionTestWithDoubleArgument() {
		new CommandAPICommand("test")
			.withArguments(new DoubleArgument("value"))
			.executesPlayer(P_EXEC)
			.register();

		PlayerMock player = server.addPlayer();

		// /test
		assertEquals(List.of(), server.getSuggestions(player, "test "));
	}

}
