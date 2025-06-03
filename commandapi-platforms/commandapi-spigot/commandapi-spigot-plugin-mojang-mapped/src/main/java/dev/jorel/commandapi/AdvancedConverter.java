/*******************************************************************************
 * Copyright 2018, 2021 Jorel Ali (Skepter) - MIT License
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
package dev.jorel.commandapi;

import dev.jorel.commandapi.arguments.AdvancementArgument;
import dev.jorel.commandapi.arguments.AngleArgument;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.AsyncPlayerProfileArgument;
import dev.jorel.commandapi.arguments.AxisArgument;
import dev.jorel.commandapi.arguments.BiomeArgument;
import dev.jorel.commandapi.arguments.BlockPredicateArgument;
import dev.jorel.commandapi.arguments.BlockStateArgument;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.ChatArgument;
import dev.jorel.commandapi.arguments.ChatColorArgument;
import dev.jorel.commandapi.arguments.ChatComponentArgument;
import dev.jorel.commandapi.arguments.CommandAPIArgumentType;
import dev.jorel.commandapi.arguments.CommandArgument;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.EnchantmentArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.EntityTypeArgument;
import dev.jorel.commandapi.arguments.FloatArgument;
import dev.jorel.commandapi.arguments.FloatRangeArgument;
import dev.jorel.commandapi.arguments.FunctionArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.IntegerRangeArgument;
import dev.jorel.commandapi.arguments.ItemStackArgument;
import dev.jorel.commandapi.arguments.ItemStackPredicateArgument;
import dev.jorel.commandapi.arguments.Location2DArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.arguments.LongArgument;
import dev.jorel.commandapi.arguments.LootTableArgument;
import dev.jorel.commandapi.arguments.MathOperationArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.NBTCompoundArgument;
import dev.jorel.commandapi.arguments.NamespacedKeyArgument;
import dev.jorel.commandapi.arguments.ObjectiveArgument;
import dev.jorel.commandapi.arguments.ObjectiveCriteriaArgument;
import dev.jorel.commandapi.arguments.ParticleArgument;
import dev.jorel.commandapi.arguments.PlayerProfileArgument;
import dev.jorel.commandapi.arguments.PotionEffectArgument;
import dev.jorel.commandapi.arguments.RecipeArgument;
import dev.jorel.commandapi.arguments.RotationArgument;
import dev.jorel.commandapi.arguments.ScoreHolderArgument;
import dev.jorel.commandapi.arguments.ScoreboardSlotArgument;
import dev.jorel.commandapi.arguments.SoundArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.TeamArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import dev.jorel.commandapi.arguments.TimeArgument;
import dev.jorel.commandapi.arguments.UUIDArgument;
import dev.jorel.commandapi.exceptions.InvalidNumberException;
import dev.jorel.commandapi.exceptions.UnknownArgumentException;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A command parsing system that converts string arguments into something way
 * more useful
 */
class AdvancedConverter {

	/**
	 * Pattern for arguments, which are of the form {@code <node_name>[type]}
	 * <ul>
	 * <li>{@code node_name} is a standard node name.</li>
	 * <li>{@code type} is either a namespaced key representing the argument
	 * (described in {@code conversionforownerssingleargs.md} in the main
	 * documentation), or a numerical range of the form {@code n..n}, {@code ..n} or
	 * {@code n..} where {@code n} is a constant integer or floating point
	 * number</li>
	 * </ul>
	 */
	private static final Pattern ARGUMENT_PATTERN = Pattern.compile("<(\\w+)>\\[([a-z:_]+|(?:[0-9\\.]+)?\\.\\.(?:[0-9\\.]+)?)\\]");

	/**
	 * Pattern for literal, of the form {@code (node_name)}. It also supports
	 * multiple node names of the form
	 * {@code (node_name_1|node_name_2|...|node_name_n)}
	 */
	private static final Pattern LITERAL_PATTERN = Pattern.compile("\\(\\w+(\\|\\w+)*+\\)");

	private final JavaPlugin plugin;
	private final String command;
	private int argumentIndex = 1;

	public AdvancedConverter(JavaPlugin plugin, String command) {
		this.plugin = plugin;
		this.command = command;
	}

	public AdvancedConverter(String command) {
		this.plugin = null;
		this.command = command;
	}

	public void convert() {
		String commandName = command.split(" ")[0];
		List<Argument<?>> arguments;
		try {
			arguments = parseArguments(command);
		} catch (UnknownArgumentException | InvalidNumberException e) {
			CommandAPI.logError(e.getMessage());
			return;
		}
		if (arguments.isEmpty()) {
			Converter.convert(plugin, commandName);
		} else {
			Converter.convert(plugin, commandName, arguments);
		}

	}

