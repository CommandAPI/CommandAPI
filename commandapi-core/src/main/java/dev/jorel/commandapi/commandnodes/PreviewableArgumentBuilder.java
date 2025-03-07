package dev.jorel.commandapi.commandnodes;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import dev.jorel.commandapi.arguments.Previewable;
import dev.jorel.commandapi.wrappers.PreviewableFunction;

/**
 * A special type of {@link RequiredArgumentBuilder} for {@link Previewable} Arguments. Compared to the
 * {@link RequiredArgumentBuilder}, this class builds a {@link PreviewableCommandNode}
 *
 * @param <Source> The Brigadier Source object for running commands.
 * @param <T> The type returned when this argument is parsed.
 */
// We can't actually extend RequiredArgumentBuilder since its only constructor is private :(
// See https://github.com/Mojang/brigadier/pull/144
public class PreviewableArgumentBuilder<Source, T> extends ArgumentBuilder<Source, PreviewableArgumentBuilder<Source, T>> {
	// Everything here is copied from RequiredArgumentBuilder, which is why it would be nice to extend that directly
	private final String name;
	private final ArgumentType<T> type;
	private SuggestionProvider<Source> suggestionsProvider = null;

	// `Previewable` information
	private final PreviewableFunction<?, ?> previewableFunction;
	private final boolean legacy;
	private final boolean isListed;

	private PreviewableArgumentBuilder(String name, ArgumentType<T> type, PreviewableFunction<?, ?> previewableFunction, boolean legacy, boolean isListed) {
		this.name = name;
		this.type = type;

		this.previewableFunction = previewableFunction;
		this.legacy = legacy;
		this.isListed = isListed;
	}

	public static <Source, T> PreviewableArgumentBuilder<Source, T> previewableArgument(String name, ArgumentType<T> type, PreviewableFunction<?, ?> previewableFunction, boolean legacy, boolean isListed) {
		return new PreviewableArgumentBuilder<>(name, type, previewableFunction, legacy, isListed);
	}

	public PreviewableArgumentBuilder<Source, T> suggests(final SuggestionProvider<Source> provider) {
		this.suggestionsProvider = provider;
		return getThis();
	}

	// Getters
	@Override
	protected PreviewableArgumentBuilder<Source, T> getThis() {
		return this;
	}

	public String getName() {
		return name;
	}

	public ArgumentType<T> getType() {
		return type;
	}

	public SuggestionProvider<Source> getSuggestionsProvider() {
		return suggestionsProvider;
	}

	public PreviewableFunction<?, ?> getPreviewableFunction() {
		return previewableFunction;
	}

	public boolean isLegacy() {
		return legacy;
	}

	public boolean isListed() {
		return isListed;
	}

	// Create node
	public PreviewableCommandNode<Source, T> build() {
		final PreviewableCommandNode<Source, T> result = new PreviewableCommandNode<>(this);

		for (final CommandNode<Source> argument : getArguments()) {
			result.addChild(argument);
		}

		return result;
	}
}
