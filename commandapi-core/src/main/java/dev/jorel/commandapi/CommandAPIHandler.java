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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import dev.jorel.commandapi.arguments.AbstractArgument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomProvidedArgument;
import dev.jorel.commandapi.arguments.Literal;
import dev.jorel.commandapi.arguments.MultiLiteral;
import dev.jorel.commandapi.commandsenders.AbstractCommandSender;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.ExecutionInfo;
import dev.jorel.commandapi.network.CommandAPIMessenger;
import dev.jorel.commandapi.preprocessor.RequireField;

/**
 * The "brains" behind the CommandAPI.
 * Handles command registration
 *
 * @param <Argument> The implementation of AbstractArgument being used
 * @param <CommandSender> The class for running platform commands
 * @param <Source> The class for running Brigadier commands
 */
@RequireField(in = CommandContext.class, name = "arguments", ofType = Map.class)
public class CommandAPIHandler<Argument
/// @cond DOX
extends AbstractArgument<?, ?, Argument, CommandSender>
/// @endcond
, CommandSender, Source> {
	private static final SafeVarHandle<CommandContext<?>, Map<String, ParsedArgument<?, ?>>> commandContextArguments;

	// Compute all var handles all in one go so we don't do this during main server
	// runtime
	static {
		commandContextArguments = SafeVarHandle.ofOrNull(CommandContext.class, "arguments", "arguments", Map.class);
	}

	/**
	 * Returns the raw input for an argument for a given command context and its
	 * key. This effectively returns the string value that is currently typed for
	 * this argument
	 * 
	 * @param <CommandSource> the command source type
	 * @param cmdCtx               the command context which is used to run this
	 *                             command
	 * @param key                  the node name for the argument
	 * @return the raw input string for this argument
	 */
	public static <CommandSource> String getRawArgumentInput(CommandContext<CommandSource> cmdCtx, String key) {
		final ParsedArgument<?, ?> parsedArgument = commandContextArguments.get(cmdCtx).get(key);
		
		// TODO: Issue #310: Parsing this argument via /execute run <blah> doesn't have the value in
		//  the arguments for this command context (most likely because it's a redirected command).
		//  We need to figure out how to handle this case.
		if (parsedArgument != null) {
			// Sanity check: See https://github.com/JorelAli/CommandAPI/wiki/Implementation-details#chatcomponentargument-raw-arguments
			StringRange range = parsedArgument.getRange();
			if (range.getEnd() > cmdCtx.getInput().length()) {
				range = StringRange.between(range.getStart(), cmdCtx.getInput().length());
			}
			return range.get(cmdCtx.getInput());
		} else {
			return "";
		}
	}

	// TODO: Need to ensure this can be safely "disposed of" when we're done (e.g. on reloads).
	// I hiiiiiiighly doubt we're storing class caches of classes that can be unloaded at runtime,
	// but this IS a generic class caching system and we don't want derpy memory leaks
	private static final Map<ClassCache, Field> FIELDS = new HashMap<>();

	final CommandAPIPlatform<Argument, CommandSender, Source> platform;
	private CommandAPIMessenger<?, ?> messenger;

	final TreeMap<String, CommandPermission> registeredPermissions = new TreeMap<>();
	final List<RegisteredCommand> registeredCommands; // Keep track of what has been registered for type checking
	static final Pattern NAMESPACE_PATTERN = Pattern.compile("[0-9a-z_.-]+");

	private static CommandAPIHandler<?, ?, ?> instance;

	protected CommandAPIHandler(CommandAPIPlatform<Argument, CommandSender, Source> platform) {
		this.platform = platform;
		this.registeredCommands = new ArrayList<>();

		CommandAPIHandler.instance = this;
	}

	public void onLoad(CommandAPIConfig<?> config) {
		checkDependencies();
		platform.onLoad(config);
	}

	private void checkDependencies() {
		// Check for common dependencies
		try {
			Class.forName("com.mojang.brigadier.CommandDispatcher");
		} catch (ClassNotFoundException e) {
			new ClassNotFoundException("Could not hook into Brigadier (Are you running Minecraft 1.13 or above?)")
				.printStackTrace();
		}
	}

	public void onEnable() {
		platform.onEnable();
		// Setting up the network messenger usually requires registering
		//  events, which often does not work until onEnable
		messenger = platform.setupMessenger();
	}

	public void onDisable() {
		messenger.close();
		platform.onDisable();
		CommandAPIHandler.resetInstance();
	}
	
	private static void resetInstance() {
		CommandAPIHandler.instance = null;
	}

	public static CommandAPIHandler<?, ?, ?> getInstance() {
		if(CommandAPIHandler.instance != null) {
			return CommandAPIHandler.instance;
		} else {
			throw new IllegalStateException("Tried to access CommandAPIHandler instance, but it was null! Are you using CommandAPI features before calling CommandAPI#onLoad?");
		}
	}

	public CommandAPIPlatform<Argument, CommandSender, Source> getPlatform() {
		return this.platform;
	}

	/**
	 * Generates a command to be registered by the CommandAPI.
	 * 
	 * @param args       set of ordered argument pairs which contain the prompt text
	 *                   and their argument types
	 * @param executor   code to be ran when the command is executed
	 * @param converted  True if this command is being converted from another plugin, and false otherwise
	 * @return a brigadier command which is registered internally
	 */
	public Command<Source> generateCommand(Argument[] args, CommandAPIExecutor<CommandSender, AbstractCommandSender<? extends CommandSender>> executor, boolean converted) {

		// Generate our command from executor
		return cmdCtx -> {
			AbstractCommandSender<? extends CommandSender> sender = platform.getSenderForCommand(cmdCtx, executor.isForceNative());
			CommandArguments commandArguments = argsToCommandArgs(cmdCtx, args);
			ExecutionInfo<CommandSender, AbstractCommandSender<? extends CommandSender>> executionInfo = new ExecutionInfo<>() {
				@Override
				public CommandSender sender() {
					return sender.getSource();
				}

				@Override
				public AbstractCommandSender<? extends CommandSender> senderWrapper() {
					return sender;
				}

				@Override
				public CommandArguments args() {
					return commandArguments;
				}
			};
			if (converted) {
				int resultValue = 0;

				// Return a String[] of arguments for converted commands
				String[] argsAndCmd = cmdCtx.getRange().get(cmdCtx.getInput()).split(" ");
				String[] result = new String[argsAndCmd.length - 1];
				ExecutionInfo<CommandSender, AbstractCommandSender<? extends CommandSender>> convertedExecutionInfo = new ExecutionInfo<>() {
					@Override
					public CommandSender sender() {
						return sender.getSource();
					}

					@Override
					public AbstractCommandSender<? extends CommandSender> senderWrapper() {
						return sender;
					}

					@Override
					public CommandArguments args() {
						return new CommandArguments(result, new LinkedHashMap<>(), result, new LinkedHashMap<>(), "/" + cmdCtx.getInput());
					}
				};

				System.arraycopy(argsAndCmd, 1, result, 0, argsAndCmd.length - 1);

				// As stupid as it sounds, it's more performant and safer to use
				// a List<?>[] instead of a List<List<?>>, due to NPEs and AIOOBEs.
				@SuppressWarnings("unchecked")
				List<String>[] entityNamesForArgs = new List[args.length];
				for (int i = 0; i < args.length; i++) {
					entityNamesForArgs[i] = args[i].getEntityNames(commandArguments.get(i));
				}
				List<List<String>> product = CartesianProduct.getDescartes(Arrays.asList(entityNamesForArgs));

				// These objects in obj are List<String>
				for (List<String> strings : product) {
					// We assume result.length == strings.size
					if (result.length == strings.size()) {
						for (int i = 0; i < result.length; i++) {
							if (strings.get(i) != null) {
								result[i] = strings.get(i);
							}
						}
					}
					resultValue += executor.execute(convertedExecutionInfo);
				}

				return resultValue;
			} else {
				return executor.execute(executionInfo);
			}
		};
	}

	/**
	 * Converts the List&lt;Argument> into a {@link CommandArguments} for command execution
	 * 
	 * @param cmdCtx the command context that will execute this command
	 * @param args   the map of strings to arguments
	 * @return an CommandArguments object which can be used in (sender, args) ->
	 * @throws CommandSyntaxException
	 */
	CommandArguments argsToCommandArgs(CommandContext<Source> cmdCtx, Argument[] args)
			throws CommandSyntaxException {
		// Array for arguments for executor
		List<Object> argList = new ArrayList<>();

		// LinkedHashMap for arguments for executor
		Map<String, Object> argsMap = new LinkedHashMap<>();

		// List for raw arguments
		List<String> rawArguments = new ArrayList<>();

		// LinkedHashMap for raw arguments
		Map<String, String> rawArgumentsMap = new LinkedHashMap<>();

		// Populate array
		for (Argument argument : args) {
			if (argument.isListed()) {
				Object parsedArgument = parseArgument(cmdCtx, argument.getNodeName(), argument, new CommandArguments(argList.toArray(), argsMap, rawArguments.toArray(new String[0]), rawArgumentsMap, "/" + cmdCtx.getInput()));

				// Add the parsed argument
				argList.add(parsedArgument);
				argsMap.put(argument.getNodeName(), parsedArgument);

				// Add the raw argument
				String rawArgumentString = getRawArgumentInput(cmdCtx, argument.getNodeName());

				rawArguments.add(rawArgumentString);
				rawArgumentsMap.put(argument.getNodeName(), rawArgumentString);
			}
		}

		return new CommandArguments(argList.toArray(), argsMap, rawArguments.toArray(new String[0]), rawArgumentsMap, "/" + cmdCtx.getInput());
	}

	/**
	 * Parses an argument and converts it into its object
	 *
	 * @param cmdCtx the command context
	 * @param key    the key (declared in arguments)
	 * @param value  the value (the argument declared in arguments)
	 * @return the Argument's corresponding object
	 * @throws CommandSyntaxException when the input for the argument isn't formatted correctly
	 */
	Object parseArgument(CommandContext<Source> cmdCtx, String key, Argument value, CommandArguments previousArgs) throws CommandSyntaxException {
		if (value.isListed()) {
			return value.parseArgument(cmdCtx, key, previousArgs);
		} else {
			return null;
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// SECTION: Permissions //
	//////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * This permission generation setup ONLY works iff:
	 * <ul>
	 * <li>You register the parent permission node FIRST.</li>
	 * <li>Example:<br>
	 * /mycmd - permission node: <code>my.perm</code> <br>
	 * /mycmd &lt;arg> - permission node: <code>my.perm.other</code></li>
	 * </ul>
	 *
	 * The <code>my.perm.other</code> permission node is revoked for the COMMAND
	 * REGISTRATION, however:
	 * <ul>
	 * <li>The permission node IS REGISTERED.</li>
	 * <li>The permission node, if used for an argument (as in this case), will be
	 * used for suggestions for said argument</li>
	 * </ul>
	 * 
	 * @param requirements An arbitrary additional check to perform on the CommandSender
	 *                        after the permissions check
	 */
	Predicate<Source> generatePermissions(String commandName, CommandPermission permission, Predicate<CommandSender> requirements, String namespace) {
		// If namespace:commandName was already registered, always use the first permission used
		String namespacedCommand = namespace.isEmpty()
			? commandName.toLowerCase()
			: namespace.toLowerCase() + ":" + commandName.toLowerCase();
		if (registeredPermissions.containsKey(namespacedCommand)) {
			permission = registeredPermissions.get(namespacedCommand);
		} else {
			registeredPermissions.put(namespacedCommand, permission);
			// The first command to be registered determines the permission for the `commandName` version of the command
			registeredPermissions.putIfAbsent(commandName.toLowerCase(), permission);
		}

		// Register permission to the platform's registry, if both exist
		permission.getPermission().ifPresent(platform::registerPermission);

		// Generate predicate for the permission and requirement check
		CommandPermission finalPermission = permission;
		return (Source css) -> permissionCheck(platform.getCommandSenderFromCommandSource(css), finalPermission,
				requirements);
	}

	/**
	 * Checks if a sender has a given permission. If the {@code sender}
	 * is {@code null}, this will always return {@code true}.
	 * 
	 * @param sender     the sender to check permissions of
	 * @param permission the CommandAPI CommandPermission permission to check
	 * @return true if the sender satisfies the provided permission
	 */
	static <CommandSender> boolean permissionCheck(AbstractCommandSender<? extends CommandSender> sender, CommandPermission permission, Predicate<CommandSender> requirements) {
		if (sender == null) {
			// We only expect the sender to be null when the sender is actually the
			//  `io.papermc.paper.brigadier.NullCommandSender` (See CommandAPIBukkit#wrapCommandSender).
			//  In this case, we always allow the sender to access the command.
			return true;
		}

		boolean satisfiesPermissions;
		if (permission.equals(CommandPermission.NONE)) {
			// No permission set
			satisfiesPermissions = true;
		} else if (permission.equals(CommandPermission.OP)) {
			// Op permission set
			satisfiesPermissions = sender.isOp();
		} else {
			final Optional<String> optionalPerm = permission.getPermission();
			if (optionalPerm.isPresent()) {
				satisfiesPermissions = sender.hasPermission(optionalPerm.get());
			} else {
				satisfiesPermissions = true;
			}
		}

		if (permission.isNegated()) {
			satisfiesPermissions = !satisfiesPermissions;
		}
		return satisfiesPermissions && requirements.test(sender.getSource());
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// SECTION: Registration //
	//////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * Expands multiliteral arguments and registers all expansions of
	 * MultiLiteralArguments throughout the provided command. Returns true if
	 * multiliteral arguments were present (and expanded) and returns false if
	 * multiliteral arguments were not present.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean expandMultiLiterals(CommandMetaData<CommandSender> meta, final Argument[] args,
			CommandAPIExecutor<CommandSender, AbstractCommandSender<? extends CommandSender>> executor, boolean converted, String namespace) {

		// "Expands" our MultiLiterals into Literals
		for (int index = 0; index < args.length; index++) {
			// Find the first multiLiteral in the for loop
			if (args[index] instanceof MultiLiteral) {
				MultiLiteral<? extends Argument> superArg = (MultiLiteral<? extends Argument>) args[index];

				String nodeName = superArg.instance().getNodeName();

				// Add all of its entries
				for (String literal: superArg.getLiterals()) {
					// TODO: We only expect nodeName to be null here because the constructor for a MultiLiteralArgument
					//  without a nodeName is currently deprecated but not removed. Once that constructor is removed,
					//  this `nodeName == null` statement can probably be removed as well
					Argument litArg = platform.newConcreteLiteralArgument(nodeName == null ? literal : nodeName, literal);

					litArg.setListed(superArg.instance().isListed())
						.withPermission(superArg.instance().getArgumentPermission())
						.withRequirement((Predicate) superArg.instance().getRequirements());

					// Reconstruct the list of arguments and place in the new literals
					Argument[] newArgs = Arrays.copyOf(args, args.length);
					newArgs[index] = litArg;
					register(meta, newArgs, executor, converted, namespace);
				}
				return true;
			}
		}
		return false;
	}

	// Prevent nodes of the same name but with different types:
	// allow /race invite<LiteralArgument> player<PlayerArgument>
	// disallow /race invite<LiteralArgument> player<EntitySelectorArgument>
	// Return true if conflict was present, otherwise return false
	private boolean hasCommandConflict(String commandName, Argument[] args, String argumentsAsString) {
		List<String[]> regArgs = new ArrayList<>();
		for (RegisteredCommand rCommand : registeredCommands) {
			if (rCommand.commandName().equals(commandName)) {
				for (String str : rCommand.argsAsStr()) {
					regArgs.add(str.split(":"));
				}
				// We just find the first entry that causes a conflict. If this
				// were some industry-level code, we would probably generate a
				// list of all commands first, then check for command conflicts
				// all in one go so we can display EVERY command conflict for
				// all commands, but this works perfectly and isn't important.
				break;
			}
		}
		for (int i = 0; i < args.length; i++) {
			// Avoid IAOOBEs and ensure all node names are the same
			if ((regArgs.size() == i && regArgs.size() < args.length) || (!regArgs.get(i)[0].equals(args[i].getNodeName()))) {
				break;
			}
			// This only applies to the last argument
			if (i == args.length - 1 && !regArgs.get(i)[1].equals(args[i].getClass().getSimpleName())) {
				// Command it conflicts with
				StringBuilder builder2 = new StringBuilder();
				for (String[] arg : regArgs) {
					builder2.append(arg[0]).append("<").append(arg[1]).append("> ");
				}

				CommandAPI.logError("""
					Failed to register command:

					  %s %s

					Because it conflicts with this previously registered command:

					  %s %s
					""".formatted(commandName, argumentsAsString, commandName, builder2.toString()));
				return true;
			}
		}
		return false;
	}

	// Links arg -> Executor
	private ArgumentBuilder<Source, ?> generateInnerArgument(Command<Source> command, Argument[] args) {
		Argument innerArg = args[args.length - 1];

		// Handle Literal arguments
		if (innerArg instanceof Literal) {
			@SuppressWarnings("unchecked")
			Literal<? extends Argument> literalArgument = (Literal<? extends Argument>) innerArg;
			return getLiteralArgumentBuilderArgument(literalArgument.getLiteral(), innerArg.getArgumentPermission(),
					innerArg.getRequirements()).executes(command);
		}

		// Handle arguments with built-in suggestion providers
		else if (innerArg instanceof CustomProvidedArgument customProvidedArg && innerArg.getOverriddenSuggestions().isEmpty()) {
			return getRequiredArgumentBuilderWithProvider(innerArg, args,
					platform.getSuggestionProvider(customProvidedArg.getSuggestionProvider())).executes(command);
		}

		// Handle every other type of argument
		else {
			return getRequiredArgumentBuilderDynamic(args, innerArg).executes(command);
		}
	}

	// Links arg1 -> arg2 -> ... argN -> innermostArgument
	private ArgumentBuilder<Source, ?> generateOuterArguments(ArgumentBuilder<Source, ?> innermostArgument, Argument[] args) {
		ArgumentBuilder<Source, ?> outer = innermostArgument;
		for (int i = args.length - 2; i >= 0; i--) {
			Argument outerArg = args[i];

			// Handle Literal arguments
			if (outerArg instanceof Literal) {
				@SuppressWarnings("unchecked")
				Literal<? extends Argument> literalArgument = (Literal<? extends Argument>) outerArg;
				outer = getLiteralArgumentBuilderArgument(literalArgument.getLiteral(),
					outerArg.getArgumentPermission(), outerArg.getRequirements()).then(outer);
			}

			// Handle arguments with built-in suggestion providers
			else if (outerArg instanceof CustomProvidedArgument customProvidedArg
					&& outerArg.getOverriddenSuggestions().isEmpty()) {
				outer = getRequiredArgumentBuilderWithProvider(outerArg, args,
						platform.getSuggestionProvider(customProvidedArg.getSuggestionProvider())).then(outer);
			}

			// Handle every other type of argument
			else {
				outer = getRequiredArgumentBuilderDynamic(args, outerArg).then(outer);
			}
		}
		return outer;
	}

	// Builds a command then registers it
	void register(CommandMetaData<CommandSender> meta, final Argument[] args,
			CommandAPIExecutor<CommandSender, AbstractCommandSender<? extends CommandSender>> executor, boolean converted, String namespace) {
		// "Expands" our MultiLiterals into Literals
		if (expandMultiLiterals(meta, args, executor, converted, namespace)) {
			return;
		}

		// Create the human-readable command syntax of arguments
		final String humanReadableCommandArgSyntax;
		{
			StringBuilder builder = new StringBuilder();
			for (Argument arg : args) {
				builder.append(arg.toString()).append(" ");
			}
			humanReadableCommandArgSyntax = builder.toString().trim();
		}

		// #312 Safeguard against duplicate node names. This only applies to
		// required arguments (i.e. not literal arguments)
		if(!checkForDuplicateArgumentNodeNames(args, humanReadableCommandArgSyntax, meta.commandName)) {
			return;
		}

		// Expand metaData into named variables
		String commandName = meta.commandName;
		CommandPermission permission = meta.permission;
		String[] aliases = meta.aliases;
		Predicate<CommandSender> requirements = meta.requirements;
		Optional<String> shortDescription = meta.shortDescription;
		Optional<String> fullDescription = meta.fullDescription;
		Optional<String[]> usageDescription = meta.usageDescription;
		Optional<Object> helpTopic = meta.helpTopic;

		// Handle command conflicts
		boolean hasRegisteredCommand = false;
		for (int i = 0, size = registeredCommands.size(); i < size && !hasRegisteredCommand; i++) {
			hasRegisteredCommand |= registeredCommands.get(i).commandName().equals(commandName);
		}

		if (hasRegisteredCommand && hasCommandConflict(commandName, args, humanReadableCommandArgSyntax)) {
			return;
		}

		List<String> argumentsString = new ArrayList<>();
		for (Argument arg : args) {
			argumentsString.add(arg.getNodeName() + ":" + arg.getClass().getSimpleName());
		}
		RegisteredCommand registeredCommandInformation = new RegisteredCommand(commandName, argumentsString, List.of(args), shortDescription,
			fullDescription, usageDescription, helpTopic, aliases, permission, namespace);
		registeredCommands.add(registeredCommandInformation);

		platform.preCommandRegistration(commandName);

		String namespacedCommandName = namespace.isEmpty() ? commandName : namespace +  ":" + commandName;
		CommandAPI.logInfo("Registering command /" + namespacedCommandName + " " + humanReadableCommandArgSyntax);

		// Generate the actual command
		Command<Source> command = generateCommand(args, executor, converted);

		/*
		 * The innermost argument needs to be connected to the executor. Then that
		 * argument needs to be connected to the previous argument etc. Then the first
		 * argument needs to be connected to the command name, so we get: CommandName ->
		 * Args1 -> Args2 -> ... -> ArgsN -> Executor
		 */
		LiteralCommandNode<Source> resultantNode;
		List<LiteralCommandNode<Source>> aliasNodes = new ArrayList<>();
		if (args.length == 0) {
			// Link command name to the executor
			resultantNode = platform.registerCommandNode(getLiteralArgumentBuilder(commandName)
				.requires(generatePermissions(commandName, permission, requirements, namespace)).executes(command), namespace);

			// Register aliases
			for (String alias : aliases) {
				CommandAPI.logInfo("Registering alias /" + alias + " -> " + resultantNode.getName());
				aliasNodes.add(platform.registerCommandNode(getLiteralArgumentBuilder(alias)
					.requires(generatePermissions(alias, permission, requirements, namespace)).executes(command), namespace));
			}
		} else {

			// Generate all of the arguments, following each other and finally linking to
			// the executor
			ArgumentBuilder<Source, ?> commandArguments = generateOuterArguments(
				generateInnerArgument(command, args), args);

			// Link command name to first argument and register
			resultantNode = platform.registerCommandNode(getLiteralArgumentBuilder(commandName)
				.requires(generatePermissions(commandName, permission, requirements, namespace)).then(commandArguments), namespace);

			// Register aliases
			for (String alias : aliases) {
				if (CommandAPI.getConfiguration().hasVerboseOutput()) {
					CommandAPI.logInfo("Registering alias /" + alias + " -> " + resultantNode.getName());
				}

				aliasNodes.add(platform.registerCommandNode(getLiteralArgumentBuilder(alias)
					.requires(generatePermissions(alias, permission, requirements, namespace)).then(commandArguments), namespace));
			}
		}

//		TODO: Do something when ambiguities are found
//		platform.getBrigadierDispatcher().findAmbiguities(
//			(CommandNode<Source> parent,
//			 CommandNode<Source> child,
//			 CommandNode<Source> sibling,
//			 Collection<String> inputs) -> {
//				if(resultantNode.equals(parent)) {
//					// Byeeeeeeeeeeeeeeeeeeeee~
//				}
//			});
		// We never know if this is "the last command" and we want dynamic (even if
		// partial) command registration. Generate the dispatcher file!
		writeDispatcherToFile();

		platform.postCommandRegistration(registeredCommandInformation, resultantNode, aliasNodes);
	}

	/**
	 * Checks for duplicate argument node names and logs them as errors in the
	 * console
	 * 
	 * @param args                          the list of arguments
	 * @param humanReadableCommandArgSyntax the human readable command argument
	 *                                      syntax
	 * @param commandName                   the name of the command
	 * @return true if there were no duplicate argument node names, false otherwise
	 */
	private boolean checkForDuplicateArgumentNodeNames(Argument[] args, String humanReadableCommandArgSyntax, String commandName) {
		Set<String> argumentNames = new HashSet<>();
		for (Argument arg : args) {
			// We shouldn't find MultiLiteralArguments at this point, only LiteralArguments
			if (!(arg instanceof Literal)) {
				if (argumentNames.contains(arg.getNodeName())) {
					CommandAPI.logError("""
						Failed to register command:

						  %s %s

						Because the following argument shares the same node name as another argument:

						  %s
						""".formatted(commandName, humanReadableCommandArgSyntax, arg.toString()));
					return false;
				} else {
					argumentNames.add(arg.getNodeName());
				}
			}
		}
		return true;
	}
	
	public void writeDispatcherToFile() {
		File file = CommandAPI.getConfiguration().getDispatcherFile();
		if (file != null) {
			try {
				file.getParentFile().mkdirs();
				if (file.createNewFile()) {
					// Cool, we've created the file
					assert true;
				}
			} catch (IOException e) {
				CommandAPI.logError("Failed to create the required directories for " + file.getName() + ": " + e.getMessage());
				return;
			}

			try {
				platform.createDispatcherFile(file, platform.getBrigadierDispatcher());
			} catch (IOException e) {
				CommandAPI.logError("Failed to write command registration info to " + file.getName() + ": " + e.getMessage());
			}
		}
	}

	<CommandSource> LiteralCommandNode<CommandSource> namespaceNode(LiteralCommandNode<CommandSource> original, String namespace) {
		// Adapted from a section of `CraftServer#syncCommands`
		LiteralCommandNode<CommandSource> clone = new LiteralCommandNode<>(
			namespace + ":" + original.getLiteral(),
			original.getCommand(),
			original.getRequirement(),
			original.getRedirect(),
			original.getRedirectModifier(),
			original.isFork()
		);

		for (CommandNode<CommandSource> child : original.getChildren()) {
			clone.addChild(child);
		}
		return clone;
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// SECTION: Argument Builders //
	//////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a literal for a given name.
	 * 
	 * @param commandName the name of the literal to create
	 * @return a brigadier LiteralArgumentBuilder representing a literal
	 */
	LiteralArgumentBuilder<Source> getLiteralArgumentBuilder(String commandName) {
		return LiteralArgumentBuilder.literal(commandName);
	}

	/**
	 * Creates a literal for a given name that requires a specified permission.
	 * 
	 * @param commandName the name fo the literal to create
	 * @param permission  the permission required to use this literal
	 * @return a brigadier LiteralArgumentBuilder representing a literal
	 */
	LiteralArgumentBuilder<Source> getLiteralArgumentBuilderArgument(String commandName, CommandPermission permission, Predicate<CommandSender> requirements) {
		LiteralArgumentBuilder<Source> builder = LiteralArgumentBuilder.literal(commandName);
		return builder.requires((Source css) -> permissionCheck(platform.getCommandSenderFromCommandSource(css),
				permission, requirements));
	}

	// Gets a RequiredArgumentBuilder for a DynamicSuggestedStringArgument
	RequiredArgumentBuilder<Source, ?> getRequiredArgumentBuilderDynamic(final Argument[] args, Argument argument) {

		final SuggestionProvider<Source> suggestions;

		if (argument.getOverriddenSuggestions().isPresent()) {
			suggestions = toSuggestions(argument, args, true);
		} else if (argument.getIncludedSuggestions().isPresent()) {
			// TODO(#317): Merge the suggestions included here instead?
			suggestions = (cmdCtx, builder) -> argument.getRawType().listSuggestions(cmdCtx, builder);
		} else {
			suggestions = null;
		}

		return getRequiredArgumentBuilderWithProvider(argument, args, suggestions);
	}

	// Gets a RequiredArgumentBuilder for an argument, given a SuggestionProvider
	RequiredArgumentBuilder<Source, ?> getRequiredArgumentBuilderWithProvider(Argument argument, Argument[] args, SuggestionProvider<Source> provider) {
		SuggestionProvider<Source> newSuggestionsProvider = provider;

		// If we have suggestions to add, combine provider with the suggestions
		if (argument.getIncludedSuggestions().isPresent() && argument.getOverriddenSuggestions().isEmpty()) {
			SuggestionProvider<Source> addedSuggestions = toSuggestions(argument, args, false);

			newSuggestionsProvider = (cmdCtx, builder) -> {
				// Heavily inspired by CommandDispatcher#listSuggestions, with combining
				// multiple CompletableFuture<Suggestions> into one.

				CompletableFuture<Suggestions> addedSuggestionsFuture = addedSuggestions.getSuggestions(cmdCtx,
						builder);
				CompletableFuture<Suggestions> providerSuggestionsFuture = provider.getSuggestions(cmdCtx, builder);
				CompletableFuture<Suggestions> result = new CompletableFuture<>();
				CompletableFuture.allOf(addedSuggestionsFuture, providerSuggestionsFuture).thenRun(() -> {
					List<Suggestions> suggestions = new ArrayList<>();
					suggestions.add(addedSuggestionsFuture.join());
					suggestions.add(providerSuggestionsFuture.join());
					result.complete(Suggestions.merge(cmdCtx.getInput(), suggestions));
				});
				return result;
			};
		}

		RequiredArgumentBuilder<Source, ?> requiredArgumentBuilder = RequiredArgumentBuilder
				.argument(argument.getNodeName(), argument.getRawType());

		return requiredArgumentBuilder.requires(css -> permissionCheck(platform.getCommandSenderFromCommandSource(css),
				argument.getArgumentPermission(), argument.getRequirements())).suggests(newSuggestionsProvider);
	}

	CommandArguments generatePreviousArguments(CommandContext<Source> context, Argument[] args, String nodeName)
			throws CommandSyntaxException {
		// Populate Object[], which is our previously filled arguments
		List<Object> previousArguments = new ArrayList<>();

		// LinkedHashMap for arguments
		Map<String, Object> argsMap = new LinkedHashMap<>();

		// List for raw arguments
		List<String> rawArguments = new ArrayList<>();

		// LinkedHashMap for raw arguments
		Map<String, String> rawArgumentsMap = new LinkedHashMap<>();

		for (Argument arg : args) {
			if (arg.getNodeName().equals(nodeName) && !(arg instanceof Literal)) {
				break;
			}

			Object result;
			try {
				result = parseArgument(context, arg.getNodeName(), arg, new CommandArguments(previousArguments.toArray(), argsMap, rawArguments.toArray(new String[0]), rawArgumentsMap, "/" + context.getInput()));
			} catch (IllegalArgumentException e) {
				/*
				 * Redirected commands don't parse previous arguments properly. Simplest way to
				 * determine what we should do is simply set it to null, since there's nothing
				 * else we can do. I thought about letting this simply be an empty array, but
				 * then it's even more annoying to deal with - I wouldn't expect an array of
				 * size n to suddenly, randomly be 0, but I would expect random NPEs because
				 * let's be honest, this is Java we're dealing with.
				 */
				result = null;
			}
			if (arg.isListed()) {
				// Add the parsed argument
				previousArguments.add(result);
				argsMap.put(arg.getNodeName(), result);

				// Add the raw argument
				String rawArgumentString = getRawArgumentInput(context, arg.getNodeName());

				rawArguments.add(rawArgumentString);
				rawArgumentsMap.put(arg.getNodeName(), rawArgumentString);
			}
		}
		return new CommandArguments(previousArguments.toArray(), argsMap, rawArguments.toArray(new String[0]), rawArgumentsMap, "/" + context.getInput());
	}

	SuggestionProvider<Source> toSuggestions(Argument theArgument, Argument[] args,
			boolean overrideSuggestions) {
		return (CommandContext<Source> context, SuggestionsBuilder builder) -> {
			// Construct the suggestion info
			SuggestionInfo<CommandSender> suggestionInfo = new SuggestionInfo<>(platform.getCommandSenderFromCommandSource(context.getSource()).getSource(),
					generatePreviousArguments(context, args, theArgument.getNodeName()), builder.getInput(), builder.getRemaining());

			// Get the suggestions
			Optional<ArgumentSuggestions<CommandSender>> suggestionsToAddOrOverride = overrideSuggestions
					? theArgument.getOverriddenSuggestions()
					: theArgument.getIncludedSuggestions();
			return suggestionsToAddOrOverride.orElse(ArgumentSuggestions.empty()).suggest(suggestionInfo, builder);
		};
	}

	/////////////////////////
	// SECTION: Reflection //
	/////////////////////////

	/**
	 * Caches a field using reflection if it is not already cached, then return the
	 * field of a given class. This will also make the field accessible.
	 * 
	 * @param clazz the class where the field is declared
	 * @param name  the name of the field
	 * @return a Field reference
	 */
	public static Field getField(Class<?> clazz, String name) {
		return getField(clazz, name, name);
	}

	/**
	 * Caches a field using reflection if it is not already cached, then return the
	 * field of a given class. This will also make the field accessible.
	 * 
	 * @param clazz the class where the field is declared
	 * @param name  the name of the field
	 * @param mojangMappedName the name of a field under Mojang mappings
	 * @return a Field reference
	 */
	public static Field getField(Class<?> clazz, String name, String mojangMappedName) {
		ClassCache key = new ClassCache(clazz, name, mojangMappedName);
		if (FIELDS.containsKey(key)) {
			return FIELDS.get(key);
		} else {
			Field result;
			try {
				result = clazz.getDeclaredField(SafeVarHandle.USING_MOJANG_MAPPINGS ? mojangMappedName : name);
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
				return null;
			}
			result.setAccessible(true);
			FIELDS.put(key, result);
			return result;
		}
	}

	//////////////////////////////
	// SECTION: Private classes //
	//////////////////////////////

	/**
	 * Class to store cached methods and fields
	 * <p>
	 * This is required because each key is made up of a class and a field or method
	 * name
	 */
	private record ClassCache(Class<?> clazz, String name, String mojangMappedName) {
	}

	/**
	 * A class to compute the Cartesian product of a number of lists. Source:
	 * https://www.programmersought.com/article/86195393650/
	 */
	private static final class CartesianProduct {

		// Shouldn't be instantiated
		private CartesianProduct() {
		}

		/**
		 * Returns the Cartesian product of a list of lists
		 * 
		 * @param <T>  the underlying type of the list of lists
		 * @param list the list to calculate the Cartesian product of
		 * @return a List of lists which represents the Cartesian product of all
		 *         elements of the input
		 */
		public static <T> List<List<T>> getDescartes(List<List<T>> list) {
			List<List<T>> returnList = new ArrayList<>();
			descartesRecursive(list, 0, returnList, new ArrayList<T>());
			return returnList;
		}

		/**
		 * Recursive implementation Principle: traverse sequentially from 0 of the
		 * original list to the end
		 * 
		 * @param <T>          the underlying type of the list of lists
		 * @param originalList original list
		 * @param position     The position of the current recursion in the original
		 *                     list
		 * @param returnList   return result
		 * @param cacheList    temporarily saved list
		 */
		private static <T> void descartesRecursive(List<List<T>> originalList, int position,
				List<List<T>> returnList, List<T> cacheList) {
			List<T> originalItemList = originalList.get(position);
			for (int i = 0; i < originalItemList.size(); i++) {
				// The last one reuses cacheList to save memory
				List<T> childCacheList = (i == originalItemList.size() - 1) ? cacheList : new ArrayList<>(cacheList);
				childCacheList.add(originalItemList.get(i));
				if (position == originalList.size() - 1) {// Exit recursion to the end
					returnList.add(childCacheList);
					continue;
				}
				descartesRecursive(originalList, position + 1, returnList, childCacheList);
			}
		}

	}
}
