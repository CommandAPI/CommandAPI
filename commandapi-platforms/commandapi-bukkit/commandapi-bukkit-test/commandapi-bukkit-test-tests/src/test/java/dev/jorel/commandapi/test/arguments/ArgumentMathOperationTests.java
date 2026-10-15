package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.MathOperationArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import dev.jorel.commandapi.wrappers.MathOperation;

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

		PlayerMock player = server.addPlayer();

		// /test +=
		server.dispatchCommand(player, "test +=");
		assertEquals(MathOperation.ADD, results.get());

		// /test =
		server.dispatchCommand(player, "test =");
		assertEquals(MathOperation.ASSIGN, results.get());

		// /test /=
		server.dispatchCommand(player, "test /=");
		assertEquals(MathOperation.DIVIDE, results.get());

		// /test >
		server.dispatchCommand(player, "test >");
		assertEquals(MathOperation.MAX, results.get());

		// /test <
		server.dispatchCommand(player, "test <");
		assertEquals(MathOperation.MIN, results.get());

		// /test %=
		server.dispatchCommand(player, "test %=");
		assertEquals(MathOperation.MOD, results.get());

		// /test *=
		server.dispatchCommand(player, "test *=");
		assertEquals(MathOperation.MULTIPLY, results.get());

		// /test -=
		server.dispatchCommand(player, "test -=");
		assertEquals(MathOperation.SUBTRACT, results.get());

		// /test ><
		server.dispatchCommand(player, "test ><");
		assertEquals(MathOperation.SWAP, results.get());

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

		PlayerMock player = server.addPlayer();

		// /test 1 += 1
		server.dispatchCommand(player, "test 1 += 1");
		assertEquals(2, results.get());

		// /test 1 = 2
		server.dispatchCommand(player, "test 1 = 2");
		assertEquals(2, results.get());

		// /test 10 /= 5
		server.dispatchCommand(player, "test 10 /= 5");
		assertEquals(2, results.get());
		// /test 5 /= 10
		server.dispatchCommand(player, "test 5 /= 10");
		assertEquals(0, results.get());

		// /test 10 > 3
		server.dispatchCommand(player, "test 10 > 3");
		assertEquals(10, results.get());
		// /test 3 > 10
		server.dispatchCommand(player, "test 3 > 10");
		assertEquals(10, results.get());

		// /test 10 < 3
		server.dispatchCommand(player, "test 10 < 3");
		assertEquals(3, results.get());
		// /test 3 < 10
		server.dispatchCommand(player, "test 3 < 10");
		assertEquals(3, results.get());

		// /test 10 %= 7
		server.dispatchCommand(player, "test 10 %= 7");
		assertEquals(3, results.get());
		// /test 7 %= 10
		server.dispatchCommand(player, "test 7 %= 10");
		assertEquals(7, results.get());

		// /test 10 *= 10
		server.dispatchCommand(player, "test 10 *= 10");
		assertEquals(100, results.get());

		// /test 20 -= 10
		server.dispatchCommand(player, "test 20 -= 10");
		assertEquals(10, results.get());
		// /test 10 -= 20
		server.dispatchCommand(player, "test 10 -= 20");
		assertEquals(-10, results.get());

		// /test 1 >< 2
		server.dispatchCommand(player, "test 1 >< 2");
		assertEquals(2, results.get());

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

		PlayerMock player = server.addPlayer();

		// /test
		assertEquals(List.of("%=", "*=", "+=", "-=", "/=", "<", "=", ">", "><"), server.getSuggestions(player, "test "));
		
		// /test >
		assertEquals(List.of("><"), server.getSuggestions(player, "test >"));
	}

}
