/*******************************************************************************
 * Copyright 2022 Jorel Ali (Skepter) - MIT License
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

import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.World;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dev.jorel.commandapi.CommandAPIBukkit;

/**
 * An argument that represents the Bukkit World object
 * 
 * @since 8.6.0
 */
public class WorldArgument extends SafeOverrideableArgument<World, World> {

	/**
	 * A World argument. Represents Bukkit's World object
	 * 
	 * @param nodeName the name of the node for this argument
	 */
	public WorldArgument(String nodeName) {
		// Dev note: DO NOT use a method reference for the World class! See
		// https://github.com/JorelAli/CommandAPI/issues/397 for more info
		super(nodeName, CommandAPIBukkit.get().getNMS()._ArgumentDimension(), world -> world.getName().toLowerCase());
	}

	@Override
	public Class<World> getPrimitiveType() {
		return World.class;
	}

	@Override
	public CommandAPIArgumentType getArgumentType() {
		// We're keeping this underlying internal name as Dimension. The only thing
		// that differs is the name of this class which is "WorldArgument", because
		// the CommandAPI argument class names represent Bukkit objects wherever
		// possible
		return CommandAPIArgumentType.DIMENSION;
	}

	@Override
	public <CommandSourceStack> World parseArgument(CommandContext<CommandSourceStack> cmdCtx, String key, CommandArguments previousArgs) throws CommandSyntaxException {
		return CommandAPIBukkit.<CommandSourceStack>get().getNMS().getDimension(cmdCtx, key);
	}
}
