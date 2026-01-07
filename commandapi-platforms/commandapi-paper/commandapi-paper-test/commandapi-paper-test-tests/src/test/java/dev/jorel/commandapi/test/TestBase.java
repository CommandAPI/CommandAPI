package dev.jorel.commandapi.test;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import dev.jorel.commandapi.test.mockbukkit.CommandAPIPlayerMock;
import dev.jorel.commandapi.test.mockbukkit.CommandAPIServerMock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.function.Executable;
import org.opentest4j.AssertionFailedError;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TestBase {
	public static final MockPlatform mockPlatform = MockPlatform.getInstance();

	// Start and stop
	public <NBTContainer> void setUp(Class<NBTContainer> nbtContainerClass, Function<Object, NBTContainer> nbtContainerConstructor) {
		Main.nbtContainerClass = nbtContainerClass;
		Main.nbtContainerConstructor = nbtContainerConstructor;
		setUp(Main.class);
	}

	public void setUp() {
		setUp(Main.class);
	}

	public <T extends JavaPlugin> void setUp(Class<T> pluginClass) {
		mockPlatform.startServer(pluginClass);
	}

	public CommandAPIServerMock getServer() {
		return mockPlatform.getServer();
	}

	public void enableServer() {
		mockPlatform.enableServer();
	}

	public void tearDown() {
		mockPlatform.stopServer();
	}

	// Command dispatch
	public static final PlayerCommandExecutor P_EXEC = (player, args) -> {
	};

	public CommandAPIPlayerMock addPlayer() {
		return mockPlatform.getServer().addCommandAPIPlayer();
	}

	public CommandAPIPlayerMock addPlayer(String name) {
		return mockPlatform.getServer().addCommandAPIPlayer(name);
	}

	public <T> void assertStoresResult(CommandSender sender, String command, Mut<T> queue, T expected) {
		assertDoesNotThrow(() -> assertTrue(
			mockPlatform.getServer().dispatchThrowableCommand(sender, command),
			"Expected command dispatch to return true, but it gave false"));
		assertEquals(expected,
			assertDoesNotThrow(queue::get, "Expected to find <" + expected + "> in queue, but nothing was present")
		);
	}

	public <T extends Throwable> T assertThrowsWithMessage(Class<T> expectedType, String message, Executable executable) {
		T thrown = assertThrows(expectedType, executable);
		assertEquals(message, thrown.getMessage());
		return thrown;
	}

	public void assertCommandFailsWith(CommandSender sender, String command, String message) {
		CommandSyntaxException exception = assertThrows(CommandSyntaxException.class, () -> mockPlatform.getServer().dispatchThrowableCommand(sender, command));
		assertEquals(message, exception.getMessage());
	}

	public void assertNotCommandFailsWith(CommandSender sender, String command, String message) {
		CommandSyntaxException exception = assertThrows(CommandSyntaxException.class, () -> mockPlatform.getServer().dispatchThrowableCommand(sender, command));
		assertNotEquals(message, exception.getMessage());
	}

	public void assertNoMoreResults(Mut<?> mut) {
		assertThrows(NoSuchElementException.class, () -> mut.get(), "Expected there to be no results left, but at least one was found");
	}

	public String getDispatcherString() {
		try {
			return Files.readString(new File(mockPlatform.getPlugin().getDataFolder(), "command_registration.json").toPath());
		} catch (IOException e) {
			e.printStackTrace(System.out);
			return "";
		}
	}

	public void registerDummyCommands(CommandMap commandMap, String... commandName) {
		commandMap.registerAll("minecraft", Arrays.stream(commandName).map(name ->
			new Command(name) {
				@Override
				public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {
					return true;
				}
			}
		).collect(Collectors.toList()));
	}

	public <T> void compareLists(Collection<T> list1, Collection<T> list2) {
		Set<T> s1 = new LinkedHashSet<>(list1);
		Set<T> s2 = new LinkedHashSet<>(list2);

		Set<T> s1_2 = new LinkedHashSet<>(list1);
		Set<T> s2_2 = new LinkedHashSet<>(list2);

		s1.removeAll(s2);
		s2_2.removeAll(s1_2);
		System.out.println("List 1 has the following extra items: " + s1);
		System.out.println("List 2 has the following extra items: " + s2_2);
	}


	// Suggestions
	public Suggestion mkSuggestion(String text, String tooltip) {
		return new Suggestion(StringRange.at(0), text, new LiteralMessage(tooltip));
	}

	public boolean suggestionEquals(Suggestion suggestion1, Suggestion suggestion2) {
		// We only care about checking the text and tooltip message, nothing else
		return suggestion1.getText().equals(suggestion2.getText())
			&& suggestion1.getTooltip().getString().equals(suggestion2.getTooltip().getString());
	}

	public void assertSuggestionListEquals(List<Suggestion> expected, List<Suggestion> actual) {
		if (expected.size() != actual.size()) {
			throw new AssertionFailedError("List " + expected + " and " + actual + " have differing lengths");
		}
		for (int i = 0; i < expected.size(); i++) {
			if (!suggestionEquals(expected.get(i), actual.get(i))) {
				throw new AssertionFailedError("Expected: <" + expected + "> but was: <" + actual + ">");
			}
		}
	}

	public void assertNoSuggestions(CommandSender sender, String command) {
		List<Suggestion> suggestions = mockPlatform.getServer().getSuggestionsWithTooltips(sender, command);
		if (suggestions.size() != 0)
			throw new AssertionFailedError("Expected no suggestions, but found <" + suggestions + ">");
	}

	public void assertCommandSuggests(CommandSender sender, String command, String... expected) {
		assertCommandSuggests(sender, command, List.of(expected));
	}

	public void assertCommandSuggests(CommandSender sender, String command, List<String> expected) {
		assertEquals(expected, mockPlatform.getServer().getSuggestions(sender, command));
	}

	public void assertCommandSuggestsTooltips(CommandSender sender, String command, Suggestion... expected) {
		assertCommandSuggestsTooltips(sender, command, List.of(expected));
	}

	public void assertCommandSuggestsTooltips(CommandSender sender, String command, List<Suggestion> expected) {
		assertSuggestionListEquals(expected, mockPlatform.getServer().getSuggestionsWithTooltips(sender, command));
	}
}
