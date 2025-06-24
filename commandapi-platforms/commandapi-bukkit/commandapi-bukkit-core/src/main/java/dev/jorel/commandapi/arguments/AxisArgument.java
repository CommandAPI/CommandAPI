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
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.Axis;

import java.util.EnumSet;

/**
 * An argument that represents x, y and z axes as an EnumSet of Axis
 * 
 * @since 3.0
 * 
 * @apiNote Returns a {@link EnumSet}{@code <}{@link Axis}{@code >} object
 */
@SuppressWarnings("rawtypes")
public class AxisArgument extends SafeOverrideableArgument<EnumSet, EnumSet<Axis>> {

	/**
	 * Constructs an AxisArgument with a given node name. Represents the axes x, y
	 * and z
	 * 
	 * @param nodeName the name of the node for argument
	 */
	public AxisArgument(String nodeName) {
		super(nodeName, CommandAPIBukkit.get().getNMS()._ArgumentAxis(),
			e -> e.stream().map(Axis::name).map(String::toLowerCase).reduce(String::concat).get());
	}

	@Override
	public Class<EnumSet> getPrimitiveType() {
		return EnumSet.class;
	}

	@Override
	public CommandAPIArgumentType getArgumentType() {
		return CommandAPIArgumentType.AXIS;
	}

	@Override
	public <CommandSourceStack> EnumSet<Axis> parseArgument(CommandContext<CommandSourceStack> cmdCtx, String key,
                                                            CommandArguments previousArgs)
		throws CommandSyntaxException {
		return CommandAPIBukkit.<CommandSourceStack>get().getNMS().getAxis(cmdCtx, key);
	}
}
