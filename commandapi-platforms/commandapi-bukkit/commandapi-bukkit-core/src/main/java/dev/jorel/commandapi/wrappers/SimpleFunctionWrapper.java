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
package dev.jorel.commandapi.wrappers;

import java.util.Set;
import java.util.function.ToIntFunction;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.CommandAPIBukkit;

/**
 * A wrapper class for Minecraft 1.12's functions
 */
public class SimpleFunctionWrapper implements Keyed {
	
	final NamespacedKey minecraftKey;
	final ToIntFunction<Object> functionExecutor; //What the function does
	final String[] internalCommands;
	
	/**
	 * Creates a SimpleFunctionWrapper
	 * @param minecraftKey the MinecraftKey which is used to reference this function
	 * @param invoker a method which, when invoked, runs the function
	 * @param internalCommands a String[] of internal commands that this customFunction represents. Typically customFunction.b().map(Object::toString)
	 */
	@SuppressWarnings("unchecked")
	public SimpleFunctionWrapper(NamespacedKey minecraftKey, @SuppressWarnings("rawtypes") ToIntFunction invoker, String[] internalCommands) {
		this.minecraftKey = minecraftKey;
		this.functionExecutor = invoker;
		this.internalCommands = internalCommands;
	}
	
	SimpleFunctionWrapper(SimpleFunctionWrapper functionWrapper) {
		this.minecraftKey = functionWrapper.minecraftKey;
		this.functionExecutor = functionWrapper.functionExecutor;
		this.internalCommands = functionWrapper.internalCommands;
	}
	
	/**
	 * Returns a SimpleFunctionWrapper[], which is an array of all of the functions that this tag contains 
	 * @param key a NamespacedKey representation of the tag. This key should not include a # symbol.
	 * @return a SimpleFunctionWrapper[], which is an array of all of the functions that this tag contains
	 */
	public static SimpleFunctionWrapper[] getTag(NamespacedKey key) {
		return CommandAPIBukkit.get().getTag(key);
	}
	
	/**
	 * Returns a SimpleFunctionWrapper representation of the Minecraft function for the provided NamespacedKey
	 * @param key a NamespacedKey representation of the function
	 * @return a SimpleFunctionWrapper representation of the Minecraft function for the provided NamespacedKey
	 */
	public static SimpleFunctionWrapper getFunction(NamespacedKey key) {
		return CommandAPIBukkit.get().getFunction(key);
	}
	
	/**
	 * Returns a set of all functions that the server has registered
	 * @return a set of all functions that the server has registered
	 */
	public static Set<NamespacedKey> getFunctions() {
		return CommandAPIBukkit.get().getFunctions();
	}
	
	/**
	 * Returns a set of all tags that the server has registered
	 * @return a set of all tags that the server has registered
	 */
	public static Set<NamespacedKey> getTags() {
		return CommandAPIBukkit.get().getTags();
	}
	
	/**
	 * Runs this function with a given CommandSender
	 * @param sender the sender to use to run this function
	 * @return the result of running this command
	 */
	public int run(CommandSender sender) {
		CommandAPIBukkit<?> platform = CommandAPIBukkit.get();
		return runInternal(platform.getBrigadierSourceFromCommandSender(sender));
	}
	
	/**
	 * Returns an array of commands that will be executed by this FunctionWrapper 
	 * @return the commands that are defined by this custom function
	 */
	public String[] getCommands() {
		return this.internalCommands;
	}
	
	int runInternal(Object clw) {
		return functionExecutor.applyAsInt(clw);
	}

	/**
	 * Returns the NamespacedKey that uniquely represents this object
	 * @return the NamespacedKey that uniquely represents this object
	 */
	@Override
	public NamespacedKey getKey() {
		return minecraftKey;
	}
	
}
