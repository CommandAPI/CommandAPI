package dev.jorel.commandapi.arguments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.IStringTooltip;
import dev.jorel.commandapi.StringTooltip;
import dev.jorel.commandapi.SuggestionInfo;
import dev.jorel.commandapi.executors.CommandArguments;

/**
 * An argument that accepts a list of objects
 *
 * @param <T> the type that this list argument generates a list of.
 */
@SuppressWarnings("rawtypes")
public class ListArgumentCommon<T> extends Argument<List> {
	private final String delimiter;
	private final boolean allowDuplicates;
	private final Function<SuggestionInfo<CommandSender>, Collection<T>> supplier;
	private final Function<T, IStringTooltip> mapper;
	private final boolean text;

	ListArgumentCommon(String nodeName, String delimiter, boolean allowDuplicates, Function<SuggestionInfo<CommandSender>, Collection<T>> supplier, Function<T, IStringTooltip> suggestionsMapper, boolean text) {
		super(nodeName, text ? StringArgumentType.string() : StringArgumentType.greedyString());
		this.delimiter = delimiter;
		this.allowDuplicates = allowDuplicates;
		this.supplier = supplier;
		this.mapper = suggestionsMapper;
		this.text = text;

		applySuggestions();
	}

	private void applySuggestions() {
		this.replaceSuggestions((info, builder) -> {
			String currentArg = info.currentArg();
			if(text && currentArg.startsWith("\"")) {
				// Ignore initial " when suggesting for TextArgument
				currentArg = currentArg.substring(1);
				builder = builder.createOffset(builder.getStart() + 1);
			}

			// This need not be a sorted map because entries in suggestions are
			// automatically sorted anyway
			Set<IStringTooltip> values = new HashSet<>();
			for (T object : supplier.apply(info)) {
				values.add(mapper.apply(object));
			}

			String[] splitArguments = currentArg.split(Pattern.quote(delimiter));
			// If an argument is finished, suggest the delimiter
			String lastArgument = splitArguments[splitArguments.length - 1];
			if (!currentArg.endsWith(delimiter) && values.stream().map(IStringTooltip::getSuggestion).anyMatch(lastArgument::equals)) {
				values.add(StringTooltip.none(lastArgument + delimiter));
			}

			if (!allowDuplicates) {
				// Filter out values already given
				for (String str : splitArguments) {
					IStringTooltip valueToRemove = null;
					for (IStringTooltip value : values) {
						if (value.getSuggestion().equals(str)) {
							valueToRemove = value;
							break;
						}
					}
					if (valueToRemove != null) {
						values.remove(valueToRemove);
					}
				}
			}


			// Offset builder to just after the last argument
			if (currentArg.contains(delimiter)) {
				builder = builder.createOffset(builder.getStart() + currentArg.lastIndexOf(delimiter) + delimiter.length());
			}

			// 'values' is a set of all objects that need to be suggested
			for (IStringTooltip str : values) {
				if (str.getSuggestion().startsWith(builder.getRemaining())) {
					if (str.getTooltip() == null)
						builder.suggest(str.getSuggestion());
					else
						builder.suggest(str.getSuggestion(), str.getTooltip());
				}
			}

			return builder.buildFuture();
		});
	}

	@Override
	public Class<List> getPrimitiveType() {
		return List.class;
	}

	@Override
	public CommandAPIArgumentType getArgumentType() {
		return CommandAPIArgumentType.LIST;
	}

	@Override
	public <CommandSourceStack> List<T> parseArgument(CommandContext<CommandSourceStack> cmdCtx, String key, CommandArguments previousArgs) throws CommandSyntaxException {
		final CommandSender sender = CommandAPIBukkit.<CommandSourceStack>get().getCommandSenderFromCommandSource(cmdCtx.getSource());
		final SuggestionInfo<CommandSender> currentInfo = new SuggestionInfo<>(sender, previousArgs, cmdCtx.getInput(), cmdCtx.getArgument(key, String.class));
		
		// Get the list of values which this can take
		Map<String, T> values = new HashMap<>();
		for (T object : supplier.apply(currentInfo)) {
			values.put(mapper.apply(object).getSuggestion(), object);
		}
	
		// If the argument's value is in the list of values, include it
		List<T> list = new ArrayList<>();
		Set<String> listKeys = new HashSet<>(); // Set to keep track of duplicates - we can't use the main list because of object hashing
		final String argument = cmdCtx.getArgument(key, String.class);
		final String[] strArr = argument.split(Pattern.quote(delimiter));
		final StringReader context = new StringReader(argument);
		for (String str : strArr) {
			if (!values.containsKey(str)) {
				throw new SimpleCommandExceptionType(new LiteralMessage("Item is not allowed in list")).createWithContext(context);
			} else if (!allowDuplicates && listKeys.contains(str)) {
				throw new SimpleCommandExceptionType(new LiteralMessage("Duplicate arguments are not allowed")).createWithContext(context);
			} else {
				list.add(values.get(str));
				listKeys.add(str);
			}
			context.setCursor(context.getCursor() + str.length() + delimiter.length());
		}
		return list;
	}
}
