package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.MathOperationArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import dev.jorel.commandapi.wrappers.MathOperation;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link MathOperationArgument}
 */
class ArgumentMathOperationTests extends TestBase {

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
	void executionTestWithMathOperationArgument() {
		Mut<MathOperation> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new MathOperationArgument("operation"))
			.executesPlayer((player, args) -> {
				results.set((MathOperation) args.get(0));
			})
			.register();

		Player player = addPlayer();

		// /test +=
		assertStoresResult(player, "test +=", results, MathOperation.ADD);

		// /test =
		assertStoresResult(player, "test =", results, MathOperation.ASSIGN);

		// /test /=
		assertStoresResult(player, "test /=", results, MathOperation.DIVIDE);

		// /test >
		assertStoresResult(player, "test >", results, MathOperation.MAX);

		// /test <
		assertStoresResult(player, "test <", results, MathOperation.MIN);

		// /test %=
		assertStoresResult(player, "test %=", results, MathOperation.MOD);

		// /test *=
		assertStoresResult(player, "test *=", results, MathOperation.MULTIPLY);

		// /test -=
		assertStoresResult(player, "test -=", results, MathOperation.SUBTRACT);

		// /test ><
		assertStoresResult(player, "test ><", results, MathOperation.SWAP);

		// /test invalid
		assertCommandFailsWith(player, "test invalid", "Invalid operation");

		assertNoMoreResults(results);
	}

	@SuppressWarnings("null")
	@Test
	void executionTestWithMathOperationArgumentArgumentApplication() {
		Mut<Integer> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new IntegerArgument("value1"))
			.withArguments(new MathOperationArgument("operation"))
			.withArguments(new IntegerArgument("value2"))
			.executesPlayer((player, args) -> {
				int value1 = (int) args.get("value1");
				int value2 = (int) args.get("value2");

				MathOperation operation = (MathOperation) args.get("operation");
				results.set(operation.apply(value1, value2));
			})
			.register();

		Player player = addPlayer();

		// /test 1 += 1
		assertStoresResult(player, "test 1 += 1", results, 2);

		// /test 1 = 2
		assertStoresResult(player, "test 1 = 2", results, 2);

		// /test 10 /= 5
		assertStoresResult(player, "test 10 /= 5", results, 2);
		// /test 5 /= 10
		assertStoresResult(player, "test 5 /= 10", results, 0);

		// /test 10 > 3
		assertStoresResult(player, "test 10 > 3", results, 10);
		// /test 3 > 10
		assertStoresResult(player, "test 3 > 10", results, 10);

		// /test 10 < 3
		assertStoresResult(player, "test 10 < 3", results, 3);
		// /test 3 < 10
		assertStoresResult(player, "test 3 < 10", results, 3);

		// /test 10 %= 7
		assertStoresResult(player, "test 10 %= 7", results, 3);
		// /test 7 %= 10
		assertStoresResult(player, "test 7 %= 10", results, 7);

		// /test 10 *= 10
		assertStoresResult(player, "test 10 *= 10", results, 100);

		// /test 20 -= 10
		assertStoresResult(player, "test 20 -= 10", results, 10);
		// /test 10 -= 20
		assertStoresResult(player, "test 10 -= 20", results, -10);

		// /test 1 >< 2
		assertStoresResult(player, "test 1 >< 2", results, 2);

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithMathOperationArgument() {
		new CommandAPICommand("test")
			.withArguments(new MathOperationArgument("operation"))
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// /test
		assertCommandSuggests(player, "test ", "%=", "*=", "+=", "-=", "/=", "<", "=", ">", "><");

		// /test >
		assertCommandSuggests(player, "test >", "><");
	}
}
