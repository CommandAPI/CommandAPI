package dev.jorel.commandapi.test.mockbukkit;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import dev.jorel.commandapi.Brigadier;
import dev.jorel.commandapi.commandsenders.AbstractCommandSender;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpTopic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.fail;

public interface CommandAPIServerMock<Source> extends Server {
	// Start server
	void onEnable();

	// Command dispatch
	// TODO: Not sure these old implementations make sense anymore in the Paper command system
	@SuppressWarnings("unchecked")
	default boolean dispatchThrowableCommand(CommandSender sender, String commandLine) throws CommandSyntaxException {
		String[] commands = commandLine.split(" ");
		String commandLabel = commands[0];
		Command command = getCommandMap().getCommand(commandLabel);

		if (command != null) {
			return serverDispatchCommand(sender, commandLine);
		} else {
			@SuppressWarnings("rawtypes")
			CommandDispatcher dispatcher = Brigadier.getCommandDispatcher();
			Object css = Brigadier.getBrigadierSourceFromCommandSender(sender);
			return dispatcher.execute(commandLine, css) != 0;
		}
	}

	@Override
	default boolean dispatchCommand(CommandSender sender, String commandLine) {
		try {
			return dispatchThrowableCommand(sender, commandLine);
		} catch (CommandSyntaxException e) {
			fail("Command '/" + commandLine + "' failed. If you expected this to fail, use dispatchThrowableCommand() instead.", e);
			return false;
		}
	}

	default int dispatchBrigadierCommand(CommandSender sender, String commandLine) {
		try {
			return dispatchThrowableBrigadierCommand(sender, commandLine);
		} catch (CommandSyntaxException e) {
			fail("Command '/" + commandLine + "' failed. If you expected this to fail, use dispatchThrowableBrigadierCommand() instead.", e);
			return 0;
		}
	}

	@SuppressWarnings("unchecked")
	default int dispatchThrowableBrigadierCommand(CommandSender sender, String commandLine) throws CommandSyntaxException {
		@SuppressWarnings("rawtypes")
		CommandDispatcher dispatcher = Brigadier.getCommandDispatcher();
		Object css = Brigadier.getBrigadierSourceFromCommandSender(sender);
		return dispatcher.execute(commandLine, css);
	}

	// Calls the super version of dispatchCommand, rather than our overridden implementation
	boolean serverDispatchCommand(CommandSender sender, String commandLine);

