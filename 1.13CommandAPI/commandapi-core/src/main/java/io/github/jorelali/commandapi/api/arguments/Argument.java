package io.github.jorelali.commandapi.api.arguments;

import com.mojang.brigadier.arguments.ArgumentType;

import io.github.jorelali.commandapi.api.CommandPermission;

public abstract class Argument implements OverrideableSuggestions {
		
	
	public abstract <V> Class<V> getPrimitiveType();
	public abstract CommandAPIArgumentType getArgumentType();
	
	////////////////////////
	// Raw Argument Types //
	////////////////////////
	
	ArgumentType<?> rawType;
	
	@SuppressWarnings("unchecked")
	public <T> ArgumentType<T> getRawType() {
		return (ArgumentType<T>) rawType;
	}
	
	/////////////////
	// Suggestions //
	/////////////////
	
	private DynamicSuggestions suggestions;
	
	public Argument overrideSuggestions(String... suggestions) {
		this.suggestions = mkSuggestions(suggestions);
		return this;
	}
	
	public Argument overrideSuggestions(DynamicSuggestions suggestions) {
		this.suggestions = suggestions;
		return this;
	}
	
	public DynamicSuggestions getOverriddenSuggestions() {
		return suggestions;
	}
	
	/////////////////
	// Permissions //
	/////////////////
	
	private CommandPermission permission = null;
	
	public Argument withPermission(CommandPermission permission) {
		this.permission = permission;
		return this;
	}

	public CommandPermission getArgumentPermission() {
		return permission;
	}
		
}