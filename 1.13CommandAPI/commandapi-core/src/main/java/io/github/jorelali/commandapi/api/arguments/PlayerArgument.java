package io.github.jorelali.commandapi.api.arguments;

import org.bukkit.entity.Player;

import com.mojang.brigadier.arguments.ArgumentType;

import io.github.jorelali.commandapi.api.CommandAPIHandler;
import io.github.jorelali.commandapi.api.CommandPermission;

@SuppressWarnings("unchecked")
public class PlayerArgument extends Argument {

	ArgumentType<?> rawType;
	
	/**
	 * A Player argument. Produces a single player, regardless of whether @a, @p, @r or @e is used.
	 */
	public PlayerArgument() {
		rawType = CommandAPIHandler.getNMS()._ArgumentProfile();
	}
	
	@Override
	public <T> ArgumentType<T> getRawType() {
		return (ArgumentType<T>) rawType;
	}

	@Override
	public Class<?> getPrimitiveType() {
		return Player.class;
	}

	@Override
	public boolean isSimple() {
		return false;
	}
	
	private String[] suggestions;
	
	@Override
	public PlayerArgument overrideSuggestions(String... suggestions) {
		this.suggestions = suggestions;
		return this;
	}
	
	@Override
	public String[] getOverriddenSuggestions() {
		return suggestions;
	}
	
	private CommandPermission permission = null;
	
	@Override
	public PlayerArgument withPermission(CommandPermission permission) {
		this.permission = permission;
		return this;
	}

	@Override
	public CommandPermission getArgumentPermission() {
		return permission;
	}
	
	@Override
	public CommandAPIArgumentType getArgumentType() {
		return CommandAPIArgumentType.PLAYER;
	}
}
