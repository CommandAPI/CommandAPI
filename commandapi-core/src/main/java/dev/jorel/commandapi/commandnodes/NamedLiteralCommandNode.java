package dev.jorel.commandapi.commandnodes;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.jorel.commandapi.arguments.Literal;
import dev.jorel.commandapi.arguments.MultiLiteral;

import java.util.function.Predicate;

/**
 * A special type of {@link LiteralCommandNode} for listed {@link Literal}s and {@link MultiLiteral}s. Compared to
 * the {@link LiteralCommandNode}, this class also has a {@code nodeName}. When this node is parsed, it will add its
 * literal value as an argument in the CommandContext, like a required argument.
 *
 * @param <Source> The Brigadier Source object for running commands.
 */
public class NamedLiteralCommandNode<Source> extends LiteralCommandNode<Source> {
	// Serialization logic
	static {
		NodeTypeSerializer.registerSerializer(NamedLiteralCommandNode.class, (target, type) -> {
			target.addProperty("type", "namedLiteral");
			target.addProperty("nodeName", type.nodeName);
		});
	}

	private final String nodeName;

	public NamedLiteralCommandNode(String nodeName, String literal, Command<Source> command, Predicate<Source> requirement, CommandNode<Source> redirect, RedirectModifier<Source> modifier, boolean forks) {
		super(literal, command, requirement, redirect, modifier, forks);
		this.nodeName = nodeName;
	}

	/**
	 * @return the node name this command represents.
	 */
	public String getNodeName() {
		return nodeName;
	}

	// A NamedLiteralCommandNode is mostly identical to a LiteralCommandNode
	//  The only difference is that when a NamedLiteral is parsed, it adds its literal as an argument based on the nodeName
	@Override
	public void parse(StringReader reader, CommandContextBuilder<Source> contextBuilder) throws CommandSyntaxException {
		int start = reader.getCursor();
		super.parse(reader, contextBuilder);

		contextBuilder.withArgument(getNodeName(), new ParsedArgument<>(start, reader.getCursor(), getLiteral()));
	}

	// Typical LiteralCommandNode methods, adding in the nodeName parameter
	//  Mostly copied and inspired by the implementations for these methods in LiteralCommandNode
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof NamedLiteralCommandNode<?> other)) return false;

		if (!nodeName.equals(other.nodeName)) return false;
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		int result = nodeName.hashCode();
		result = 31 * result + super.hashCode();
		return result;
	}

	@Override
	public NamedLiteralArgumentBuilder<Source> createBuilder() {
		NamedLiteralArgumentBuilder<Source> builder = NamedLiteralArgumentBuilder.namedLiteral(this.nodeName, getLiteral());

		builder.requires(getRequirement());
		builder.forward(getRedirect(), getRedirectModifier(), isFork());
		if (getCommand() != null) {
			builder.executes(getCommand());
		}
		return builder;
	}

	@Override
	public String toString() {
		return "<namedliteral " + nodeName + ":" + getLiteral() + ">";
	}
}
