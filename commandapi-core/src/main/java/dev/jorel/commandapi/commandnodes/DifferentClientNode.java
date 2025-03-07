package dev.jorel.commandapi.commandnodes;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.jorel.commandapi.CommandAPIHandler;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface DifferentClientNode<Source> {
	// Rewrite node trees for the client
	static <Source> void rewriteAllChildren(Source client, CommandNode<Source> parent, boolean onRegister) {
		// Copy the children, as we do expect them to be modified
		Queue<CommandNode<Source>> childrenToProcess = new LinkedList<>(parent.getChildren());
		CommandNode<Source> child;
		while ((child = childrenToProcess.poll()) != null) {
			rewriteChild(client, parent, child, onRegister, childrenToProcess);
		}
	}

	private static <Source> void rewriteChild(Source client, CommandNode<Source> parent, CommandNode<Source> child, boolean onRegister, Queue<CommandNode<Source>> parentChildrenToProcess) {
		// Get the node creator
		DifferentClientNode<Source> clientNodeCreator = null;
		if (child instanceof DifferentClientNode<?>) {
			// child is directly a DifferentClientNode
			clientNodeCreator = (DifferentClientNode<Source>) child;
		} else if (child instanceof ArgumentCommandNode<Source, ?> argument && argument.getType() instanceof Argument.Type<?, ?>) {
			// child was copied from a DifferentClientNode using `createBuilder` (real node hidden in ArgumentType)
			Argument.Type<Source, ?> type = (Argument.Type<Source, ?>) argument.getType();
			clientNodeCreator = type.node;
		}

		if (clientNodeCreator != null) {
			// Get the new client nodes
			List<CommandNode<Source>> clientNodes = clientNodeCreator.rewriteNodeForClient(child, client, onRegister);

			if (clientNodes != null) {
				// Inject client node
				Map<String, CommandNode<Source>> children = CommandAPIHandler.getCommandNodeChildren(parent);
				children.remove(child.getName());
				for (CommandNode<Source> clientNode : clientNodes) {
					children.put(clientNode.getName(), clientNode);
				}

				// Submit the new client nodes for further processing
				//  in case they are a client node wrapping a client node.
				//  This also ensures we rewrite the children of the client
				//  nodes, rather than the children of the original node.
				parentChildrenToProcess.addAll(clientNodes);
				return;
			}
		}

		// Modify all children
		rewriteAllChildren(client, child, onRegister);
	}

	// Create the client-side node
	/**
	 * Transforms this node into one the client should see.
	 *
	 * @param node       The {@link CommandNode} to rewrite.
	 * @param client     The client who the new node is for. If this is null,
	 *                   the generated node should just be for any general client.
	 * @param onRegister {@code true} if the node rewrite is happening when the command is being registered,
	 *                   and {@code false} if the node rewrite is happening when the command is being sent to a client.
	 * @return The version of the given {@link CommandNode} the client should see. This is a list, so one server
	 * node may become many on the client. If the list is empty, the client will not see any nodes here. If the list
	 * is null, then no rewriting will occur.
	 */
	List<CommandNode<Source>> rewriteNodeForClient(CommandNode<Source> node, Source client, boolean onRegister);

	// Specific implementations for literal and argument Brigadier nodes
	abstract class Argument<Source, T> extends ArgumentCommandNode<Source, T> implements DifferentClientNode<Source> {
		// Node information
		private final ArgumentType<T> type;

		protected Argument(
			String name, ArgumentType<T> type,
			Command<Source> command, Predicate<Source> requirement,
			CommandNode<Source> redirect, RedirectModifier<Source> modifier, boolean forks
		) {
			// Platforms often copy this node as a normal ArgumentCommandNode,
			//  but we can hide a reference to ourselves in the ArgumentType used
			//  when `ArgumentCommandNode#createBuilder` is called. It would be nice
			//  to override `createBuilder` to return this class, but that isn't possible.
			//  https://github.com/Mojang/brigadier/pull/144 :(
			super(name, new Type<>(), command, requirement, redirect, modifier, forks, null);

			((Type<Source, T>) super.getType()).node = this;

			// This type actually represents this argument when serializing nodes to json
			//  and also helps this argument blend in with ambiguity checks
			this.type = type;
		}

		// Handle type nonsense
		private static class Type<Source, T> implements ArgumentType<T> {
			private Argument<Source, T> node;

			@Override
			public T parse(StringReader stringReader) {
				throw new IllegalStateException("Not supposed to be called");
			}
		}

		@Override
		public ArgumentType<T> getType() {
			return type;
		}

		@Override
		public boolean isValidInput(String input) {
			try {
				StringReader reader = new StringReader(input);
				this.type.parse(reader);
				return !reader.canRead() || reader.peek() == ' ';
			} catch (CommandSyntaxException var3) {
				return false;
			}
		}

		@Override
		public Collection<String> getExamples() {
			return this.type.getExamples();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof Argument<?, ?> other)) return false;

			return this.type.equals(other.type) && super.equals(other);
		}

		@Override
		public int hashCode() {
			int result = this.type.hashCode();
			result = 31 * result + super.hashCode();
			return result;
		}

		// Require inheritors to redefine these methods from ArgumentCommandNode
		@Override
		public abstract void parse(StringReader reader, CommandContextBuilder<Source> contextBuilder) throws CommandSyntaxException;

		@Override
		public abstract CompletableFuture<Suggestions> listSuggestions(CommandContext<Source> context, SuggestionsBuilder builder) throws CommandSyntaxException;

		@Override
		public abstract String toString();
	}

	abstract class Literal<Source> extends LiteralCommandNode<Source> implements DifferentClientNode<Source> {
		protected Literal(
			String literal,
			Command<Source> command, Predicate<Source> requirement,
			CommandNode<Source> redirect, RedirectModifier<Source> modifier, boolean forks
		) {
			super(literal, command, requirement, redirect, modifier, forks);
		}

		// We can actually override `createBuilder` for literal nodes since `LiteralArgumentBuilder` can be extended
		//  Our builder also includes the client node creator
		@Override
		public LiteralArgumentBuilder<Source> createBuilder() {
			return new Builder<>(this);
		}

		protected static class Builder<Source> extends LiteralArgumentBuilder<Source> {
			protected final DifferentClientNode<Source> clientNodeCreator;

			protected Builder(Literal<Source> node) {
				super(node.getLiteral());
				executes(node.getCommand());
				requires(node.getRequirement());
				forward(node.getRedirect(), node.getRedirectModifier(), node.isFork());

				this.clientNodeCreator = node;
			}

			@Override
			public Literal<Source> build() {
				Literal<Source> result = new Literal<>(
					this.getLiteral(),
					this.getCommand(), this.getRequirement(),
					this.getRedirect(), this.getRedirectModifier(), this.isFork()
				) {
					@Override
					public List<CommandNode<Source>> rewriteNodeForClient(CommandNode<Source> node, Source client, boolean onRegister) {
						return clientNodeCreator.rewriteNodeForClient(node, client, onRegister);
					}
				};

				for (CommandNode<Source> argument : this.getArguments()) {
					result.addChild(argument);
				}

				return result;
			}
		}
	}
}
