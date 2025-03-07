package dev.jorel.commandapi.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.SuggestionInfo;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.wrappers.CommandResult;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * An argument that represents a command. This allows users
 * to enter the name and arguments of any other command as
 * an argument to this command.
 *
 * @since 8.6.0
 *
 * @apiNote Returns a {@link CommandResult} object
 */
public class CommandArgument extends Argument<CommandResult> implements GreedyArgument {
	/**
	 * Constructs a {@link CommandArgument} with the given node name.
	 *
	 * @param nodeName the name of the node for this argument
	 */
	public CommandArgument(String nodeName) {
		super(nodeName, StringArgumentType.greedyString());

		applySuggestions();
	}

	private void applySuggestions() {
		super.replaceSuggestions((info, builder) -> {
			// Extract information
			CommandSender sender = info.sender();
			CommandMap commandMap = CommandAPIBukkit.get().getSimpleCommandMap();
			String command = info.currentArg();

			// Setup context for errors
			StringReader context = new StringReader(command);

			if (!command.contains(" ")) {
				// Suggesting command name
				ArgumentSuggestions<CommandSender> replacement = replacements.getNextSuggestion(sender);
				if (replacement != null) {
					return replacement.suggest(new SuggestionInfo<>(sender, new CommandArguments(new Object[0], new LinkedHashMap<>(), new String[0], new LinkedHashMap<>(), info.currentInput()), command, command), builder);
				}

				List<String> results = commandMap.tabComplete(sender, command);
				// No applicable commands
				if (results == null) {
					throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(context);
				}

				// Remove / that gets prefixed to command name if the sender is a player
				if (sender instanceof Player) {
					for (String result : results) {
						builder.suggest(result.substring(1));
					}
				} else {
					for (String result : results) {
						builder.suggest(result);
					}
				}

				return builder.buildFuture();
			}


			// Verify commandLabel
			String commandLabel = command.substring(0, command.indexOf(" "));
			Command target = commandMap.getCommand(commandLabel);
			if (target == null) {
				throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(context);
			}

			// Get arguments
			String[] arguments = command.split(" ");
			if (!arguments[0].isEmpty() && command.endsWith(" ")) {
				// If command ends with space add an empty argument
				arguments = Arrays.copyOf(arguments, arguments.length + 1);
				arguments[arguments.length - 1] = "";
			}

			// Build suggestion
			builder = builder.createOffset(builder.getStart() + command.lastIndexOf(" ") + 1);

			int lastIndex = arguments.length - 1;
			String[] previousArguments = Arrays.copyOf(arguments, lastIndex);
			ArgumentSuggestions<CommandSender> replacement = replacements.getNextSuggestion(sender, previousArguments);
			if (replacement != null) {
				return replacement.suggest(new SuggestionInfo<>(sender, new CommandArguments(previousArguments, new LinkedHashMap<>(), previousArguments, new LinkedHashMap<>(), info.currentInput()), command, arguments[lastIndex]), builder);
			}

			// Remove command name from arguments for normal tab-completion
			arguments = Arrays.copyOfRange(arguments, 1, arguments.length);

			// Get location sender is looking at if they are a Player, matching vanilla behavior
			// No builtin Commands use the location parameter, but they could
			Location location = null;
			if (sender instanceof Player player) {
				Block block = player.getTargetBlockExact(5, FluidCollisionMode.NEVER);
				if (block != null) {
					location = block.getLocation();
				}
			}

			// Build suggestions for new argument
			for (String tabCompletion : target.tabComplete(sender, commandLabel, arguments, location)) {
				builder.suggest(tabCompletion);
			}
			return builder.buildFuture();
		});
	}

	SuggestionsBranch<CommandSender> replacements = SuggestionsBranch.suggest();

	/**
	 * Replaces the default command suggestions provided by the server with custom
	 * suggestions for each argument in the command, starting with the command's
	 * name. If a suggestion is null or there isn't any suggestions given for that
	 * argument, the suggestions will not be overridden.
	 *
	 * @param suggestions An array of {@link ArgumentSuggestions} representing the
	 *                    suggestions. Use the static methods in ArgumentSuggestions
	 *                    to create these.
	 * @return the current argument
	 */
	@SafeVarargs
	public final CommandArgument replaceSuggestions(ArgumentSuggestions<CommandSender>... suggestions) {
		replacements = SuggestionsBranch.suggest(suggestions);
		return this;
	}

	/**
	 * Replaces the default command suggestions provided by the server with custom
	 * suggestions for each argument in the command, starting with the command's
	 * name. If a suggestion is null or there isn't any suggestions given for that
	 * argument, the suggestions will not be overridden.
	 *
	 * @param suggestions An array of {@link ArgumentSuggestions} representing the
	 *                    suggestions. Use the static methods in ArgumentSuggestions
	 *                    to create these.
	 * @return the current argument
	 */
	@Override
	public CommandArgument replaceSuggestions(ArgumentSuggestions<CommandSender> suggestions) {
		replacements = SuggestionsBranch.suggest(suggestions);
		return this;
	}

	/**
	 * Adds {@link SuggestionsBranch} to this CommandArgument. After going through
	 * the suggestions provided by
	 * {@link CommandArgument#replaceSuggestions(ArgumentSuggestions...)} the
	 * suggestions of these branches will be used.
	 *
	 * @param branches An array of {@link SuggestionsBranch} representing the
	 *                 branching suggestions. Use
	 *                 {@link SuggestionsBranch#suggest(ArgumentSuggestions...)} to
	 *                 start creating these.
	 * @return the current argument
	 */
	@SafeVarargs
	public final Argument<CommandResult> branchSuggestions(SuggestionsBranch<CommandSender>... branches) {
		replacements.branch(branches);
		return this;
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
	public <CommandSourceStack> CommandResult parseArgument(CommandContext<CommandSourceStack> cmdCtx, String key, CommandArguments previousArgs) throws CommandSyntaxException {
		// Extract information
		String command = cmdCtx.getArgument(key, String.class);
		CommandMap commandMap = CommandAPIBukkit.get().getSimpleCommandMap();
		CommandSender sender = CommandAPIBukkit.<CommandSourceStack>get().getCommandSenderFromCommandSource(cmdCtx.getSource());

		StringReader context = new StringReader(command);

		// Verify command
		String[] arguments = command.split(" ");
		String commandLabel = arguments[0];
		Command target = commandMap.getCommand(commandLabel);
		if (target == null) {
			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(context);
		}

		// Check all replacements
		replacements.enforceReplacements(sender, arguments);

		return new CommandResult(target, Arrays.copyOfRange(arguments, 1, arguments.length));
	}
}
