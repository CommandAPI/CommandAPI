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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An argument that represents a selection of entities
 * 
 * @since 1.3
 */
public class EntitySelectorArgument {
	
	private EntitySelectorArgument() {
		throw new IllegalStateException("Use EntitySelectorArgument.OneEntity/OnePlayer/ManyEntities/ManyPlayers instead");
	}

	/**
	 * An argument that represents a single entity
	 *
	 * @apiNote Returns an {@link Entity} object
	 */
	public static class OneEntity extends Argument<Entity> implements FlattenableArgument {

		/**
		 * An argument that represents a single entity
		 * @param nodeName the name of the node for this argument
		 */
		public OneEntity(String nodeName) {
			super(nodeName, CommandAPIBukkit.get()._ArgumentEntity(ArgumentSubType.ENTITYSELECTOR_ONE_ENTITY));
		}

		@Override
		public Class<Entity> getPrimitiveType() {
			return Entity.class;
		}

		@Override
		public CommandAPIArgumentType getArgumentType() {
			return CommandAPIArgumentType.ENTITY_SELECTOR;
		}

		@Override
		public <CommandSourceStack> Entity parseArgument(CommandContext<CommandSourceStack> cmdCtx, String key, CommandArguments previousArgs) throws CommandSyntaxException {
			return (Entity) CommandAPIBukkit.<CommandSourceStack>get().getEntitySelector(cmdCtx, key, ArgumentSubType.ENTITYSELECTOR_ONE_ENTITY, true);
		}

		@Override
		public List<String> flatten(Object argument) {
			return List.of(((Entity) argument).getName());
		}

	}

	/**
	 * An argument that represents a single player
	 *
	 * @apiNote Returns a {@link Player} object
	 */
	public static class OnePlayer extends Argument<Player> implements FlattenableArgument {

		/**
		 * An argument that represents a single player
		 * @param nodeName the name of the node for this argument
		 */
		public OnePlayer(String nodeName) {
			super(nodeName, CommandAPIBukkit.get()._ArgumentEntity(ArgumentSubType.ENTITYSELECTOR_ONE_PLAYER));
		}

		@Override
		public Class<Player> getPrimitiveType() {
			return Player.class;
		}

		@Override
		public CommandAPIArgumentType getArgumentType() {
			return CommandAPIArgumentType.ENTITY_SELECTOR;
		}

		@Override
		public <CommandSourceStack> Player parseArgument(CommandContext<CommandSourceStack> cmdCtx, String key, CommandArguments previousArgs) throws CommandSyntaxException {
			return (Player) CommandAPIBukkit.<CommandSourceStack>get().getEntitySelector(cmdCtx, key, ArgumentSubType.ENTITYSELECTOR_ONE_PLAYER, true);
		}

		@Override
		public List<String> flatten(Object argument) {
			return List.of(((Player) argument).getName());
		}

	}

	/**
	 * An argument that represents many entities
	 *
	 * @apiNote Returns a {@link Collection}{@code <}{@link Entity}{@code >} object
	 */
	@SuppressWarnings("rawtypes")
	public static class ManyEntities extends Argument<Collection> implements FlattenableArgument {

		private final boolean allowEmpty;

		/**
		 * An argument that represents many entities
		 * @param nodeName the name of the node for this argument
		 */
		public ManyEntities(String nodeName) {
			this(nodeName, true);
		}
		
		/**
		 * An argument that represents many entities
		 * @param nodeName the name of the node for this argument
		 * @param allowEmpty whether this entity selector should allow no entities found, or should throw an error instead
		 */
		public ManyEntities(String nodeName, boolean allowEmpty) {
			super(nodeName, CommandAPIBukkit.get()._ArgumentEntity(ArgumentSubType.ENTITYSELECTOR_MANY_ENTITIES));
			this.allowEmpty = allowEmpty;
		}

		@Override
		public Class<Collection> getPrimitiveType() {
			return Collection.class;
		}

		@Override
		public CommandAPIArgumentType getArgumentType() {
			return CommandAPIArgumentType.ENTITY_SELECTOR;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <CommandSourceStack> Collection<Entity> parseArgument(CommandContext<CommandSourceStack> cmdCtx, String key, CommandArguments previousArgs) throws CommandSyntaxException {
			return (Collection<Entity>) CommandAPIBukkit.<CommandSourceStack>get().getEntitySelector(cmdCtx, key, ArgumentSubType.ENTITYSELECTOR_MANY_ENTITIES, this.allowEmpty);
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<String> flatten(Object argument) {
			List<String> entityNames = new ArrayList<>();
			for (Entity entity : (List<Entity>) argument) {
				entityNames.add(entity.getName());
			}
			return entityNames;
		}

	}

	/**
	 * An argument that represents many players
	 *
	 * @apiNote Returns a {@link Collection}{@code <}{@link Player}{@code >} object
	 */
	@SuppressWarnings("rawtypes")
	public static class ManyPlayers extends Argument<Collection> implements FlattenableArgument {

		private final boolean allowEmpty;

		/**
		 * An argument that represents many players
		 * @param nodeName the name of the node for this argument
		 */
		public ManyPlayers(String nodeName) {
			this(nodeName, true);
		}
		
		/**
		 * An argument that represents many players
		 * @param nodeName the name of the node for this argument
		 * * @param allowEmpty whether this entity selector should allow no entities found, or should throw an error instead
		 */
		public ManyPlayers(String nodeName, boolean allowEmpty) {
			super(nodeName, CommandAPIBukkit.get()._ArgumentEntity(ArgumentSubType.ENTITYSELECTOR_MANY_PLAYERS));
			this.allowEmpty = allowEmpty;
		}

		@Override
		public Class<Collection> getPrimitiveType() {
			return Collection.class;
		}

		@Override
		public CommandAPIArgumentType getArgumentType() {
			return CommandAPIArgumentType.ENTITY_SELECTOR;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <CommandSourceStack> Collection<Player> parseArgument(CommandContext<CommandSourceStack> cmdCtx, String key, CommandArguments previousArgs) throws CommandSyntaxException {
			return (Collection<Player>) CommandAPIBukkit.<CommandSourceStack>get().getEntitySelector(cmdCtx, key, ArgumentSubType.ENTITYSELECTOR_MANY_PLAYERS, allowEmpty);
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<String> flatten(Object argument) {
			List<String> playerNames = new ArrayList<>();
			for (Player entity : (List<Player>) argument) {
				playerNames.add(entity.getName());
			}
			return playerNames;
		}

	}
}