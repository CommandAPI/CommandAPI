package dev.jorel.commandapi.test.commandnodes;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class UnknownCommandNode extends CommandNode<Object> {
	protected UnknownCommandNode() {
		super(null, source -> true, null, null, false);
	}

	@Override
	protected boolean isValidInput(String input) {
		return false;
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public String getUsageText() {
		return "";
	}

	@Override
	public void parse(StringReader reader, CommandContextBuilder<Object> contextBuilder) throws CommandSyntaxException {

	}

	@Override
	public CompletableFuture<Suggestions> listSuggestions(CommandContext<Object> context, SuggestionsBuilder builder) throws CommandSyntaxException {
		return null;
	}

	@Override
	public ArgumentBuilder<Object, ?> createBuilder() {
		return null;
	}

	@Override
	protected String getSortedKey() {
		return "";
	}

	@Override
	public Collection<String> getExamples() {
		return List.of();
	}
}
