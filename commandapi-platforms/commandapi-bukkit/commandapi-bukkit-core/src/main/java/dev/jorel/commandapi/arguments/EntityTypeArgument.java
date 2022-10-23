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

import dev.jorel.commandapi.BukkitExecutable;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dev.jorel.commandapi.BukkitPlatform;
import dev.jorel.commandapi.abstractions.AbstractPlatform;

/**
 * An argument that represents the Bukkit EntityType object
 */
public class EntityTypeArgument extends SafeOverrideableArgument<EntityType, EntityType, EntityTypeArgument, CommandSender> implements ICustomProvidedArgument, BukkitExecutable<EntityTypeArgument> {

	/**
	 * An EntityType argument. Represents the type of an Entity
	 * @param nodeName the name of the node for this argument
	 */
	public EntityTypeArgument(String nodeName) {
		super(nodeName, BukkitPlatform.get()._ArgumentEntitySummon(), BukkitPlatform.fromKey(EntityType::getKey));
	}

	@Override
	public Class<EntityType> getPrimitiveType() {
		return EntityType.class;
	}

	@Override
	public CommandAPIArgumentType getArgumentType() {
		return CommandAPIArgumentType.ENTITY_TYPE;
	}

	@Override
	public SuggestionProviders getSuggestionProvider() {
		return SuggestionProviders.ENTITIES;
	}
	
	@Override
	public <CommandSourceStack> EntityType parseArgument(AbstractPlatform<CommandSender, CommandSourceStack> platform,
			CommandContext<CommandSourceStack> cmdCtx, String key, Object[] previousArgs) throws CommandSyntaxException {
		return ((BukkitPlatform<CommandSourceStack>) platform).getEntityType(cmdCtx, key);
	}
}