package dev.jorel.commandapi.test;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.exceptions.OptionalArgumentException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for using optional arguments in CommandAPICommands
 */
class OptionalArgumentTests extends TestBase {

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
	void testNormalArgument() {
		Mut<String> type = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new StringArgument("string"))
			.executesPlayer((player, args) -> {
				type.set((String) args.get("string"));
			})
			.register();

		PlayerMock player = server.addPlayer();
		server.dispatchCommand(player, "test hello");
		server.dispatchCommand(player, "test world");

		assertEquals("hello", type.get());
		assertEquals("world", type.get());
	}

	@Test
	void testOptionalArgument() {
		Mut<String> type = Mut.of();

		new CommandAPICommand("test")
			.withOptionalArguments(new StringArgument("string"))
			.executesPlayer((player, args) -> {
				type.set((String) args.get("string"));
			})
			.register();

		PlayerMock player = server.addPlayer();
		server.dispatchCommand(player, "test");
		server.dispatchCommand(player, "test hello");

		assertNull(type.get());
		assertEquals("hello", type.get());
	}

	@Test
	void testTwoOptionalArguments() {
		Mut<String> type = Mut.of();

		new CommandAPICommand("test")
			.withOptionalArguments(new StringArgument("string1"))
			.withOptionalArguments(new StringArgument("string2"))
			.executesPlayer((player, args) -> {
				type.set((String) args.get("string1"));
				type.set((String) args.get("string2"));
			})
			.register();

		PlayerMock player = server.addPlayer();

		// "/test" should return null, null
		server.dispatchCommand(player, "test");
		assertNull(type.get());
		assertNull(type.get());

		// "/test hello" should return "hello", null
		server.dispatchCommand(player, "test hello");
		assertEquals("hello", type.get());
		assertNull(type.get());

		// "/test hello world" should return "hello", "world"
		server.dispatchCommand(player, "test hello world");
		assertEquals("hello", type.get());
		assertEquals("world", type.get());
	}

	@Test()
	public void testOptionalArgumentException() {
		// An optional argument followed by a required argument should throw
		// an OptionalArgumentException
		assertThrowsWithMessage(
			OptionalArgumentException.class,
			"Uncombined required arguments following optional arguments are not allowed! Going down the [test<LiteralArgument> string1<StringArgument>] branch, found a required argument string2<StringArgument> after an optional argument",
			() -> new CommandAPICommand("test")
				.withOptionalArguments(new StringArgument("string1"))
				.withArguments(new StringArgument("string2"))
				.executesPlayer(P_EXEC)
				.register()
		);

		// No need to worry: since we didn't actually add any arguments in `withArguments`, this is fine
		assertDoesNotThrow(
			() -> new CommandAPICommand("test")
				.withOptionalArguments(new StringArgument("string"))
				.withArguments()
				.executesPlayer(P_EXEC)
				.register()
		);

		// A required argument following a required argument that is linked
		// to an optional argument should throw an OptionalArgumentException
		assertThrowsWithMessage(
			OptionalArgumentException.class,
			"Uncombined required arguments following optional arguments are not allowed! Going down the [test<LiteralArgument> string1<StringArgument>] branch, found a required argument string3<StringArgument> after an optional argument",
			() -> new CommandAPICommand("test")
				.withOptionalArguments(new StringArgument("string1").combineWith(new StringArgument("string2")))
				.withArguments(new StringArgument("string3"))
				.executesPlayer(P_EXEC)
				.register()
		);
	}

	@Test
	void testOptionalArgumentDefault() {
		Mut<String> type = Mut.of();

		new CommandAPICommand("test")
			.withOptionalArguments(new StringArgument("string"))
			.executesPlayer((player, args) -> {
				type.set((String) args.getOptional("string").orElse("hello"));
			})
			.register();

		PlayerMock player = server.addPlayer();

		server.dispatchCommand(player, "test");
		server.dispatchCommand(player, "test world");

		assertEquals("hello", type.get());
		assertEquals("world", type.get());
	}

	@Test
	void testOptionalArgumentWithIndex() {
		Mut<String> type = Mut.of();

		new CommandAPICommand("test")
			.withOptionalArguments(new StringArgument("string"))
			.executesPlayer((player, args) -> {
				type.set((String) args.get(0));
			})
			.register();

		PlayerMock player = server.addPlayer();
		server.dispatchCommand(player, "test");
		server.dispatchCommand(player, "test hello");

		assertNull(type.get());
		assertEquals("hello", type.get());
	}

	@Test
	void testOptionalArgumentDefaultWithIndex() {
		Mut<String> type = Mut.of();

		new CommandAPICommand("test")
			.withOptionalArguments(new StringArgument("string"))
			.executesPlayer((player, args) -> {
				type.set((String) args.getOptional(0).orElse("hello"));
			})
			.register();

		PlayerMock player = server.addPlayer();
		server.dispatchCommand(player, "test");
		server.dispatchCommand(player, "test world");

		assertEquals("hello", type.get());
		assertEquals("world", type.get());
	}

	@Test
	void testOptionalArgumentDefaultWithIndexWithSupplier() {
		Mut<String> type = Mut.of();

		new CommandAPICommand("test")
			.withOptionalArguments(new StringArgument("string"))
			.executesPlayer((player, args) -> {
				type.set((String) args.getOptional(0).orElse("hello"));
			})
			.register();

		PlayerMock player = server.addPlayer();
		server.dispatchCommand(player, "test");
		server.dispatchCommand(player, "test world");

		assertEquals("hello", type.get());
		assertEquals("world", type.get());
	}

	@Test
	void testCombinedOptionalArguments() {
		Mut<Object> results = Mut.of();

		new CommandAPICommand("test")
			.withOptionalArguments(new StringArgument("string").combineWith(new IntegerArgument("number")))
			.withOptionalArguments(new DoubleArgument("double"))
			.executesPlayer(info -> {
				results.set(info.args().get("string"));
				results.set(info.args().get("number"));
				results.set(info.args().get("double"));
			})
			.register();

		PlayerMock player = server.addPlayer();

		// /test
		server.dispatchCommand(player, "test");
		assertNull(results.get());
		assertNull(results.get());
		assertNull(results.get());

		// /test hello
		assertCommandFailsWith(player, "test hello", "Unknown or incomplete command, see below for error at position 10: test hello<--[HERE]");

		// /test hello 5
		server.dispatchCommand(player, "test hello 5");
		assertEquals("hello", results.get());
		assertEquals(5, results.get());
		assertNull(results.get());

		// /test hello 5 6.0
		server.dispatchCommand(player, "test hello 5 6.0");
		assertEquals("hello", results.get());
		assertEquals(5, results.get());
		assertEquals(6.0, results.get());

		assertNoMoreResults(results);
	}

	@Test
	void testDoubleCombinedRequiredAndOptionalArguments() {
		Mut<Object[]> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(
				new LiteralArgument("1").setListed(true).combineWith(
					new LiteralArgument("2").setListed(true),
					new LiteralArgument("3").setListed(true)
				)
			)
			.withOptionalArguments(
				new LiteralArgument("4").setListed(true).combineWith(
					new LiteralArgument("5").setListed(true),
					new LiteralArgument("6").setListed(true)
				),
				new LiteralArgument("7").setListed(true).combineWith(
					new LiteralArgument("8").setListed(true),
					new LiteralArgument("9").setListed(true)
				)
			)
			.executesPlayer(info -> {
				results.set(info.args().args());
			})
			.register();

		PlayerMock player = server.addPlayer();

		// Required arguments
		assertCommandFailsWith(player, "test 1", "Unknown or incomplete command, see below for error at position 6: test 1<--[HERE]");
		assertCommandFailsWith(player, "test 1 2", "Unknown or incomplete command, see below for error at position 8: test 1 2<--[HERE]");

		assertStoresArrayResult(player, "test 1 2 3", results, "1", "2", "3");

		// First optional argument
		assertCommandFailsWith(player, "test 1 2 3 4", "Unknown or incomplete command, see below for error at position 12: ...st 1 2 3 4<--[HERE]");
		assertCommandFailsWith(player, "test 1 2 3 4 5", "Unknown or incomplete command, see below for error at position 14: ... 1 2 3 4 5<--[HERE]");

		assertStoresArrayResult(player, "test 1 2 3 4 5 6", results, "1", "2", "3", "4", "5", "6");

		// Second optional argument
		assertCommandFailsWith(player, "test 1 2 3 4 5 6 7", "Unknown or incomplete command, see below for error at position 18: ... 3 4 5 6 7<--[HERE]");
		assertCommandFailsWith(player, "test 1 2 3 4 5 6 7 8", "Unknown or incomplete command, see below for error at position 20: ... 4 5 6 7 8<--[HERE]");

		assertStoresArrayResult(player, "test 1 2 3 4 5 6 7 8 9", results, "1", "2", "3", "4", "5", "6", "7", "8", "9");

	}
}