	// Suggestions
	default List<String> getSuggestions(CommandSender sender, String commandLine) {
		List<Suggestion> suggestions = getSuggestionsWithTooltips(sender, commandLine);

		List<String> suggestionsAsStrings = new ArrayList<>();
		for (Suggestion suggestion : suggestions) {
			suggestionsAsStrings.add(suggestion.getText());
		}

		return suggestionsAsStrings;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	default List<Suggestion> getSuggestionsWithTooltips(CommandSender sender, String commandLine) {
		CommandDispatcher dispatcher = Brigadier.getCommandDispatcher();
		Object css = Brigadier.getBrigadierSourceFromCommandSender(sender);
		ParseResults parseResults = dispatcher.parse(commandLine, css);
		Suggestions suggestions;
		try {
			suggestions = (Suggestions) dispatcher.getCompletionSuggestions(parseResults).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			suggestions = new Suggestions(StringRange.at(0), new ArrayList<>()); // Empty suggestions
		}

		return suggestions.getList();
	}

	// Mocked objects

	/**
	 * Creates a new {@link CommandAPIPlayerMock} and adds it to the server.
	 *
	 * @return A new {@link CommandAPIPlayerMock}.
	 */
	CommandAPIPlayerMock addCommandAPIPlayer();

	/**
	 * Creates a new {@link CommandAPIPlayerMock} with the given name and adds it to the server.
	 *
	 * @param name The name for the player
	 * @return A new {@link CommandAPIPlayerMock}.
	 */
	CommandAPIPlayerMock addCommandAPIPlayer(String name);

	/**
	 * Creates a new Bukkit {@link Player}. Unlike MockBukkit's {@link CommandAPIPlayerMock}, this uses Mockito to mock
	 * the CraftPlayer class, which allows the returned object to pass through VanillaCommandWrapper#getListener
	 * without error.
	 *
	 * @return A new {@link Player}.
	 */
	default Player setupMockedCraftPlayer() {
		return setupMockedCraftPlayer("defaultName");
	}

	/**
	 * Creates a new Bukkit {@link Player}. Unlike MockBukkit's {@link CommandAPIPlayerMock}, this uses Mockito to mock
	 * the CraftPlayer class, which allows the returned object to pass through VanillaCommandWrapper#getListener
	 * without error.
	 *
	 * @param name The name for the player
	 * @return A new {@link Player}.
	 */
	Player setupMockedCraftPlayer(String name);

	Entity createSimpleEntityMock();

	World addSimpleWorld(String name);

	void addFunction(NamespacedKey key, List<String> commands);

	void addTag(NamespacedKey key, List<List<String>> commands);

	int popFunctionCallbackResult();

	void addAdvancement(NamespacedKey key);

	default void addAdvancements(Collection<NamespacedKey> key) {
		key.forEach(this::addAdvancement);
	}

	Map<String, HelpTopic> getHelpMapTopics();

	Source getBrigadierSourceFromCommandSender(AbstractCommandSender<? extends CommandSender> senderWrapper);

	<T> T getMinecraftServer();


	// TODO: Are these still needed?
//	public CommandAPIServerMock() {
//		// The functionality of these methods is not important, but they shouldn't throw an `UnimplementedOperationException`
//		//  These methods are called indirectly when `CommandNamespaceTests#enableWithNamespaces` constructs a `PermissibleBase`
//		PluginManagerMock pluginManagerSpy = Mockito.spy(this.getPluginManager());
//		Mockito.doNothing().when(pluginManagerSpy).unsubscribeFromDefaultPerms(isA(Boolean.class), isA(Permissible.class));
//		Mockito.doNothing().when(pluginManagerSpy).subscribeToDefaultPerms(isA(Boolean.class), isA(Permissible.class));
//		MockPlatform.setField(ServerMock.class, "pluginManager", this, pluginManagerSpy);
//	}
//
////	@Override
//	public boolean shouldSendChatPreviews() {
//		return true;
//	}
//
//	// Registries
//
////	@Override
//	public <T extends Keyed> @Nullable Registry<T> getRegistry(@NotNull Class<T> tClass) {
//		return MockPlatform.getInstance().getRegistry(tClass);
////		if (tClass.equals(Enchantment.class)) {
////			return new Registry() {
////				@Nullable
////				public T get(@NotNull NamespacedKey var1) {
////					System.out.println("Accessing " + tClass + ":" + var1);
////					return null;
////                }
////
////				@NotNull
////				public Stream<T> stream() {
////					List<T> list = List.of();
////					return list.stream();
////                }
////
////				public Iterator<T> iterator() {
////					List<T> list = List.of();
////					return list.iterator();
////                }
////            };
////        } else {
////			return null;
////        }
//	}
//
//	// TODO: Commenting this out for now because we've not sorted out 1.20.2
//	// support with MockBukkit, this almost certainly will require all sorts of
//	// hoops to jump through because apparently MockBukkit may have changed the
//	// method signature for this and we REALLY REALLY don't want to deal with
//	// that right now
////	@Override
////	public ItemFactory getItemFactory() {
////		// Thanks MockBukkit, but we REALLY need to access
////		// the raw CraftItemMeta objects for the ItemStackArgument <3
////		return MockPlatform.getInstance().getItemFactory();
////    }
//
//	// 1.16 and 1.17 MockServers do not implement this method, but other versions do
//	//  Easiest to just always override this method
//	//  This is copied from MockBukkit-v1.18
//	private final StandardMessenger messenger = new StandardMessenger();
//	@Override
//	public @NotNull Messenger getMessenger() {
//		return messenger;
//	}
}
