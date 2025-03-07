package dev.jorel.commandapi.commandnodes;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;

/**
 * A special type of {@link RequiredArgumentBuilder} for unlisted Arguments. Compared to the
 * {@link RequiredArgumentBuilder}, this class builds a {@link UnnamedArgumentCommandNode}
 *
 * @param <Source> The Brigadier Source object for running commands.
 * @param <T> The type returned when this argument is parsed.
 */
// We can't actually extend RequiredArgumentBuilder since its only constructor is private :(
// See https://github.com/Mojang/brigadier/pull/144
public class UnnamedRequiredArgumentBuilder<Source, T> extends ArgumentBuilder<Source, UnnamedRequiredArgumentBuilder<Source, T>> {
	// Everything here is copied from RequiredArgumentBuilder, which is why it would be nice to extend that directly
	private final String name;
	private final ArgumentType<T> type;
	private SuggestionProvider<Source> suggestionsProvider = null;

	private UnnamedRequiredArgumentBuilder(final String name, final ArgumentType<T> type) {
		this.name = name;
		this.type = type;
	}

	public static <Source, T> UnnamedRequiredArgumentBuilder<Source, T> unnamedArgument(final String name, final ArgumentType<T> type) {
		return new UnnamedRequiredArgumentBuilder<>(name, type);
	}

	public UnnamedRequiredArgumentBuilder<Source, T> suggests(final SuggestionProvider<Source> provider) {
		this.suggestionsProvider = provider;
		return getThis();
	}

	public SuggestionProvider<Source> getSuggestionsProvider() {
		return suggestionsProvider;
	}

	@Override
	protected UnnamedRequiredArgumentBuilder<Source, T> getThis() {
		return this;
	}

	public ArgumentType<T> getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public UnnamedArgumentCommandNode<Source, T> build() {
		final UnnamedArgumentCommandNode<Source, T> result = new UnnamedArgumentCommandNode<>(getName(), getType(),
			getCommand(), getRequirement(), getRedirect(), getRedirectModifier(), isFork(), getSuggestionsProvider());

		for (final CommandNode<Source> argument : getArguments()) {
			result.addChild(argument);
		}

		return result;
	}
}