	public void convertCommand() {
		String commandName = command.split(" ")[0];
		List<Argument<?>> arguments;
		try {
			arguments = parseArguments(command);
		} catch (UnknownArgumentException | InvalidNumberException e) {
			CommandAPI.logError(e.getMessage());
			return;
		}
		if (arguments.isEmpty()) {
			Converter.convert(commandName);
		} else {
			Converter.convert(commandName, arguments);
		}
	}

	/**
	 * <pre>
	 * plugins-to-convert: 
	 *   - Essentials: 
	 *     - speed <speed>[0..10]
	 *     - speed <target>[minecraft:game_profile]
	 *     - speed (walk|fly) <speed>[0..10]
	 *     - speed (walk|fly) <speed>[0..10] <target>[minecraft:game_profile]
	 * </pre>
	 */
	private List<Argument<?>> parseArguments(String command) throws UnknownArgumentException, InvalidNumberException {
		List<Argument<?>> arguments = new ArrayList<>();
		String[] parts = command.split(" ");
		for (argumentIndex = 1; argumentIndex < parts.length; argumentIndex++) {
			arguments.add(parseArgument(parts[argumentIndex]));
		}
		return arguments;
	}

	private boolean isRangeAnInteger(double value) {
		return value == (long) value;
	}

	private double parseValue(String bound) throws InvalidNumberException {
		try {
			return Double.parseDouble(bound);
		} catch (NumberFormatException e) {
			throw new InvalidNumberException(bound, command, argumentIndex);
		}
	}

	private Argument<?> parseRange(String nodeName, String[] bounds) throws InvalidNumberException {
		if (bounds.length == 1) {
			// x..
			double value = parseValue(bounds[0]);
			if (isRangeAnInteger(value)) {
				return new LongArgument(nodeName, (long) value);
			} else {
				return new DoubleArgument(nodeName, value);
			}
		} else if (bounds[0].length() == 0) {
			// ..x
			double value = parseValue(bounds[1]);
			if (isRangeAnInteger(value)) {
				return new LongArgument(nodeName, Long.MIN_VALUE, (long) value);
			} else {
				return new DoubleArgument(nodeName, -Double.MAX_VALUE, value);
			}
		} else {
			// x..x
			double value0 = parseValue(bounds[0]);
			double value1 = parseValue(bounds[1]);
			if (!isRangeAnInteger(value0) || !isRangeAnInteger(value1)) {
				return new DoubleArgument(nodeName, value0, value1);
			} else {
				return new LongArgument(nodeName, (long) value0, (long) value1);
			}
		}
	}

