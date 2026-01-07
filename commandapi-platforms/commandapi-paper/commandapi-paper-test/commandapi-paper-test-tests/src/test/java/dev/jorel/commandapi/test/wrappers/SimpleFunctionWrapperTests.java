package dev.jorel.commandapi.test.wrappers;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.test.TestBase;
import dev.jorel.commandapi.wrappers.SimpleFunctionWrapper;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleFunctionWrapperTests extends TestBase {

	/*********
	 * Setup *
	 *********/

	@BeforeEach
	public void setUp() {
		super.setUp();

		new CommandAPICommand("mysay")
			.withArguments(new GreedyStringArgument("message"))
			.executesPlayer(P_EXEC)
			.register();

		addPlayer();
	}

	@AfterEach
	public void tearDown() {
		super.tearDown();
	}

	/*********
	 * Tests *
	 *********/

	@Test
	void testSimpleFunctionWrapperFunctionLookup() {
		NamespacedKey key = NamespacedKey.fromString("ns:myfunc");
		getServer().addFunction(key, List.of("mysay hi", "mysay bye"));

		SimpleFunctionWrapper wrapper = SimpleFunctionWrapper.getFunction(key);
		assertEquals(key, wrapper.getKey());
		assertArrayEquals(new String[]{"mysay hi", "mysay bye"}, wrapper.getCommands());
	}

	@Test
	void testSimpleFunctionWrapperListOfFunctions() {
		List<NamespacedKey> keys = List.of(
			NamespacedKey.fromString("ns:myfunc1"),
			NamespacedKey.fromString("namespace:myfunc2"),
			NamespacedKey.fromString("myfunc3"));

		for (int i = 0; i < keys.size(); i++) {
			getServer().addFunction(keys.get(i), List.of("mysay hi " + i));
		}

		Set<NamespacedKey> functions = SimpleFunctionWrapper.getFunctions();
		assertEquals(keys.stream().collect(Collectors.toSet()), functions);
	}
}
