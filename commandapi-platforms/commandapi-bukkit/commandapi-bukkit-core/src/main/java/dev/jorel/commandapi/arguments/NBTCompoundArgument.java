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

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.executors.CommandArguments;

/**
 * An argument that represents an NBTContainer from the NBT API
 * 
 * @since 3.0
 */
public class NBTCompoundArgument<NBTContainer> extends SafeOverrideableArgument<NBTContainer, NBTContainer> {

	/**
	 * An NBT Compound Argument. Represents Minecraft's NBT Compound Tag using the
	 * NBT API
	 * 
	 * @param nodeName the name of the node for this argument
	 */
	public NBTCompoundArgument(String nodeName) {
		super(nodeName, CommandAPIBukkit.get().getNMS()._ArgumentNBTCompound(), NBTContainer::toString);
		if (CommandAPI.getConfiguration().getNBTContainerClass() == null || CommandAPI.getConfiguration().getNBTContainerConstructor() == null) {
			throw new NullPointerException(
					"The NBTCompoundArgument hasn't been initialized properly! Use CommandAPIConfig.initializeNBTAPI() in your onLoad() method");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<NBTContainer> getPrimitiveType() {
		return (Class<NBTContainer>) CommandAPI.getConfiguration().getNBTContainerClass();
	}

	@Override
	public CommandAPIArgumentType getArgumentType() {
		return CommandAPIArgumentType.NBT_COMPOUND;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <CommandSourceStack> NBTContainer parseArgument(CommandContext<CommandSourceStack> cmdCtx, String key, CommandArguments previousArgs)
			throws CommandSyntaxException {
		return (NBTContainer) CommandAPIBukkit.<CommandSourceStack>get().getNMS().getNBTCompound(cmdCtx, key, CommandAPI.getConfiguration().getNBTContainerConstructor());
	}
}
