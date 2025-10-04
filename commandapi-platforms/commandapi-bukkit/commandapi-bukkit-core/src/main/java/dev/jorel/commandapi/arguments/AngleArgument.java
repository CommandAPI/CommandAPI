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

/**
 * An argument that represents a yaw angle, measured in degrees with float
 * number. -180.0 for due north, -90.0 for due east, 0.0 for due south, 90.0 for
 * due west, to 179.9 for just west of due north, before wrapping back around to
 * -180.0. Tilde notation can be used to specify a rotation relative to the
 * executor's yaw angle.
 * 
 * @since 5.0
 * 
 * @apiNote Returns a {@link float}
 */
public class AngleArgument extends SafeOverrideableArgument<Float, Float> {

	/**
	 * Constructs an AngleArgument with a given node name
	 * 
	 * @param nodeName the name of the node for argument
	 */
	public AngleArgument(String nodeName) {
		super(nodeName, CommandAPIBukkit.get().getNMS()._ArgumentAngle(), String::valueOf);
	}

	@Override
	public Class<Float> getPrimitiveType() {
		return float.class;
	}

	@Override
	public CommandAPIArgumentType getArgumentType() {
		return CommandAPIArgumentType.ANGLE;
	}

	@Override
	public <CommandSourceStack> Float parseArgument(CommandContext<CommandSourceStack> cmdCtx, String key, CommandArguments previousArgs)
		throws CommandSyntaxException {
		return CommandAPIBukkit.<CommandSourceStack>get().getNMS().getAngle(cmdCtx, key);
	}
}
