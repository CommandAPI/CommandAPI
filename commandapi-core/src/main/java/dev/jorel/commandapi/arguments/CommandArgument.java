package dev.jorel.commandapi.arguments;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.jorel.commandapi.CommandAPIHandler;
import dev.jorel.commandapi.SuggestionInfo;
import dev.jorel.commandapi.nms.NMS;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandArgument extends Argument<CommandResult> implements IGreedyArgument {
	public CommandArgument(String nodeName) {
		super(nodeName, StringArgumentType.greedyString());

		applySuggestions();
	}

	private void applySuggestions() {
		super.replaceSuggestions((info, builder) -> {
			CommandSender sender = info.sender();
			// The greedy string, being filled by a command
			String command = info.currentArg();

			CommandMap commandMap = CommandAPIHandler.getInstance().getNMS().getSimpleCommandMap();

			// Setup context for errors
			StringReader context = new StringReader(command);

			if (!command.contains(" ")) {
				// Suggesting command name
				if (hasReplacement(0))
					return replacements[0].suggest(new SuggestionInfo(sender, new Object[0], command, command), builder);

				List<String> results = commandMap.tabComplete(sender, command);
				// No applicable commands
				if (results == null)
					throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(context);

				// Build suggestions for new argument
				if (sender instanceof Player)
					// Remove / that gets prefixed to command name if the sender is a player
					results.stream().map(s -> s.substring(1)).forEach(builder::suggest);
				else
					results.forEach(builder::suggest);

				return builder.buildFuture();
			}


			// Verify commandLabel
			String commandLabel = command.substring(0, command.indexOf(" "));
			Command target = commandMap.getCommand(commandLabel);
			if (target == null)
				throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(context);

			// Enforce argument suggestions
			String[] arguments = command.split(" ");
			if (!arguments[0].isEmpty() && command.endsWith(" ")) {
				// If command ends with space add an empty argument
				arguments = Arrays.copyOf(arguments, arguments.length + 1);
				arguments[arguments.length - 1] = "";
			}

			// Check all previous arguments
			enforceReplacements(sender, context, arguments, arguments.length - 1);

			// Build suggestion
			builder = builder.createOffset(builder.getStart() + command.lastIndexOf(" ") + 1);

			int lastIndex = arguments.length - 1;
			if (hasReplacement(lastIndex))
				return replacements[lastIndex].suggest(new SuggestionInfo(sender, Arrays.copyOf(arguments, lastIndex), command, arguments[lastIndex]), builder);

			// Get location sender is looking at if they are a Player, matching vanilla behavior
			// No builtin Commands use the location parameter, but they could
			Location location = null;
			if (sender instanceof Player player) {
				Block block = player.getTargetBlockExact(5, FluidCollisionMode.NEVER);
				if (block != null) location = block.getLocation();
			}

			// Build suggestions for new argument
			target.tabComplete(sender, commandLabel, arguments, location).forEach(builder::suggest);
			return builder.buildFuture();
		});
	}

	private boolean hasReplacement(int index) {
		return replacements.length > index && replacements[index] != null;
	}

	private void enforceReplacements(CommandSender sender, StringReader context, String[] arguments, int range) throws CommandSyntaxException {
		List<String> previousArguments = new ArrayList<>();
		StringBuilder currentInput = new StringBuilder();
		for (int i = 0; i < range; i++) {
			String currentArgument = "";
			if (i < arguments.length)
				currentArgument = arguments[i];

			if (hasReplacement(i)) {
				SuggestionsBuilder argumentBuilder = new SuggestionsBuilder(currentInput.toString(), currentInput.length());
				replacements[i].suggest(new SuggestionInfo(sender, previousArguments.toArray(), currentInput.toString(), ""), argumentBuilder);
				Suggestions suggestions = argumentBuilder.build();
				List<String> results = suggestions.getList().stream().map(Suggestion::getText).toList();
				if (i < arguments.length) {
					if (!results.contains(currentArgument)) {
						if (i == 0)
							throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(context);
						else
							throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(context);
					}
				} else {
					if (results.size() != 0)
						throw new SimpleCommandExceptionType(new LiteralMessage("Expected more arguments")).createWithContext(context);
					else
						break;
				}
			}

			if (i != 0) currentInput.append(" ");
			currentInput.append(currentArgument);
			previousArguments.add(currentArgument);
			context.setCursor(currentInput.length());
		}
	}

	ArgumentSuggestions[] replacements = new ArgumentSuggestions[0];

	/**
	 * Replaces the default command suggestions provided by the server with custom suggestions for each argument in the
	 * command, starting with the command's name. If a suggestion is null or the array is too short, the suggestions in
	 * that position will not be overridden.
	 *
	 * @param suggestions An array of {@link ArgumentSuggestions} representing the suggestions. Use the static methods in
	 * ArgumentSuggestions to create these.
	 * @return the current argument
	 */
	public Argument<CommandResult> replaceSuggestions(ArgumentSuggestions... suggestions) {
		this.replacements = suggestions;
		return this;
	}

	/**
	 * Replaces the default command suggestions provided by the server with custom suggestions for each argument in the
	 * command, starting with the command's name. If a suggestion is null or the array is too short, the suggestions in
	 * that position will not be overridden.
	 *
	 * @param suggestions An array of {@link ArgumentSuggestions} representing the suggestions. Use the static methods in
	 * ArgumentSuggestions to create these.
	 * @return the current argument
	 */
	@Override
	public Argument<CommandResult> replaceSuggestions(ArgumentSuggestions suggestions) {
		return replaceSuggestions(new ArgumentSuggestions[]{suggestions});
	}

	@Override
	public Class<CommandResult> getPrimitiveType() {
		return CommandResult.class;
	}

	@Override
	public CommandAPIArgumentType getArgumentType() {
		return CommandAPIArgumentType.COMMAND;
	}

	@Override
	public <CommandSourceStack> CommandResult parseArgument(NMS<CommandSourceStack> nms, CommandContext<CommandSourceStack> cmdCtx, String key, Object[] previousArgs) throws CommandSyntaxException {
		// Extract information
		String command = cmdCtx.getArgument(key, String.class);
		CommandMap commandMap = nms.getSimpleCommandMap();
		CommandSender sender = nms.getSenderForCommand(cmdCtx, false);

		StringReader context = new StringReader(command);

		// Verify command
		String[] arguments = command.split(" ");
		String commandLabel = arguments[0];
		Command target = commandMap.getCommand(commandLabel);
		if (target == null)
			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(context);

		// check all replacements
		enforceReplacements(sender, context, arguments, replacements.length);

		return new CommandResult(target, Arrays.copyOfRange(arguments, 1, arguments.length));
	}
}
