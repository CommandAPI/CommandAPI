package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.mojang.brigadier.context.CommandContext;

import org.mockbukkit.mockbukkit.world.WorldMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentException;
import dev.jorel.commandapi.arguments.CustomArgument.MessageBuilder;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import net.kyori.adventure.text.Component;

/**
 * Tests for the {@link CustomArgument}
 */
class ArgumentCustomTests extends TestBase {

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

	// From the documentation
	public Argument<World> customWorldArgument(String nodeName) {
		return new CustomArgument<World, String>(new StringArgument(nodeName), info -> {
			World world = Bukkit.getWorld(info.input());
			if (world == null) {
				throw CustomArgumentException.fromMessageBuilder(new MessageBuilder("Unknown world: ").appendArgInput());
			} else {
				return world;
			}
		}).replaceSuggestions(ArgumentSuggestions.strings(info -> Bukkit.getWorlds().stream().map(World::getName).toArray(String[]::new)));
	}
	
	public Argument<World> customArgumentThatThrowsException(String nodeName) {
		return new CustomArgument<World, String>(new StringArgument(nodeName), info -> {
			throw new RuntimeException("👻 Spoopy Exception 👻");
		});
	}

	/*********
	 * Tests *
	 *********/

	@Test
	void executionTestWithCustomWorldArgument() {
		Mut<World> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(customWorldArgument("world"))
			.executesPlayer((player, args) -> {
				results.set(args.getUnchecked(0));
			})
			.register();

		PlayerMock player = server.addPlayer();

		WorldMock world1 = server.addSimpleWorld("world1");
		server.addSimpleWorld("world2");
		server.addSimpleWorld("world3");

		// /test unknownworld
		assertCommandFailsWith(player, "test unknownworld", "Unknown world: unknownworld");

		// /test world1
		server.dispatchCommand(player, "test world1");
		assertEquals(world1, results.get());

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithCustomArgumentThatThrowsException() {
		Mut<World> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(customArgumentThatThrowsException("world"))
			.executesPlayer((player, args) -> {
				results.set(args.getUnchecked(0));
			})
			.register();

		PlayerMock player = server.addPlayer();

		// /test world
		assertCommandFailsWith(player, "test world", "Error in executing command test world - world<--[HERE]");

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithCustomWorldArgument() {
		new CommandAPICommand("test")
			.withArguments(customWorldArgument("world"))
			.executesPlayer(P_EXEC)
			.register();

		PlayerMock player = server.addPlayer();

		server.addSimpleWorld("world1");
		server.addSimpleWorld("world2");
		server.addSimpleWorld("world3");

		// /test
		// We expect to see "world" because that's the default world
		assertEquals(List.of("world", "world1", "world2", "world3"), server.getSuggestions(player, "test "));
	}

	/***********************
	 * Instantiation tests *
	 ***********************/
	
	@Test
	void instantiationTestWithLiteralCustomArgument() {
		// LiteralArgument
		LiteralArgument literalArgument = new LiteralArgument("world");
		
		assertThrows(IllegalArgumentException.class, () -> {
			new CustomArgument<>(literalArgument, info -> 1);
		});
		
		// MultiLiteralArgument
		MultiLiteralArgument multiLiteralArgument = new MultiLiteralArgument("node", "hello", "world");
		
		assertThrows(IllegalArgumentException.class, () -> {
			new CustomArgument<>(multiLiteralArgument, info -> 1);
		});
	}
	
	@Test
	void instantiationTestWithCustomArgumentException() {
		final String result = "result";
		@SuppressWarnings("unchecked")
		final CommandContext<Object> context = Mockito.mock(CommandContext.class);
		Mockito.when(context.getInput()).thenReturn("input");
		
		// String
		assertDoesNotThrow(() -> CustomArgumentException.fromString("hello").toCommandSyntax(result, context));
		
		// Adventure Components
		Component adventureComponent = Component.text("hello");
		
		assertDoesNotThrow(() -> CustomArgumentException.fromAdventureComponent(adventureComponent).toCommandSyntax(result, context));
		
		// Null values
		assertThrows(IllegalStateException.class, () -> CustomArgumentException.fromString(null).toCommandSyntax(result, context));
		assertThrows(IllegalStateException.class, () -> CustomArgumentException.fromAdventureComponent(null).toCommandSyntax(result, context));
	}

}
