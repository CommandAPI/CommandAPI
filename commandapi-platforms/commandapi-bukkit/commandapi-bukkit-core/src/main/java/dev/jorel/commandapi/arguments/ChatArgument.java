/*******************************************************************************
 * Copyright 2018, 2020 Jorel Ali (Skepter) - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package dev.jorel.commandapi.arguments;

import java.util.Optional;

import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPIHandler;
import dev.jorel.commandapi.exceptions.SpigotNotFoundException;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.wrappers.PreviewableFunction;
import net.md_5.bungee.api.chat.BaseComponent;

/**
 * An argument that represents chat with entity selectors
 * 
 * @since 3.0
 * 
 * @apiNote Returns a {@link BaseComponent}{@code []} object
 */
public class ChatArgument extends Argument<BaseComponent[]> implements GreedyArgument, Previewable<ChatArgument, BaseComponent[], Player> {

	private PreviewableFunction<BaseComponent[], Player> preview;
	private boolean usePreview;

	/**
	 * Constructs a Chat argument with a given node name. Represents fancy greedy
	 * strings that can parse entity selectors
	 * 
	 * @param nodeName the name of the node for argument
	 */
	public ChatArgument(String nodeName) {
		super(nodeName, CommandAPIBukkit.get()._ArgumentChat());

		try {
			Class.forName("org.spigotmc.SpigotConfig");
		} catch (ClassNotFoundException e) {
			throw new SpigotNotFoundException(this.getClass());
		}
	}

	@Override
	public Class<BaseComponent[]> getPrimitiveType() {
		return BaseComponent[].class;
	}

	@Override
	public CommandAPIArgumentType getArgumentType() {
		return CommandAPIArgumentType.CHAT;
	}

	@Override
	public <CommandSourceStack> BaseComponent[] parseArgument(CommandContext<CommandSourceStack> cmdCtx, String key, CommandArguments previousArgs) throws CommandSyntaxException {
		final CommandSender sender = CommandAPIBukkit.<CommandSourceStack>get().getCommandSenderFromCommandSource(cmdCtx.getSource());
		BaseComponent[] component = CommandAPIBukkit.<CommandSourceStack>get().getChat(cmdCtx, key);

		Optional<PreviewableFunction<BaseComponent[], Player>> previewOptional = getPreview();
		if (this.usePreview && previewOptional.isPresent() && sender instanceof Player player) {
			try {
				BaseComponent[] previewComponent = previewOptional.get()
					.generatePreview(new PreviewInfo<>(player, CommandAPIHandler.getRawArgumentInput(cmdCtx, key), cmdCtx.getInput(), component));

				component = previewComponent;
			} catch (WrapperCommandSyntaxException e) {
				throw e.getException();
			}
		}
		return component;
	}

	@Override
	public ChatArgument withPreview(PreviewableFunction<BaseComponent[], Player> preview) {
		this.preview = preview;
		return this;
	}

	@Override
	public Optional<PreviewableFunction<BaseComponent[], Player>> getPreview() {
		return Optional.ofNullable(preview);
	}

	@Override
	public boolean isLegacy() {
		return true;
	}

	@Override
	public ChatArgument usePreview(boolean usePreview) {
		this.usePreview = usePreview;
		return this;
	}
}
