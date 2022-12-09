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

import java.util.function.Function;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dev.jorel.commandapi.CommandAPIHandler;
import dev.jorel.commandapi.nms.NMS;

/**
 * An argument that represents the Bukkit Biome object
 * 
 * @apiNote Returns a {@link Biome} object
 */
public class BiomeArgument extends SafeOverrideableArgument<org.bukkit.block.Biome, org.bukkit.block.Biome> implements ICustomProvidedArgument {

	/**
	 * Constructs a BiomeArgument with a given node name.
	 * 
	 * @param nodeName the name of the node for argument
	 */
	public BiomeArgument(String nodeName) {
		super(nodeName, CommandAPIHandler.getInstance().getNMS()._ArgumentSyntheticBiome(),
			((Function<org.bukkit.block.Biome, String>) org.bukkit.block.Biome::name).andThen(String::toLowerCase));
	}

	@Override
	public Class<org.bukkit.block.Biome> getPrimitiveType() {
		return org.bukkit.block.Biome.class;
	}

	@Override
	public CommandAPIArgumentType getArgumentType() {
		return CommandAPIArgumentType.BIOME;
	}

	@Override
	public SuggestionProviders getSuggestionProvider() {
		return SuggestionProviders.BIOMES;
	}

	@Override
	public <CommandListenerWrapper> org.bukkit.block.Biome parseArgument(NMS<CommandListenerWrapper> nms,
		CommandContext<CommandListenerWrapper> cmdCtx, String key, Object[] previousArgs)
		throws CommandSyntaxException {
		return (org.bukkit.block.Biome) nms.getBiome(cmdCtx, key, ArgumentSubType.BIOME_BIOME);
	}

	/**
	 * An argument that represents the Bukkit Biome object
	 * 
	 * @apiNote Returns a {@link org.bukkit.block.Biome} object
	 */
	// For feature parity, BiomeArgument.Biome() is equivalent to BiomeArgument()
	public static class Biome extends BiomeArgument {

		/**
		 * Constructs a BiomeArgument with a given node name.
		 * 
		 * @param nodeName the name of the node for argument
		 */
		public Biome(String nodeName) {
			super(nodeName);
		}

	}

	/**
	 * An argument that represents the Bukkit Biome object
	 * 
	 * @apiNote Returns a {@link NamespacedKey} object
	 */
	public static class NamespacedKey extends SafeOverrideableArgument<org.bukkit.NamespacedKey, org.bukkit.NamespacedKey> implements ICustomProvidedArgument {

		/**
		 * Constructs a BiomeArgument with a given node name. This BiomeArgument will
		 * return a {@link NamespacedKey}
		 * 
		 * @param nodeName the name of the node for argument
		 */
		public NamespacedKey(String nodeName) {
			super(nodeName, CommandAPIHandler.getInstance().getNMS()._ArgumentSyntheticBiome(), org.bukkit.NamespacedKey::toString);
		}

		@Override
		public SuggestionProviders getSuggestionProvider() {
			return SuggestionProviders.BIOMES;
		}

		@Override
		public Class<org.bukkit.NamespacedKey> getPrimitiveType() {
			return org.bukkit.NamespacedKey.class;
		}

		@Override
		public CommandAPIArgumentType getArgumentType() {
			return CommandAPIArgumentType.BIOME;
		}

		@Override
		public <CommandSourceStack> org.bukkit.NamespacedKey parseArgument(NMS<CommandSourceStack> nms, CommandContext<CommandSourceStack> cmdCtx, String key,
			Object[] previousArgs) throws CommandSyntaxException {
			return (org.bukkit.NamespacedKey) nms.getBiome(cmdCtx, key, ArgumentSubType.BIOME_NAMESPACEDKEY);
		}

	}
}
