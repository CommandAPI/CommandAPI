package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.DoubleRangeArgument;
import dev.jorel.commandapi.arguments.IntegerRangeArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import dev.jorel.commandapi.wrappers.DoubleRange;
import dev.jorel.commandapi.wrappers.IntegerRange;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

/**
 * Tests for the range arguments {@link IntegerRangeArgument} and {@link DoubleRangeArgument}
 */
class ArgumentRangeTests extends TestBase {

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
	void executionTestWithIntegerRangeArgument() {
		Mut<IntegerRange> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new IntegerRangeArgument("value"))
			.executesPlayer((player, args) -> {
				results.set((IntegerRange) args.get(0));
			})
			.register();

		Player player = addPlayer();

		// /test 0..10
		IntegerRange testZeroToTen = new IntegerRange(0, 10);
		assertStoresResult(player, "test 0..10", results, testZeroToTen);

		// /test -10..0
		IntegerRange testMinusTenToZero = new IntegerRange(-10, 0);
		assertStoresResult(player, "test -10..0", results, testMinusTenToZero);

		// /test 10
		IntegerRange testTenToTen = new IntegerRange(10, 10);
		assertStoresResult(player, "test 10", results, testTenToTen);

		// /test -2147483648..2147483647
		IntegerRange testMinIntToMaxInt = new IntegerRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
		assertStoresResult(player, "test -2147483648..2147483647", results, testMinIntToMaxInt);

		// /test ..2147483647
		IntegerRange testMinIntToMaxIntWithoutLower = IntegerRange.integerRangeLessThanOrEq(Integer.MAX_VALUE);
		assertStoresResult(player, "test ..2147483647", results, testMinIntToMaxIntWithoutLower);

		// /test -2147483648..
		IntegerRange testMinIntToMaxIntWithoutUpper = IntegerRange.integerRangeGreaterThanOrEq(Integer.MIN_VALUE);
		assertStoresResult(player, "test -2147483648..", results, testMinIntToMaxIntWithoutUpper);

		// /test -2147483648..2147483648
		assertCommandFailsWith(player, "test -2147483648..2147483648", "Invalid integer '2147483648' at position 5: test <--[HERE]");

		// /test -2147483649..2147483647
		assertCommandFailsWith(player, "test -2147483649..2147483647", "Invalid integer '-2147483649' at position 5: test <--[HERE]");

		// test hello123..10
		assertCommandFailsWith(player, "test hello123..10", "Expected value or range of values at position 5: test <--[HERE]");

		// test 123hello..10
		assertCommandFailsWith(player, "test 123hello..10", "Expected whitespace to end one argument, but found trailing data at position 8: test 123<--[HERE]");

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithDoubleRangeArgument() {
		Mut<DoubleRange> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new DoubleRangeArgument("value"))
			.executesPlayer((player, args) -> {
				results.set((DoubleRange) args.get(0));
			})
			.register();

		Player player = addPlayer();

		String floatMinValue = String.format(Locale.ENGLISH, "%f", -Float.MAX_VALUE);
		String floatMaxValue = String.format(Locale.ENGLISH, "%f", Float.MAX_VALUE);

		// /test 0.0..10.0
		DoubleRange testZeroToTen = new DoubleRange(0.0F, 10.0F);
		assertStoresResult(player, "test 0.0..10.0", results, testZeroToTen);

		// /test -10.0..0.0
		DoubleRange testMinusTenToZero = new DoubleRange(-10.0F, 0.0F);
		assertStoresResult(player, "test -10.0..0.0", results, testMinusTenToZero);

		// /test 10.0
		DoubleRange testTenToTen = new DoubleRange(10.0F, 10.0F);
		assertStoresResult(player, "test 10.0", results, testTenToTen);

		// /test -Float.MAX_VALUE..Float.MAX_VALUE
		DoubleRange testMinIntToMaxInt = new DoubleRange(-Float.MAX_VALUE, Float.MAX_VALUE);
		String floatMinToMax = "test " + floatMinValue + ".." + floatMaxValue;
		assertStoresResult(player, floatMinToMax, results, testMinIntToMaxInt);

		// /test ..Float.MAX_VALUE
		DoubleRange testMinIntToMaxIntWithoutLower = DoubleRange.doubleRangeLessThanOrEq(Float.MAX_VALUE);
		String floatUntilMax = "test .." + floatMaxValue;
		assertStoresResult(player, floatUntilMax, results, testMinIntToMaxIntWithoutLower);

		// /test -2147483648..
		DoubleRange testMinIntToMaxIntWithoutUpper = DoubleRange.doubleRangeGreaterThanOrEq(-Float.MAX_VALUE);
		String floatFromMin = "test " + floatMinValue + "..";
		assertStoresResult(player, floatFromMin, results, testMinIntToMaxIntWithoutUpper);

		// test hello123..10.0
		assertCommandFailsWith(player, "test hello123..10.0", "Expected value or range of values at position 5: test <--[HERE]");

		// test 123hello..10.0
		assertCommandFailsWith(player, "test 123hello..10.0", "Expected whitespace to end one argument, but found trailing data at position 8: test 123<--[HERE]");

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithIntegerRangeArgument() {
		new CommandAPICommand("test")
			.withArguments(new IntegerRangeArgument("value"))
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// /test
		assertNoSuggestions(player, "test ");
	}

	@Test
	void suggestionTestWithDoubleRangeArgument() {
		new CommandAPICommand("test")
			.withArguments(new DoubleRangeArgument("value"))
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// /test
		assertNoSuggestions(player, "test ");
	}
}
