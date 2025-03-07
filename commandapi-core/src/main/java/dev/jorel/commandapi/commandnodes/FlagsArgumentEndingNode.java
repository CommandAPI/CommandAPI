package dev.jorel.commandapi.commandnodes;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.jorel.commandapi.arguments.AbstractArgument;
import dev.jorel.commandapi.arguments.FlagsArgumentCommon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public interface FlagsArgumentEndingNode<Argument
/// @cond DOX
extends AbstractArgument<?, ?, Argument, CommandSender>
/// @endcond
, CommandSender, Source> extends DifferentClientNode<Source> {
	// Set information
	static <Argument extends AbstractArgument<?, ?, Argument, CommandSender>, CommandSender, Source>
	CommandNode<Source> wrapNode(
		CommandNode<Source> node, String flagsArgumentName, List<Argument> previousArguments
	) {
		if (node instanceof LiteralCommandNode<Source> literalNode) {
			return new LiteralNode<>(literalNode, flagsArgumentName, previousArguments);
		} else if (node instanceof ArgumentCommandNode<Source, ?> argumentNode) {
			return new ArgumentNode<>(argumentNode, flagsArgumentName, previousArguments);
		} else {
			throw new IllegalArgumentException("Node must be an argument or literal. Given " + node + " with type " + node.getClass().getName());
		}
	}

	// Getters
	CommandNode<Source> getWrappedNode();

	String getFlagsArgumentName();

	List<Argument> getPreviousArguments();

	// Use information
	default void parse(StringReader reader, CommandContextBuilder<Source> contextBuilder) throws CommandSyntaxException {
		// Parse the original node
		getWrappedNode().parse(reader, contextBuilder);

		// Correctly set parsed node value
		List<ParsedCommandNode<Source>> nodes = contextBuilder.getNodes();
		int lastNodeIndex = nodes.size() - 1;
		ParsedCommandNode<Source> currentNode = nodes.get(lastNodeIndex);
		nodes.set(lastNodeIndex, new ParsedCommandNode<>((CommandNode<Source>) this, currentNode.getRange()));

		// Extract previous flags
		String name = getFlagsArgumentName();
		ParsedArgument<Source, List<FlagsArgumentCommon.ParseInformation<Argument, CommandSender, Source>>> currentValue =
			(ParsedArgument<Source, List<FlagsArgumentCommon.ParseInformation<Argument, CommandSender, Source>>>)
				contextBuilder.getArguments().get(name);
		List<FlagsArgumentCommon.ParseInformation<Argument, CommandSender, Source>> currentInformation = currentValue.getResult();

		// Add new branch
		//  We do need to copy the information list and the contextBuilder
		//  Otherwise, parsing the argument result references itself, and you get a stack overflow
		List<FlagsArgumentCommon.ParseInformation<Argument, CommandSender, Source>> newInformation = new ArrayList<>(currentInformation);
		newInformation.add(new FlagsArgumentCommon.ParseInformation<>(
			contextBuilder.copy().build(reader.getRead()), getPreviousArguments()
		));

		// Submit new flags
		ParsedArgument<Source, List<FlagsArgumentCommon.ParseInformation<Argument, CommandSender, Source>>> newValue =
			new ParsedArgument<>(currentValue.getRange().getStart(), reader.getCursor(), newInformation);
		contextBuilder.withArgument(name, newValue);
	}

	@Override
	default List<CommandNode<Source>> rewriteNodeForClient(CommandNode<Source> node, Source client, boolean onRegister) {
		// It's important to do this rewrite when the node is registered, because
		//  command send packet tree copying code tends to throw a StackOverflow if children loop,
		//  and the events we hook into to rewrite for a specific client aren't called
		//  until after the tree is copied.

		// Clone wrapped node to remove children
		ArgumentBuilder<Source, ?> result = getWrappedNode().createBuilder();

		if (result.getRedirect() == null) {
			// If this is a terminating node, we just have to unwrap the
			//  DifferentClient node so it doesn't get unwrapped again
			for (CommandNode<Source> child : node.getChildren()) {
				result.then(child);
			}
			return List.of(result.build());
		}
		// Fix the redirect in the looped node case.
		// Note that on Velocity, this method will be called after the tree is copied, so we
		//  need to make sure our redirect properly references the copied flag root,
		//  which is why we hid a reference to it within the tree that got copied.
		CommandNode<Source> hiddenNode = node.getChild(FlagsArgumentRootNode.HIDDEN_NAME);
		CommandNode<Source> redirect = hiddenNode.getChildren().iterator().next();

		result.redirect(redirect);
		return List.of(result.build());
	}

	default Collection<? extends CommandNode<Source>> filterRelevantNodes(Collection<? extends CommandNode<Source>> nodes) {
		// Terminal node is fine
		if (getWrappedNode().getRedirect() == null) return nodes;

		// Loop node needs the hidden root reference removed to not mess up parsing
		List<? extends CommandNode<Source>> filteredNodes = new ArrayList<>(nodes);
		for (int i = 0; i < filteredNodes.size(); i++) {
			if (filteredNodes.get(i).getName().equals(FlagsArgumentRootNode.HIDDEN_NAME)) {
				filteredNodes.remove(i);
				break;
			}
		}
		return filteredNodes;
	}

	// Specific implementations for literal and argument Brigadier nodes
	class LiteralNode<Argument
	/// @cond DOX
	extends AbstractArgument<?, ?, Argument, CommandSender>
	/// @endcond
	, CommandSender, Source>
		extends DifferentClientNode.Literal<Source> implements FlagsArgumentEndingNode<Argument, CommandSender, Source> {
		// Serialization logic
		static {
			NodeTypeSerializer.registerSerializer(LiteralNode.class, (target, type) -> {
				target.addProperty("type", "flagArgumentEndingLiteral");
				target.addProperty("flagsArgumentName", type.flagsArgumentName);

				LiteralCommandNode<?> wrappedNode = type.literalNode;

				JsonObject subProperties = new JsonObject();
				subProperties.addProperty("name", wrappedNode.getName());
				NodeTypeSerializer.addTypeInformation(subProperties, wrappedNode);
				target.add("wrappedNode", subProperties);
			});
		}

		// Set information
		private final LiteralCommandNode<Source> literalNode;
		private final String flagsArgumentName;
		private final List<Argument> previousArguments;

		public LiteralNode(
			LiteralCommandNode<Source> literalNode, String flagsArgumentName, List<Argument> previousArguments
		) {
			super(
				literalNode.getName(), literalNode.getCommand(), literalNode.getRequirement(),
				// This node should not redirect because that causes the branch arguments to disappear
				//  https://github.com/Mojang/brigadier/issues/137
				null, null, false
			);

			this.literalNode = literalNode;
			this.flagsArgumentName = flagsArgumentName;
			this.previousArguments = previousArguments;
		}

		@Override
		public void addChild(CommandNode<Source> node) {
			super.addChild(node);

			if (literalNode.getRedirect() == null) {
				// If we are a terminal node (not redirecting back to root), then
				//  the wrapped node needs our children
				literalNode.addChild(node);
			}
		}

		@Override
		public Collection<? extends CommandNode<Source>> getRelevantNodes(StringReader input) {
			return filterRelevantNodes(super.getRelevantNodes(input));
		}

		// Getters
		@Override
		public CommandNode<Source> getWrappedNode() {
			return literalNode;
		}

		@Override
		public String getFlagsArgumentName() {
			return flagsArgumentName;
		}

		@Override
		public List<Argument> getPreviousArguments() {
			return previousArguments;
		}

		// Use information
		@Override
		public void parse(StringReader reader, CommandContextBuilder<Source> contextBuilder) throws CommandSyntaxException {
			FlagsArgumentEndingNode.super.parse(reader, contextBuilder);
		}

		// We don't reference super for equals and hashCode since those methods reference
		//  the children of this class, which if this node is part of a loop leads to a StackOverflowException
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof LiteralNode<?, ?, ?> other)) return false;
			return Objects.equals(this.literalNode, other.literalNode)
				&& Objects.equals(this.flagsArgumentName, other.flagsArgumentName)
				&& Objects.equals(this.previousArguments, other.previousArguments);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.literalNode, this.flagsArgumentName, this.previousArguments);
		}

		@Override
		public String toString() {
			return "<flagargend " + literalNode.toString() + ">";
		}
	}

	class ArgumentNode<Argument
	/// @cond DOX
	extends AbstractArgument<?, ?, Argument, CommandSender>
	/// @endcond
	, CommandSender, Source, T>
		extends DifferentClientNode.Argument<Source, T> implements FlagsArgumentEndingNode<Argument, CommandSender, Source> {
		// Serialization logic
		static {
			NodeTypeSerializer.registerSerializer(ArgumentNode.class, (target, type) -> {
				target.addProperty("type", "flagArgumentEndingArgument");
				target.addProperty("flagsArgumentName", type.flagsArgumentName);

				ArgumentCommandNode<?, ?> wrappedNode = type.argumentNode;

				JsonObject subProperties = new JsonObject();
				subProperties.addProperty("name", wrappedNode.getName());
				NodeTypeSerializer.addTypeInformation(subProperties, wrappedNode);
				target.add("wrappedNode", subProperties);
			});
		}

		// Set information
		private final ArgumentCommandNode<Source, T> argumentNode;
		private final String flagsArgumentName;
		private final List<Argument> previousArguments;

		public ArgumentNode(
			ArgumentCommandNode<Source, T> argumentNode, String flagsArgumentName, List<Argument> previousArguments
		) {
			super(
				argumentNode.getName(), argumentNode.getType(),
				argumentNode.getCommand(), argumentNode.getRequirement(),
				// This node should not redirect because that causes the branch arguments to disappear
				//  https://github.com/Mojang/brigadier/issues/137
				null, null, false
			);

			this.argumentNode = argumentNode;
			this.flagsArgumentName = flagsArgumentName;
			this.previousArguments = previousArguments;
		}

		@Override
		public void addChild(CommandNode<Source> node) {
			super.addChild(node);

			if (argumentNode.getRedirect() == null) {
				// If we are a terminal node (not redirecting back to root), then
				//  the wrapped node needs our children
				argumentNode.addChild(node);
			}
		}

		@Override
		public Collection<? extends CommandNode<Source>> getRelevantNodes(StringReader input) {
			return filterRelevantNodes(super.getRelevantNodes(input));
		}

		// Getters
		@Override
		public CommandNode<Source> getWrappedNode() {
			return argumentNode;
		}

		@Override
		public String getFlagsArgumentName() {
			return flagsArgumentName;
		}

		@Override
		public List<Argument> getPreviousArguments() {
			return previousArguments;
		}

		// Use information
		@Override
		public void parse(StringReader reader, CommandContextBuilder<Source> contextBuilder) throws CommandSyntaxException {
			FlagsArgumentEndingNode.super.parse(reader, contextBuilder);
		}

		@Override
		public CompletableFuture<Suggestions> listSuggestions(CommandContext<Source> context, SuggestionsBuilder builder) throws CommandSyntaxException {
			return argumentNode.listSuggestions(context, builder);
		}

		// We don't reference super for equals and hashCode since those methods reference
		//  the children of this class, which if this node is part of a loop leads to a StackOverflowException
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof ArgumentNode<?, ?, ?, ?> other)) return false;
			return Objects.equals(this.argumentNode, other.argumentNode)
				&& Objects.equals(this.flagsArgumentName, other.flagsArgumentName)
				&& Objects.equals(this.previousArguments, other.previousArguments);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.argumentNode, this.flagsArgumentName, this.previousArguments);
		}

		@Override
		public String toString() {
			return "<flagargend " + argumentNode.toString() + ">";
		}
	}
}