	/*
	 * CodeFactor will always complain about this method because it's really bulky
	 * and adding a generator (i.e. Function<String, Argument<?>>) inside the
	 * CommandAPIArgumentType class would be better, EXCEPT in practice, this is
	 * worse because then CommandAPIArgumentType would have to depend on every
	 * argument and every argument depends on CommandAPIArgumentType, so that would
	 * mean every argument would always have to be packaged in the CommandAPI - jar
	 * minimisation wouldn't be able to minimise this as best as it could.
	 * 
	 * Additionally, we only need this for the plugin version of the CommandAPI, not
	 * the main API.
	 */
	@SuppressWarnings("rawtypes")
	private Argument<?> parseDefinedArgumentType(String argumentType, String nodeName) throws UnknownArgumentException {
		return switch (CommandAPIArgumentType.fromInternal(argumentType)) {
			case ADVANCEMENT -> new AdvancementArgument(nodeName);
			case ADVENTURE_CHAT -> new ChatArgument(nodeName);
			case ADVENTURE_CHAT_COMPONENT -> new ChatComponentArgument(nodeName);
			case ANGLE -> new AngleArgument(nodeName);
			case ASYNC_OFFLINE_PLAYER -> new AsyncPlayerProfileArgument(nodeName);
			case AXIS -> new AxisArgument(nodeName);
			case BIOME -> new BiomeArgument(nodeName);
			case BLOCKSTATE -> new BlockStateArgument(nodeName);
			case BLOCK_PREDICATE -> new BlockPredicateArgument(nodeName);
			case CHAT -> new ChatArgument(nodeName);
			case CHATCOLOR -> new ChatColorArgument(nodeName);
			case CHAT_COMPONENT -> new ChatComponentArgument(nodeName);
			case COMMAND -> new CommandArgument(nodeName);
			case ENCHANTMENT -> new EnchantmentArgument(nodeName);
			case ENTITY_SELECTOR -> new EntitySelectorArgument.OneEntity(nodeName);
			case ENTITY_TYPE -> new EntityTypeArgument(nodeName);
			case FLOAT_RANGE -> new FloatRangeArgument(nodeName);
			case FUNCTION -> new FunctionArgument(nodeName);
			case INT_RANGE -> new IntegerRangeArgument(nodeName);
			case ITEMSTACK -> new ItemStackArgument(nodeName);
			case ITEMSTACK_PREDICATE -> new ItemStackPredicateArgument(nodeName);
			case LOCATION -> new LocationArgument(nodeName, LocationType.BLOCK_POSITION);
			case LOCATION_2D -> new Location2DArgument(nodeName, LocationType.BLOCK_POSITION);
			case LOOT_TABLE -> new LootTableArgument(nodeName);
			case MATH_OPERATION -> new MathOperationArgument(nodeName);
			case NAMESPACED_KEY -> new NamespacedKeyArgument(nodeName);
			case NBT_COMPOUND -> new NBTCompoundArgument(nodeName);
			case OBJECTIVE -> new ObjectiveArgument(nodeName);
			case OBJECTIVE_CRITERIA -> new ObjectiveCriteriaArgument(nodeName);
			case PARTICLE -> new ParticleArgument(nodeName);
			case PLAYER -> new PlayerProfileArgument(nodeName);
			case POTION_EFFECT -> new PotionEffectArgument(nodeName);
			case RECIPE -> new RecipeArgument(nodeName);
			case ROTATION -> new RotationArgument(nodeName);
			case SCOREBOARD_SLOT -> new ScoreboardSlotArgument(nodeName);
			case SCORE_HOLDER -> new ScoreHolderArgument.Single(nodeName);
			case SOUND -> new SoundArgument(nodeName);
			case TEAM -> new TeamArgument(nodeName);
			case TIME -> new TimeArgument(nodeName);
			case UUID -> new UUIDArgument(nodeName);
			case PRIMITIVE_BOOLEAN -> new BooleanArgument(nodeName);
			case PRIMITIVE_DOUBLE -> new DoubleArgument(nodeName);
			case PRIMITIVE_FLOAT -> new FloatArgument(nodeName);
			case PRIMITIVE_GREEDY_STRING -> new GreedyStringArgument(nodeName);
			case PRIMITIVE_INTEGER -> new IntegerArgument(nodeName);
			case PRIMITIVE_LONG -> new LongArgument(nodeName);
			case PRIMITIVE_STRING -> new StringArgument(nodeName);
			case PRIMITIVE_TEXT -> new TextArgument(nodeName);
			case LITERAL, MULTI_LITERAL, CUSTOM -> throw new UnknownArgumentException(argumentType);
			default -> throw new UnknownArgumentException(argumentType);
		};
	}

	private Argument<?> parseArgument(String argument) throws UnknownArgumentException, InvalidNumberException {
		Matcher literalMatcher = LITERAL_PATTERN.matcher(argument);
		Matcher argumentMatcher = ARGUMENT_PATTERN.matcher(argument);
		if (literalMatcher.matches()) {
			// Parse literals
			return new MultiLiteralArgument("literals", literalMatcher.group().substring(1, literalMatcher.group().length() - 1).split("\\|"));
		} else if (argumentMatcher.matches()) {
			// Parse arguments
			String nodeName = argumentMatcher.group(1);
			String argumentType = argumentMatcher.group(2);

			if (argumentType.contains("..")) {
				// Parse ranges
				return parseRange(nodeName, argumentType.split("\\.\\."));
			} else {
				// We have a few edge cases to handle
				return switch (argumentType) {
					case "api:entity" -> new EntitySelectorArgument.OneEntity(nodeName);
					case "api:entities" -> new EntitySelectorArgument.ManyEntities(nodeName);
					case "api:player" -> new EntitySelectorArgument.OnePlayer(nodeName);
					case "api:players" -> new EntitySelectorArgument.ManyPlayers(nodeName);
					case "minecraft:vec3" -> new LocationArgument(nodeName, LocationType.PRECISE_POSITION);
					case "minecraft:vec2" -> new Location2DArgument(nodeName, LocationType.PRECISE_POSITION);
					default -> parseDefinedArgumentType(argumentType, nodeName);
				};
			}
		} else {
			throw new UnknownArgumentException(argument);
		}
	}

}
