/*******************************************************************************
 * Copyright 2018, 2021 Jorel Ali (Skepter) - MIT License
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

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPIPaper;
import dev.jorel.commandapi.exceptions.PaperAdventureNotFoundException;
import dev.jorel.commandapi.executors.CommandArguments;
import net.kyori.adventure.text.Component;

/**
 * An argument that represents raw JSON text
 * 
 * @since 5.10
 * @apiNote Returns a {@link Component} object
 */
public class ChatComponentArgument extends Argument<Component> {

	/**
	 * Constructs a ChatComponent argument with a given node name. Represents raw JSON text, used in Book MetaData, Chat and other various areas of Minecraft
	 * @see <a href="https://minecraft.wiki/w/Commands#Raw_JSON_text">Raw JSON text</a> 
	 * @param nodeName the name of the node for argument
	 */
	public ChatComponentArgument(String nodeName) {
		super(nodeName, CommandAPIBukkit.get()._ArgumentChatComponent());
		
		try {
			Class.forName("net.kyori.adventure.text.Component");
		} catch(ClassNotFoundException e) {
			throw new PaperAdventureNotFoundException(this.getClass());
		}
	}
	
	@Override
	public Class<Component> getPrimitiveType() {
		return Component.class;
	}
	
	@Override
	public CommandAPIArgumentType getArgumentType() {
		return CommandAPIArgumentType.ADVENTURE_CHAT_COMPONENT;
	}
	
	@Override
	public <CommandSourceStack> Component parseArgument(CommandContext<CommandSourceStack> cmdCtx, String key, CommandArguments previousArgs) throws CommandSyntaxException {
		return CommandAPIPaper.<CommandSourceStack>getPaper().getChatComponent(cmdCtx, key);
	}
}
