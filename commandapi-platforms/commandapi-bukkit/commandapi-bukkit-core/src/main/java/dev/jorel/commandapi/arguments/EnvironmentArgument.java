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
import org.bukkit.World.Environment;

import java.util.function.Function;

/**
 * An argument that represents the Bukkit Environment object
 *
 * @deprecated Use {@link WorldArgument} instead.
 */
@Deprecated
public class EnvironmentArgument extends SafeOverrideableArgument<Environment, Environment> {
	
	/**
	 * An Environment argument. Represents Bukkit's Environment object
	 * @param nodeName the name of the node for this argument
	 *
	 * @deprecated Use {@link WorldArgument#WorldArgument(String)} instead.
	 */
	@Deprecated
	public EnvironmentArgument(String nodeName) {
		super(nodeName, CommandAPIBukkit.get()._ArgumentEnvironment(), ((Function<Environment, String>) Environment::name).andThen(String::toLowerCase));
	}
	
	@Override
	public Class<Environment> getPrimitiveType() {
		return Environment.class;
	}
	
	@Override
	public CommandAPIArgumentType getArgumentType() {
		return CommandAPIArgumentType.ENVIRONMENT;
	}
	
	@Override
	public <CommandSourceStack> Environment parseArgument(CommandContext<CommandSourceStack> cmdCtx, String key, Object[] previousArgs) throws CommandSyntaxException {
		return CommandAPIBukkit.<CommandSourceStack>get().getEnvironment(cmdCtx, key);
	}
}
