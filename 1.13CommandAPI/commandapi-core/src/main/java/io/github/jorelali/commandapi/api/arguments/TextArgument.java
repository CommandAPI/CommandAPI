package io.github.jorelali.commandapi.api.arguments;

import com.mojang.brigadier.arguments.StringArgumentType;

public class TextArgument extends Argument {

	/**
	 * A string argument for one word, or multiple words encased in quotes
	 */
	public TextArgument() {
		super(StringArgumentType.string());
	}

	@Override
	public Class<?> getPrimitiveType() {
		return String.class;
	}
	
	@Override
	public CommandAPIArgumentType getArgumentType() {
		return CommandAPIArgumentType.SIMPLE_TYPE;
	}
}
