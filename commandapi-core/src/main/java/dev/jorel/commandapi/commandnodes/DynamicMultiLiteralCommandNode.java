package dev.jorel.commandapi.commandnodes;

import com.google.gson.JsonArray;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.jorel.commandapi.CommandAPIHandler;
import dev.jorel.commandapi.arguments.DynamicMultiLiteralArgumentCommon.LiteralsCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class DynamicMultiLiteralCommandNode<CommandSender, Source> extends DifferentClientNode.Argument<Source, String> {
	static {
		NodeTypeSerializer.registerSerializer(DynamicMultiLiteralCommandNode.class, (target, type) -> {
			target.addProperty("type", "dynamicMultiLiteral");
			target.addProperty("isListed", type.isListed);

			LiteralsCreator<?> literalsCreator = type.literalsCreator;
			JsonArray literals = new JsonArray();
			literalsCreator.createLiterals(null).forEach(literals::add);
			target.add("defaultLiterals", literals);
		});
	}

	private final boolean isListed;
	private final LiteralsCreator<CommandSender> literalsCreator;

	public DynamicMultiLiteralCommandNode(DynamicMultiLiteralArgumentBuilder<CommandSender, Source> builder) {
		// This mostly acts like a StringArgument
		super(
			builder.getName(), StringArgumentType.word(),
			builder.getCommand(), builder.getRequirement(),
			builder.getRedirect(), builder.getRedirectModifier(), builder.isFork()
		);

		this.isListed = builder.isListed();
		this.literalsCreator = builder.getLiteralsCreator();
	}

	// Getters
	public boolean isListed() {
		return isListed;
	}

	public LiteralsCreator<CommandSender> getLiteralsCreator() {
		return literalsCreator;
	}

	// On the client, this node looks like a bunch of literal nodes
	@Override
	public List<CommandNode<Source>> rewriteNodeForClient(CommandNode<Source> node, Source client, boolean onRegister) {
		// We only want to rewrite when actually being sent to a client
		if (onRegister) {
			// However, we do need to ensure the node currently in the tree is a DynamicMultiLiteralCommandNode,
			//  and not a copied argument, so we can be properly parsed server-side
			if (node instanceof DynamicMultiLiteralCommandNode<?, ?>) return null; // No rewrite

			CommandNode<Source> result = DynamicMultiLiteralArgumentBuilder
				.<CommandSender, Source>dynamicMultiLiteral(this.getName(), this.isListed, this.literalsCreator)
				.executes(node.getCommand())
				.requires(node.getRequirement())
				.forward(node.getRedirect(), node.getRedirectModifier(), node.isFork())
				.build();

			for (CommandNode<Source> child : node.getChildren()) {
				result.addChild(child);
			}

			return List.of(result);
		}

		if (client == null) {
			// Triggers on Spigot, we will send this node directly to a client (onRegister == false), but we don't know
			//  the client. Best we can do in this case is be a StringArgument with server-side suggestions. This means
			//  invalid literals will show up as correct on the client, but that's less confusing than correct literals
			//  possibly showing up as invalid or the suggestions being incomplete. Note that doing this only affects
			//  the suggestions, as server-side the command will be parsed as intended.
			CommandNode<Source> result = RequiredArgumentBuilder
				.<Source, String>argument(node.getName(), StringArgumentType.word())
				.executes(node.getCommand())
				.requires(node.getRequirement())
				.forward(node.getRedirect(), node.getRedirectModifier(), node.isFork())
				.suggests(this::listSuggestions)
				.build();

			for (CommandNode<Source> child : node.getChildren()) {
				result.addChild(child);
			}

			return List.of(result);
		}

		CommandAPIHandler<?, CommandSender, Source> commandAPIHandler = CommandAPIHandler.getInstance();
		CommandSender sender = commandAPIHandler.getPlatform().getCommandSenderFromCommandSource(client);
		List<String> literals = literalsCreator.createLiterals(sender);

		if (literals.isEmpty()) return List.of();

		List<CommandNode<Source>> clientNodes = new ArrayList<>(literals.size());
		for (String literal : literals) {
			LiteralCommandNode<Source> clientNode = new LiteralCommandNode<>(
				literal,
				node.getCommand(), node.getRequirement(),
				node.getRedirect(), node.getRedirectModifier(), node.isFork()
			);
			for (CommandNode<Source> child : node.getChildren()) {
				clientNode.addChild(child);
			}
			clientNodes.add(clientNode);
		}

		return clientNodes;
	}

	// Implement CommandNode methods
	@Override
	public void parse(StringReader reader, CommandContextBuilder<Source> contextBuilder) throws CommandSyntaxException {
		// Read the input
		int start = reader.getCursor();
		String literal = reader.readUnquotedString();

		// Validate input
		CommandAPIHandler<?, CommandSender, Source> commandAPIHandler = CommandAPIHandler.getInstance();
		CommandSender client = commandAPIHandler.getPlatform().getCommandSenderFromCommandSource(contextBuilder.getSource());
		List<String> literals = literalsCreator.createLiterals(client);
		if (!literals.contains(literal)) {
			reader.setCursor(start);
			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().createWithContext(reader, literals);
		}

		// Add node to list
		ParsedArgument<Source, String> parsed = new ParsedArgument<>(start, reader.getCursor(), literal);
		if (isListed) contextBuilder.withArgument(this.getName(), parsed);
		contextBuilder.withNode(this, parsed.getRange());
	}

	@Override
	public CompletableFuture<Suggestions> listSuggestions(CommandContext<Source> context, SuggestionsBuilder builder) throws CommandSyntaxException {
		CommandAPIHandler<?, CommandSender, Source> commandAPIHandler = CommandAPIHandler.getInstance();
		CommandSender client = commandAPIHandler.getPlatform().getCommandSenderFromCommandSource(context.getSource());
		List<String> literals = literalsCreator.createLiterals(client);

		String remaining = builder.getRemaining().toLowerCase();
		for (String literal : literals) {
			if (literal.startsWith(remaining)) {
				builder.suggest(literal);
			}
		}
		return builder.buildFuture();
	}

	@Override
	public boolean isValidInput(String input) {
		// For ambiguity checking purposes, this node could accept any input
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof DynamicMultiLiteralCommandNode<?, ?> other)) return false;

		if (this.isListed != other.isListed) return false;
		if (!Objects.equals(this.literalsCreator, other.literalsCreator)) return false;
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(this.isListed, this.literalsCreator);
		result = 31 * result + super.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "<dynamicMultiLiteral " + getName() + ">";
	}
}
