package dev.jorel.commandapi.commandnodes;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class FlagsArgumentRootNode<Source> extends LiteralCommandNode<Source> {
	// Serialization logic
	static {
		NodeTypeSerializer.registerSerializer(FlagsArgumentRootNode.class, (target, type) ->
			target.addProperty("type", "flagsArgumentRootNode")
		);
	}

	// Ending nodes that loop back should always share the children of this node
	private final Set<CommandNode<Source>> loopEndNodes = new HashSet<>();

	// Looping nodes also need a hidden reference to this node to fix their redirect when rewriting for the client
	public static final String HIDDEN_NAME = "commandapi:hiddenRootNode";
	private final HiddenRedirect<Source> hiddenRedirect;

	public static final class HiddenRedirect<Source> extends ArgumentCommandNode<Source, String> {
		// Serialization logic
		static {
			NodeTypeSerializer.registerSerializer(HiddenRedirect.class, (target, type) ->
				target.addProperty("type", "flagArgumentHiddenRedirect")
			);
		}

		public HiddenRedirect(CommandNode<Source> redirect) {
			super(
				HIDDEN_NAME, StringArgumentType.word(),
				null, source -> true,
				null, null, false,
				null
			);
			this.addChild(redirect);
		}

		// Don't interfere with suggestions
		@Override
		public CompletableFuture<Suggestions> listSuggestions(CommandContext<Source> context, SuggestionsBuilder builder) throws CommandSyntaxException {
			return builder.buildFuture();
		}
	}

	public FlagsArgumentRootNode(LiteralCommandNode<Source> literalNode) {
		super(
			literalNode.getName(), literalNode.getCommand(), literalNode.getRequirement(),
			literalNode.getRedirect(), literalNode.getRedirectModifier(), literalNode.isFork()
		);
		this.hiddenRedirect = new HiddenRedirect<>(this);
	}

	// Handle loops back to here
	public void addLoopEndNode(CommandNode<Source> node) {
		node.addChild(hiddenRedirect);
		loopEndNodes.add(node);

		for (CommandNode<Source> child : getChildren()) {
			node.addChild(child);
		}
	}

	@Override
	public void addChild(CommandNode<Source> node) {
		super.addChild(node);

		for (CommandNode<Source> loopEndNode : loopEndNodes) {
			loopEndNode.addChild(node);
		}
	}

	// Set up the list of information output by this argument
	@Override
	public void parse(StringReader reader, CommandContextBuilder<Source> contextBuilder) throws CommandSyntaxException {
		int start = reader.getCursor();
		super.parse(reader, contextBuilder);

		contextBuilder.withArgument(getName(), new ParsedArgument<>(start, reader.getCursor(), new ArrayList<>()));
	}

	@Override
	public String toString() {
		return "<flagargstart " + getLiteral() + ">";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof FlagsArgumentRootNode<?> other)) return false;

		if (!Objects.equals(this.hiddenRedirect, other.hiddenRedirect)) return false;
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(this.hiddenRedirect);
		result = 31 * result + super.hashCode();
		return result;
	}
}
