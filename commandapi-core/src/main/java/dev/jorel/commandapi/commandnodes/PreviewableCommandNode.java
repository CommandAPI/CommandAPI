package dev.jorel.commandapi.commandnodes;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import dev.jorel.commandapi.CommandAPIHandler;
import dev.jorel.commandapi.arguments.Previewable;
import dev.jorel.commandapi.wrappers.PreviewableFunction;

import java.util.Objects;
import java.util.Optional;

/**
 * A special type of {@link ArgumentCommandNode} for {@link Previewable} arguments. Compared to the
 * {@link ArgumentCommandNode}, this class also has the methods {@link #getPreview()} and {@link #isLegacy()},
 * which are used when players try to use the chat preview feature.
 *
 * @param <Source> The Brigadier Source object for running commands.
 * @param <T> The type returned when this argument is parsed.
 */
public class PreviewableCommandNode<Source, T> extends ArgumentCommandNode<Source, T> {
	// Serialization logic
	static {
		NodeTypeSerializer.registerSerializer(PreviewableCommandNode.class, (target, type) -> {
			ArgumentType<?> argumentType = type.getType();

			target.addProperty("type", "previewableArgument");
			target.addProperty("hasPreview", type.preview != null);
			target.addProperty("legacy", type.legacy);
			target.addProperty("listed", type.isListed);

			target.addProperty("argumentType", argumentType.getClass().getName());

			CommandAPIHandler.getInstance().getPlatform()
				.getArgumentTypeProperties(argumentType).ifPresent(properties -> target.add("properties", properties));
		});
	}

	private final PreviewableFunction<?, ?> preview;
	private final boolean legacy;

	// Instead of having a listed and unlisted copy of this class, we can handle both with this boolean
	private final boolean isListed;

	public PreviewableCommandNode(PreviewableArgumentBuilder<Source, T> builder) {
		super(
			builder.getName(), builder.getType(),
			builder.getCommand(), builder.getRequirement(),
			builder.getRedirect(), builder.getRedirectModifier(), builder.isFork(),
			builder.getSuggestionsProvider()
		);

		this.preview = builder.getPreviewableFunction();
		this.legacy = builder.isLegacy();
		this.isListed = builder.isListed();
	}

	// Methods needed to generate a preview
	public Optional<PreviewableFunction<?, ?>> getPreview() {
		return Optional.ofNullable(preview);
	}

	public boolean isLegacy() {
		return legacy;
	}

	// If we are unlisted, then when parsed, don't add the argument result to the CommandContext
	public boolean isListed() {
		return isListed;
	}

	@Override
	public void parse(StringReader reader, CommandContextBuilder<Source> contextBuilder) throws CommandSyntaxException {
		// Copied from `super#parse`, but with listability added
		int start = reader.getCursor();

		T result = this.getType().parse(reader);
		ParsedArgument<Source, T> parsed = new ParsedArgument<>(start, reader.getCursor(), result);

		if (isListed) contextBuilder.withArgument(this.getName(), parsed);

		contextBuilder.withNode(this, parsed.getRange());
	}

	// Typical ArgumentCommandNode methods, but make it our classes
	//  Mostly copied and inspired by the implementations for these methods in ArgumentCommandNode
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof PreviewableCommandNode<?, ?> other)) return false;

		if (!Objects.equals(this.preview, other.preview)) return false;
		if (this.legacy != other.legacy) return false;
		if (this.isListed != other.isListed) return false;
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(this.preview, this.legacy, this.isListed);
		result = 31 * result + super.hashCode();
		return result;
	}

	// TODO: Um, this currently doesn't work since PreviewableArgumentBuilder does not extend RequiredArgumentBuilder
	//  See PreviewableArgumentBuilder for why
	//  I hope no one tries to use this method!
//	@Override
//	public PreviewableArgumentBuilder<Source, T> createBuilder() {
//		PreviewableArgumentBuilder<Source, T> builder = PreviewableArgumentBuilder.previewableArgument(getName(), getType(), preview, legacy, isListed);
//
//		builder.requires(getRequirement());
//		builder.forward(getRedirect(), getRedirectModifier(), isFork());
//		if (getCommand() != null) {
//			builder.executes(getCommand());
//		}
//		return builder;
//	}
}
