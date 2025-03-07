package dev.jorel.commandapi.commandnodes;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import dev.jorel.commandapi.arguments.DynamicMultiLiteralArgumentCommon.LiteralsCreator;

public class DynamicMultiLiteralArgumentBuilder<CommandSender, Source> extends ArgumentBuilder<Source, DynamicMultiLiteralArgumentBuilder<CommandSender, Source>> {
	// Build
	private final String name;
	private final boolean isListed;
	private final LiteralsCreator<CommandSender> literalsCreator;

	public DynamicMultiLiteralArgumentBuilder(String name, boolean isListed, LiteralsCreator<CommandSender> literalsCreator) {
		this.name = name;
		this.isListed = isListed;
		this.literalsCreator = literalsCreator;
	}

	public static <CommandSender, Source> DynamicMultiLiteralArgumentBuilder<CommandSender, Source> dynamicMultiLiteral(
		String name, boolean isListed, LiteralsCreator<CommandSender> literalsCreator
	) {
		return new DynamicMultiLiteralArgumentBuilder<>(name, isListed, literalsCreator);
	}

	// Getters
	@Override
	protected DynamicMultiLiteralArgumentBuilder<CommandSender, Source> getThis() {
		return this;
	}

	public String getName() {
		return name;
	}

	public boolean isListed() {
		return isListed;
	}

	public LiteralsCreator<CommandSender> getLiteralsCreator() {
		return literalsCreator;
	}

	// Create node
	@Override
	public DynamicMultiLiteralCommandNode<CommandSender, Source> build() {
		final DynamicMultiLiteralCommandNode<CommandSender, Source> result = new DynamicMultiLiteralCommandNode<>(this);

		for (final CommandNode<Source> argument : getArguments()) {
			result.addChild(argument);
		}

		return result;
	}
}
