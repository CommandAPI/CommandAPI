package io.github.jorelali.commandapi.api.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import io.github.jorelali.commandapi.api.CommandAPIHandler;
import io.github.jorelali.commandapi.api.CommandPermission;
import org.bukkit.loot.LootTable;

@SuppressWarnings("unchecked")
public class LootTableArgument implements Argument, CustomProvidedArgument {

	ArgumentType<?> rawType;
	
	public LootTableArgument() {
		rawType = CommandAPIHandler.getNMS()._ArgumentMinecraftKeyRegistered();
	}
	
	@Override
	public <T> ArgumentType<T> getRawType() {
		return (ArgumentType<T>) rawType;
	}

	@Override
	public <V> Class<V> getPrimitiveType() {
		return (Class<V>) LootTable.class;
	}

	@Override
	public boolean isSimple() {
		return false;
	}
	
	private CommandPermission permission = CommandPermission.NONE;
	
	@Override
	public LootTableArgument withPermission(CommandPermission permission) {
		this.permission = permission;
		return this;
	}

	@Override
	public CommandPermission getArgumentPermission() {
		return permission;
	}

	@Override
	public SuggestionProviders getSuggestionProvider() {
		return SuggestionProviders.LOOT_TABLES;
	}
	
	@Override
	public CommandAPIArgumentType getArgumentType() {
		return CommandAPIArgumentType.LOOT_TABLE;
	}
}
