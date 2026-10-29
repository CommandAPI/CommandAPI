package dev.jorel.commandapi.test;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.isA;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.mockbukkit.mockbukkit.plugin.PluginManagerMock;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.advancement.Advancement;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.VersionCommand;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.loot.LootTable;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;

import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.Brigadier;
import org.mockito.Mockito;

public class CommandAPIServerMock extends ServerMock {
	public CommandAPIServerMock() {
		// The functionality of these methods is not important, but they shouldn't throw an `UnimplementedOperationException`
		//  These methods are called indirectly when `CommandNamespaceTests#enableWithNamespaces` constructs a `PermissibleBase`
		PluginManagerMock pluginManagerSpy = Mockito.spy(this.getPluginManager());
		Mockito.doNothing().when(pluginManagerSpy).unsubscribeFromDefaultPerms(isA(Boolean.class), isA(Permissible.class));
		Mockito.doNothing().when(pluginManagerSpy).subscribeToDefaultPerms(isA(Boolean.class), isA(Permissible.class));
		MockPlatform.setField(ServerMock.class, "pluginManager", this, pluginManagerSpy);

		// Real servers always have Bukkit's /version command. MockBukkit 4 creates its command map with the
		//  constructor that doesn't register Bukkit's default commands.
		getCommandMap().register("bukkit", new VersionCommand("version"));
	}

	@SuppressWarnings("unchecked")
	public boolean dispatchThrowableCommand(CommandSender sender, String commandLine) throws CommandSyntaxException {
		String[] commands = commandLine.split(" ");
		String commandLabel = commands[0];
		Command command = getCommandMap().getCommand(commandLabel);

		if (command != null) {
			return super.dispatchCommand(sender, commandLine);
		} else {
			@SuppressWarnings("rawtypes")
			CommandDispatcher dispatcher = Brigadier.getCommandDispatcher();
			Object css = Brigadier.getBrigadierSourceFromCommandSender(sender);
			return dispatcher.execute(commandLine, css) != 0;
		}
	}

	@Override
	public boolean dispatchCommand(CommandSender sender, String commandLine) {
		try {
			return dispatchThrowableCommand(sender, commandLine);
		} catch (CommandSyntaxException e) {
			fail("Command '/" + commandLine + "' failed. If you expected this to fail, use dispatchThrowableCommand() instead.", e);
			return false;
		}
	}

	public int dispatchBrigadierCommand(CommandSender sender, String commandLine) {
		try {
			return dispatchThrowableBrigadierCommand(sender, commandLine);
		} catch (CommandSyntaxException e) {
			fail("Command '/" + commandLine + "' failed. If you expected this to fail, use dispatchThrowableBrigadierCommand() instead.", e);
			return 0;
		}
	}

