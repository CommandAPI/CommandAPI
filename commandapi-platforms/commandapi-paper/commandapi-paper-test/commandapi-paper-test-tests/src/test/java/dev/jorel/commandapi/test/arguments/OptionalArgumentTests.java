package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.exceptions.OptionalArgumentException;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for optional arguments
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

		Player player = addPlayer();
		getServer().dispatchCommand(player, "test hello");
		getServer().dispatchCommand(player, "test world");

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

		Player player = addPlayer();
		getServer().dispatchCommand(player, "test");
		getServer().dispatchCommand(player, "test hello");

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

		Player player = addPlayer();

		// "/test" should return null, null
		getServer().dispatchCommand(player, "test");
		assertNull(type.get());
		assertNull(type.get());

		// "/test hello" should return "hello", null
		getServer().dispatchCommand(player, "test hello");
		assertEquals("hello", type.get());
		assertNull(type.get());

		// "/test hello world" should return "hello", "world"
		getServer().dispatchCommand(player, "test hello world");
		assertEquals("hello", type.get());
		assertEquals("world", type.get());
	}

	@Test()
	public void testOptionalArgumentException() {
		Mut<String> type = Mut.of();

		// An optional argument followed by a required argument should throw
		// an OptionalArgumentException
		assertThrows(OptionalArgumentException.class, () -> {
			new CommandAPICommand("test")
				.withOptionalArguments(new StringArgument("string1"))
				.withArguments(new StringArgument("string2"))
				.executesPlayer((player, args) -> {
					type.set((String) args.get("string1"));
					type.set((String) args.get("string2"));
				})
				.register();
		});

		// A required argument following a required argument that is linked
		// to an optional argument should throw an OptionalArgumentException
		assertThrows(OptionalArgumentException.class, () -> {
			new CommandAPICommand("test")
				.withOptionalArguments(new StringArgument("string1").combineWith(new StringArgument("string2")))
				.withArguments(new StringArgument("string3"))
				.executesPlayer(P_EXEC)
				.register();
		});
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

		Player player = addPlayer();

		getServer().dispatchCommand(player, "test");
		getServer().dispatchCommand(player, "test world");

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

		Player player = addPlayer();
		getServer().dispatchCommand(player, "test");
		getServer().dispatchCommand(player, "test hello");

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

		Player player = addPlayer();
		getServer().dispatchCommand(player, "test");
		getServer().dispatchCommand(player, "test world");

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

		Player player = addPlayer();
		getServer().dispatchCommand(player, "test");
		getServer().dispatchCommand(player, "test world");

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

		Player player = addPlayer();

		// /test
		getServer().dispatchCommand(player, "test");
		assertNull(results.get());
		assertNull(results.get());
		assertNull(results.get());

		// /test hello
		assertCommandFailsWith(player, "test hello", "Unknown or incomplete command, see below for error at position 10: test hello<--[HERE]");

		// /test hello 5
		getServer().dispatchCommand(player, "test hello 5");
		assertEquals("hello", results.get());
		assertEquals(5, results.get());
		assertNull(results.get());

		// /test hello 5 6.0
		getServer().dispatchCommand(player, "test hello 5 6.0");
		assertEquals("hello", results.get());
		assertEquals(5, results.get());
		assertEquals(6.0, results.get());

		assertNoMoreResults(results);
	}
}
