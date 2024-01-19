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
import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffectType;

/**
 * An argument that represents the Bukkit PotionEffectType object
 * 
 * @since 1.1
 */
public class PotionEffectArgument extends SafeOverrideableArgument<PotionEffectType, PotionEffectType> {

	/**
	 * A PotionEffect argument. Represents status/potion effects
	 * @param nodeName the name of the node for this argument
	 */
	public PotionEffectArgument(String nodeName) {
		super(nodeName, CommandAPIBukkit.get().getNMS()._ArgumentMobEffect(), CommandAPIBukkit.get().getNMS()::convert);
	}

	@Override
	public Class<PotionEffectType> getPrimitiveType() {
		return PotionEffectType.class;
	}

	@Override
	public CommandAPIArgumentType getArgumentType() {
		return CommandAPIArgumentType.POTION_EFFECT;
	}
	
	@Override
	public <CommandSourceStack> PotionEffectType parseArgument(CommandContext<CommandSourceStack> cmdCtx, String key, CommandArguments previousArgs) throws CommandSyntaxException {
		return (PotionEffectType) CommandAPIBukkit.<CommandSourceStack>get().getNMS().getPotionEffect(cmdCtx, key, ArgumentSubType.POTION_EFFECT_POTION_EFFECT);
	}

	/**
	 * An argument that represents the Bukkit PotionEffectType object
	 *
	 * @apiNote Returns a {@link org.bukkit.NamespacedKey}
	 */
	public static class NamespacedKey extends SafeOverrideableArgument<org.bukkit.NamespacedKey, org.bukkit.NamespacedKey> implements CustomProvidedArgument {

		/**
		 * Constructs a PotionEffectArgument with the given node name. This returns a {@link org.bukkit.NamespacedKey}
		 *
		 * @param nodeName The name of the node for this argument
		 */
		public NamespacedKey(String nodeName) {
			super(nodeName, CommandAPIBukkit.get().getNMS()._ArgumentMinecraftKeyRegistered(), org.bukkit.NamespacedKey::toString);
		}

		@Override
		public SuggestionProviders getSuggestionProvider() {
			return SuggestionProviders.POTION_EFFECTS;
		}

		@Override
		public Class<org.bukkit.NamespacedKey> getPrimitiveType() {
			return org.bukkit.NamespacedKey.class;
		}

		@Override
		public CommandAPIArgumentType getArgumentType() {
			return CommandAPIArgumentType.POTION_EFFECT;
		}

		@Override
		public <CommandSourceStack> org.bukkit.NamespacedKey parseArgument(CommandContext<CommandSourceStack> cmdCtx, String key, CommandArguments previousArgs) throws CommandSyntaxException {
			return (org.bukkit.NamespacedKey) CommandAPIBukkit.<CommandSourceStack>get().getNMS().getPotionEffect(cmdCtx, key, ArgumentSubType.POTION_EFFECT_NAMESPACEDKEY);
		}
	}

}