	@SuppressWarnings("unchecked")
	public int dispatchThrowableBrigadierCommand(CommandSender sender, String commandLine) throws CommandSyntaxException {
		@SuppressWarnings("rawtypes")
		CommandDispatcher dispatcher = Brigadier.getCommandDispatcher();
		Object css = Brigadier.getBrigadierSourceFromCommandSender(sender);
		return dispatcher.execute(commandLine, css);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<String> getSuggestions(CommandSender sender, String commandLine) {
		CommandDispatcher dispatcher = Brigadier.getCommandDispatcher();
		Object css = Brigadier.getBrigadierSourceFromCommandSender(sender);
		ParseResults parseResults = dispatcher.parse(commandLine, css);
		Suggestions suggestions = null;
		try {
			suggestions = (Suggestions) dispatcher.getCompletionSuggestions(parseResults).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			suggestions = new Suggestions(StringRange.at(0), new ArrayList<>()); // Empty suggestions
		}

		List<String> suggestionsAsStrings = new ArrayList<>();
		for (Suggestion suggestion : suggestions.getList()) {
			suggestionsAsStrings.add(suggestion.getText());
		}

		return suggestionsAsStrings;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Suggestion> getSuggestionsWithTooltips(CommandSender sender, String commandLine) {
		CommandDispatcher dispatcher = Brigadier.getCommandDispatcher();
		Object css = Brigadier.getBrigadierSourceFromCommandSender(sender);
		ParseResults parseResults = dispatcher.parse(commandLine, css);
		Suggestions suggestions = null;
		try {
			suggestions = (Suggestions) dispatcher.getCompletionSuggestions(parseResults).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			suggestions = new Suggestions(StringRange.at(0), new ArrayList<>()); // Empty suggestions
		}

		List<Suggestion> suggestionsList = new ArrayList<>();
		for (Suggestion suggestion : suggestions.getList()) {
			suggestionsList.add(suggestion);
		}

		return suggestionsList;
	}

//	@Override
	public boolean shouldSendChatPreviews() {
		return true;
	}
	
	// Registries

//	@Override
	public <T extends Keyed> @Nullable Registry<T> getRegistry(@NotNull Class<T> tClass) {
		return MockPlatform.getInstance().getRegistry(tClass);
//		if (tClass.equals(Enchantment.class)) {
//			return new Registry() {
//				@Nullable
//				public T get(@NotNull NamespacedKey var1) {
//					System.out.println("Accessing " + tClass + ":" + var1);
//					return null;
//				}
//				
//				@NotNull
//				public Stream<T> stream() {
//					List<T> list = List.of();
//					return list.stream();
//				}
//
//				public Iterator<T> iterator() {
//					List<T> list = List.of();
//					return list.iterator();
//				}
//			};
//		} else {
//			return null;
//		}
	}
	
	// Loot tables

	@Override
	public LootTable getLootTable(@NotNull NamespacedKey key) {
		// MockBukkit doesn't implement this, but Paper's LootTableArgument uses it to look up the loot table
		LootTable lootTable = Mockito.mock(LootTable.class);
		Mockito.when(lootTable.getKey()).thenReturn(key);
		return lootTable;
	}

	// World mocking

	static class CustomWorldMock extends WorldMock {
		
		@Override
		public boolean equals(Object obj) {
			if(obj == null) {
				return false;
			} else if(obj instanceof World target) {
				return this.getUID().equals(target.getUID());
			} else {
				return false; // I have no idea what this is
			}
		}

	}
	
	@Override
	public WorldMock addSimpleWorld(String name) {
		WorldMock world = new CustomWorldMock();
		world.setName(name);
		super.addWorld(world);
		return world;
	}
	
	// TODO: Commenting this out for now because we've not sorted out 1.20.2
	// support with MockBukkit, this almost certainly will require all sorts of
	// hoops to jump through because apparently MockBukkit may have changed the
	// method signature for this and we REALLY REALLY don't want to deal with
	// that right now
//	@Override
//	public ItemFactory getItemFactory() {
//		// Thanks MockBukkit, but we REALLY need to access
//		// the raw CraftItemMeta objects for the ItemStackArgument <3
//		return MockPlatform.getInstance().getItemFactory();
//	}

	// 1.16 and 1.17 MockServers do not implement this method, but other versions do
	//  Easiest to just always override this method
	//  This is copied from MockBukkit-v1.18
	private final StandardMessenger messenger = new StandardMessenger();
	@Override
	public @NotNull Messenger getMessenger() {
		return messenger;
	}

	/**
	 * Creates a new Bukkit {@link Player}. Unlike {@link PlayerMock}, this uses Mockito to mock the CraftPlayer class,
	 * which allows the returned object to pass through VanillaCommandWrapper#getListener without error.
	 *
	 * @return A new {@link Player}.
	 */
	public Player setupMockedCraftPlayer() {
		return setupMockedCraftPlayer("defaultName");
	}

	/**
	 * Creates a new Bukkit {@link Player}. Unlike {@link PlayerMock}, this uses Mockito to mock the CraftPlayer class,
	 * which allows the returned object to pass through VanillaCommandWrapper#getListener without error.
	 *
	 * @param name The name for the player
	 * @return A new {@link Player}.
	 */
	public Player setupMockedCraftPlayer(String name) {
		return MockPlatform.getInstance().setupMockedCraftPlayer(name);
	}

	// Advancements
	
	List<Advancement> advancements = new ArrayList<>();
	
	public void addAdvancement(NamespacedKey key) {
		advancements.add(MockPlatform.getInstance().addAdvancement(key));
	}
	
	public void addAdvancements(Collection<NamespacedKey> key) {
		key.forEach(this::addAdvancement);
	}
	
	@Override
	public Iterator<Advancement> advancementIterator() {
		return advancements.iterator();
	}
}
