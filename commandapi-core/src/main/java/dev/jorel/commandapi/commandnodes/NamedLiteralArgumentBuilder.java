package dev.jorel.commandapi.commandnodes;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import dev.jorel.commandapi.arguments.Literal;
import dev.jorel.commandapi.arguments.MultiLiteral;

/**
 * A special type of {@link LiteralArgumentBuilder} for listed {@link Literal}s and {@link MultiLiteral}s. Compared to
 * the {@link LiteralArgumentBuilder}, this class also has a {code nodeName} and builds a {@link NamedLiteralCommandNode}.
 *
 * @param <Source> The Brigadier Source object for running commands.
 */
public class NamedLiteralArgumentBuilder<Source> extends LiteralArgumentBuilder<Source> {
	private final String nodeName;

	/**
	 * Creates a new {@link NamedLiteralCommandNode} with the given nodeName and literal.
	 *
	 * @param nodeName the string that identifies the parsed command node in the CommandContext.
	 * @param literal  the literal that identifies the built command node in the CommandDispatcher
	 */
	protected NamedLiteralArgumentBuilder(String nodeName, String literal) {
		super(literal);
		this.nodeName = nodeName;
	}

	/**
	 * A factory method to create a new builder for a {@link NamedLiteralCommandNode}.
	 *
	 * @param nodeName the string that identifies the parsed command node in the CommandContext.
	 * @param literal  the literal that identifies the built command node in the CommandDispatcher
	 * @param <Source> The Brigadier Source object for running commands.
	 * @return the constructed {@link NamedLiteralArgumentBuilder}.
	 */
	public static <Source> NamedLiteralArgumentBuilder<Source> namedLiteral(String nodeName, String literal) {
		return new NamedLiteralArgumentBuilder<>(nodeName, literal);
	}

	/**
	 * @return the node name that the built command node will be identified by.
	 */
	public String getNodeName() {
		return nodeName;
	}

	@Override
	public NamedLiteralCommandNode<Source> build() {
		NamedLiteralCommandNode<Source> result = new NamedLiteralCommandNode<>(getNodeName(), getLiteral(), getCommand(), getRequirement(), getRedirect(), getRedirectModifier(), isFork());

		for (CommandNode<Source> argument : getArguments()) {
			result.addChild(argument);
		}

		return result;
	}
}
