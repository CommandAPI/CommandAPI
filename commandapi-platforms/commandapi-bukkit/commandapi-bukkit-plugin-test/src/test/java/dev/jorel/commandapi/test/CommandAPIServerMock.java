package dev.jorel.commandapi.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;

import be.seeseemelk.mockbukkit.AsyncCatcher;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import dev.jorel.commandapi.Brigadier;

public class CommandAPIServerMock extends ServerMock {

	@SuppressWarnings("unchecked")
	public boolean dispatchThrowableCommand(CommandSender sender, String commandLine) throws CommandSyntaxException {
		String[] commands = commandLine.split(" ");
		String commandLabel = commands[0];
		Command command = getCommandMap().getCommand(commandLabel);

		if (command != null) {
			return super.dispatchCommand(sender, commandLine);
		} else {
			AsyncCatcher.catchOp("command dispatch");
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
		} catch (CommandSyntaxException e1) {
			return false;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<String> getSuggestions(CommandSender sender, String commandLine) {
		AsyncCatcher.catchOp("command tabcomplete");
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

	@Override
	public boolean shouldSendChatPreviews() {
		return true;
	}

	@Override
	public <T extends Keyed> @Nullable Registry<T> getRegistry(@NotNull Class<T> tClass) {
		return null;
	}
	
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
		AsyncCatcher.catchOp("world creation");
		WorldMock world = new CustomWorldMock();
		world.setName(name);
		super.addWorld(world);
		return world;
	}
	
	@Override
	public ItemFactory getItemFactory() {
		// Thanks MockBukkit, but we REALLY need to access
		// the raw CraftItemMeta objects for the ItemStackArgument <3
		return MockNMS.getItemFactory();
	}
}
