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

import com.google.common.io.Files;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import dev.jorel.commandapi.arguments.AbstractArgument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomProvidedArgument;
import dev.jorel.commandapi.commandnodes.NodeTypeSerializer;
import dev.jorel.commandapi.exceptions.CommandConflictException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.ExecutionInfo;
import dev.jorel.commandapi.preprocessor.RequireField;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * The "brains" behind the CommandAPI.
 * Handles command registration
 *
 * @param <Argument>      The implementation of AbstractArgument being used
 * @param <CommandSender> The class for running platform commands
 * @param <Source>        The class for running Brigadier commands
 */
@RequireField(in = CommandContext.class, name = "arguments", ofType = Map.class)
@RequireField(in = CommandNode.class, name = "children", ofType = Map.class)
@RequireField(in = CommandNode.class, name = "literals", ofType = Map.class)
@RequireField(in = CommandNode.class, name = "arguments", ofType = Map.class)
public class CommandAPIHandler<Argument
/// @cond DOX
extends AbstractArgument<?, ?, Argument, CommandSender>
/// @endcond
, CommandSender, Source> {
	// TODO: Need to ensure this can be safely "disposed of" when we're done (e.g. on reloads).
	// I hiiiiiiighly doubt we're storing class caches of classes that can be unloaded at runtime,
	// but this IS a generic class caching system and we don't want derpy memory leaks
	private static final Map<ClassCache, Field> FIELDS;

	private static final SafeVarHandle<CommandContext<?>, Map<String, ParsedArgument<?, ?>>> commandContextArguments;
	// I think these maps need to be raw since the parameter Source is inaccessible, but we want to cast to that
	private static final SafeVarHandle<CommandNode<?>, Map> commandNodeChildren;
	private static final SafeVarHandle<CommandNode<?>, Map> commandNodeLiterals;
	private static final SafeVarHandle<CommandNode<?>, Map> commandNodeArguments;

	// Compute all var handles all in one go so we don't do this during main server runtime
	static {
		FIELDS = new HashMap<>();

		commandContextArguments = SafeVarHandle.ofOrNull(CommandContext.class, "arguments", Map.class);
		commandNodeChildren = SafeVarHandle.ofOrNull(CommandNode.class, "children", Map.class);
		commandNodeArguments = SafeVarHandle.ofOrNull(CommandNode.class, "arguments", Map.class);

		SafeVarHandle<CommandNode<?>, Map> literals;
		try {
			literals = SafeVarHandle.of(CommandNode.class, "literals", "literals", Map.class);
		} catch (ReflectiveOperationException ignored) {
			// CommandNode.literals does not exist on Velocity, so we expect this to happen then
			literals = null;
		}
		commandNodeLiterals = literals;
	}

	final CommandAPIPlatform<Argument, CommandSender, Source> platform;
	final Map<String, RegisteredCommand<CommandSender>> registeredCommands; // Keep track of what has been registered for type checking
	static final Pattern NAMESPACE_PATTERN = Pattern.compile("[0-9a-z_.-]+");

	private static CommandAPIHandler<?, ?, ?> instance;

	////////////////////
	// SECTION: Setup //
	////////////////////

	protected CommandAPIHandler(CommandAPIPlatform<Argument, CommandSender, Source> platform) {
		this.platform = platform;
		this.registeredCommands = new LinkedHashMap<>(); // This should be a LinkedHashMap to preserve insertion order

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
	}

	public void onDisable() {
		platform.onDisable();
		CommandAPIHandler.resetInstance();
	}

	private static void resetInstance() {
		CommandAPIHandler.instance = null;
	}

	public static <Argument extends AbstractArgument<?, ?, Argument, CommandSender>, CommandSender, Source>
	CommandAPIHandler<Argument, CommandSender, Source> getInstance() {
		if (CommandAPIHandler.instance != null) {
			return (CommandAPIHandler<Argument, CommandSender, Source>) CommandAPIHandler.instance;
		} else {
			throw new IllegalStateException("Tried to access CommandAPIHandler instance, but it was null! Are you using CommandAPI features before calling CommandAPI#onLoad?");
		}
	}

	public CommandAPIPlatform<Argument, CommandSender, Source> getPlatform() {
		return this.platform;
	}

	////////////////////////////////
	// SECTION: Creating commands //
	////////////////////////////////

	/**
	 * Registers a command with a given namespace. This is intended to be called by {@link ExecutableCommand#register(String)}
	 *
	 * @param command   The command to register.
	 * @param namespace The namespace of this command. This cannot be null, and each platform may impose additional requirements. 
	 *                  See {@link CommandAPIPlatform#validateNamespace(ExecutableCommand, String)}.
	 * @throws NullPointerException if the namespace is null.
	 */
	public void registerCommand(ExecutableCommand<?, CommandSender> command, String namespace) {
		// Validate parameters
		if (namespace == null) {
			throw new NullPointerException("Parameter 'namespace' was null when registering command /" + command.getName() + "!");
		}
		namespace = platform.validateNamespace(command, namespace);

		// Do plaform-specific pre-registration tasks
		platform.preCommandRegistration(command.getName());

		// Generate command information
		ExecutableCommand.CommandInformation<CommandSender, Source> commandInformation = command.createCommandInformation(namespace);

		LiteralCommandNode<Source> resultantNode = commandInformation.rootNode();
		List<LiteralCommandNode<Source>> aliasNodes = commandInformation.aliasNodes();
		RegisteredCommand<CommandSender> registeredCommand = commandInformation.command();

		// Log the commands being registered
		for (List<String> argsAsStr : registeredCommand.rootNode().argsAsStr()) {
			CommandAPI.logInfo("Registering command /" + String.join(" ", argsAsStr));
		}

		// Handle command conflicts
		ensureNoCommandConflict(resultantNode);

		// Register rootNode
		platform.registerCommandNode(resultantNode, namespace);

		// Register aliasNodes
		for (LiteralCommandNode<Source> aliasNode : aliasNodes) {
			platform.registerCommandNode(aliasNode, namespace);
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

		// Merge RegisteredCommand into map
		BiFunction<String, RegisteredCommand<CommandSender>, RegisteredCommand<CommandSender>> mergeRegisteredCommands = 
			(key, value) -> value == null ? registeredCommand : value.mergeCommandInformation(registeredCommand);
		
		if (registeredCommand.namespace().isEmpty()) {
			registeredCommands.compute(registeredCommand.commandName(), mergeRegisteredCommands);
		} else {
			registeredCommands.compute(registeredCommand.commandName(), (key, value) -> value == null ? 
				registeredCommand.copyWithEmptyNamespace() : 
				value.mergeCommandInformation(registeredCommand)
			);
			registeredCommands.compute(registeredCommand.namespace() + ":" + registeredCommand.commandName(), mergeRegisteredCommands);
		}

		// Do platform-specific post-registration tasks
		platform.postCommandRegistration(registeredCommand, resultantNode, aliasNodes);
	}

	/**
	 * When a command is registered, all its nodes get merged into the existing nodes in the Brigadier CommandDispatcher.
	 * Brigadier's default behavior is to <a href=https://github.com/Mojang/brigadier/blob/b92c420b2a292dd5c20f6adfafff5e21b9835c6d/src/main/java/com/mojang/brigadier/tree/CommandNode.java#L77>
	 * override executors</a>. This method will throw an exception if this overriding is going to happen so we can avoid 
	 * messing up previously registered commands.
	 * 
	 * @param nodeToRegister The {@link CommandNode} that is going to be regsitered.
	 * @throws CommandConflictException if registering the given node would cause an executor set up by a previous command to be overridden.
	 */
	public void ensureNoCommandConflict(CommandNode<Source> nodeToRegister) {
		ensureNoCommandConflict(nodeToRegister, platform.getBrigadierDispatcher().getRoot(), List.of());
	}
	
	private void ensureNoCommandConflict(CommandNode<Source> nodeToRegister, CommandNode<Source> targetLocation, List<String> pathSoFar) {
		CommandNode<Source> mergeTarget = targetLocation.getChild(nodeToRegister.getName());

		if (mergeTarget == null) return; // The `nodeToRegister` does not already exist, no conflict possible

		// Add node to path
		List<String> path = new ArrayList<>(pathSoFar);
		path.add(nodeToRegister.getName());

		if (nodeToRegister.getCommand() != null && mergeTarget.getCommand() != null) {
			// We just find the first entry that causes a conflict. If this
			// were some industry-level code, we would probably generate a
			// list of all commands first, then check for command conflicts
			// all in one go so we can display EVERY command conflict for
			// all commands, but this works perfectly and isn't important.
			throw new CommandConflictException(path);
		}

		// Ensure children do not conflict
		for (CommandNode<Source> child : nodeToRegister.getChildren()) {
			ensureNoCommandConflict(child, mergeTarget, path);
		}
	}

	/**
	 * Generates a Brigadier {@link Command} using the given CommandAPI objects.
	 *
	 * @param args     A list of Arguments that have been defined for this command.
	 * @param executor Code to run when the command is executed.
	 * @return A Brigadier Command object that runs the given execution with the given arguments as input.
	 */
	public Command<Source> generateBrigadierCommand(List<Argument> args, CommandAPIExecutor<CommandSender> executor) {
		// We need to make sure our arguments list is never changed
		//  If we just used the reference to the list, the caller might add arguments that aren't actually previous
		//  arguments for this suggestion node, and we would be confused because the new arguments don't exist
		List<Argument> immutableArguments = List.copyOf(args);
		// Generate our command from executor
		return cmdCtx -> {
			// Construct the execution info
			CommandSender sender = platform.getCommandSenderFromCommandSource(cmdCtx.getSource());
			CommandArguments commandArguments = argsToCommandArgs(cmdCtx, immutableArguments);

			ExecutionInfo<CommandSender, Source> executionInfo = new ExecutionInfo<>(sender, commandArguments, cmdCtx);

			// Apply the executor
			return executor.execute(executionInfo);
		};
	}

	/**
	 * Generates a Brigadier {@link SuggestionProvider} using the given CommandAPI objects.
	 *
	 * @param previousArguments A list of Arguments that came before the argument using these suggestions. These arguments
	 *                             will be available in the {@link SuggestionInfo} when providing suggestions.
	 * @param argument The argument to give suggestions for.
	 * @return A Brigadier SuggestionProvider object that generates suggestions for the given argument with the previous
	 * arguments as input, or null if there are no suggestions for the given argument.
	 */
	public SuggestionProvider<Source> generateBrigadierSuggestions(List<Argument> previousArguments, Argument argument) {
		// Overriding suggestions take precedence
		Optional<ArgumentSuggestions<CommandSender>> overriddenSuggestions = argument.getOverriddenSuggestions();
		if (overriddenSuggestions.isPresent()) {
			return generateBrigadierSuggestions(previousArguments, overriddenSuggestions.get());
		}

		// Included suggestions add on to whatever "default" suggestions exist
		Optional<ArgumentSuggestions<CommandSender>> includedSuggestions = argument.getIncludedSuggestions();
		if (includedSuggestions.isPresent()) {
			// Insert additional defined suggestions
			SuggestionProvider<Source> defaultSuggestions;
			if (argument instanceof CustomProvidedArgument cPA) {
				defaultSuggestions = platform.getSuggestionProvider(cPA.getSuggestionProvider());
			} else {
				defaultSuggestions = argument.getRawType()::listSuggestions;
			}

			SuggestionProvider<Source> suggestionsToAdd = generateBrigadierSuggestions(previousArguments, includedSuggestions.get());

			return (cmdCtx, builder) -> {
				// Heavily inspired by CommandDispatcher#getCompletionSuggestions, with combining
				// multiple CompletableFuture<Suggestions> into one.
				CompletableFuture<Suggestions> defaultSuggestionsFuture = defaultSuggestions.getSuggestions(cmdCtx, builder);
				CompletableFuture<Suggestions> includedSuggestionsFuture = suggestionsToAdd.getSuggestions(cmdCtx, builder);

				CompletableFuture<Suggestions> result = new CompletableFuture<>();
				CompletableFuture.allOf(defaultSuggestionsFuture, includedSuggestionsFuture).thenRun(() -> {
					List<Suggestions> suggestions = new ArrayList<>();
					suggestions.add(defaultSuggestionsFuture.join());
					suggestions.add(includedSuggestionsFuture.join());
					result.complete(Suggestions.merge(cmdCtx.getInput(), suggestions));
				});
				return result;
			};
		}

		// Custom provided arguments
		if (argument instanceof CustomProvidedArgument cPA) {
			return platform.getSuggestionProvider(cPA.getSuggestionProvider());
		}

		// Calling `RequiredArgumentBuilder.suggests(null)` makes it so no custom suggestions are given, so this makes sense
		return null;
	}

	/**
	 * Generates a Brigadier {@link SuggestionProvider} using the given CommandAPI objects.
	 *
	 * @param previousArguments A list of Arguments that came before the argument using these suggestions. These arguments
	 *                          will be available in the {@link SuggestionInfo} when providing suggestions.
	 * @param suggestions       An {@link ArgumentSuggestions} object that should be used to generate the suggestions.
	 * @return A Brigadier SuggestionProvider object that generates suggestions using with the given arguments as input.
	 */
	public SuggestionProvider<Source> generateBrigadierSuggestions(List<Argument> previousArguments, ArgumentSuggestions<CommandSender> suggestions) {
		// We need to make sure our arguments list is never changed
		//  If we just used the reference to the list, the caller might add arguments that aren't actually previous
		//  arguments for this suggestion node, and we would be confused because the new arguments don't exist
		List<Argument> immutableArguments = List.copyOf(previousArguments);
		return (context, builder) -> {
			// Construct the suggestion info
			SuggestionInfo<CommandSender> suggestionInfo = new SuggestionInfo<>(
				platform.getCommandSenderFromCommandSource(context.getSource()),
				argsToCommandArgs(context, immutableArguments), builder.getInput(), builder.getRemaining()
			);

			// Apply the suggestions
			return suggestions.suggest(suggestionInfo, builder);
		};
	}

	/**
	 * Generates a {@link Predicate} that evaluates a Brigadier source object using the given CommandAPI objects.
	 *
	 * @param permission   The {@link CommandPermission} to check that the source object satisfies.
	 * @param requirements An arbitrary additional check to perform on the CommandSender
	 *                     after the permissions check
	 * @return A Predicate that makes sure a Brigadier source object satisfies the given permission and arbitrary requirements.
	 */
	public Predicate<Source> generateBrigadierRequirements(CommandPermission permission, Predicate<CommandSender> requirements) {
		// If requirements are always false, result is always false
		if (requirements == CommandPermission.FALSE()) return CommandPermission.FALSE();

		// Find the intial check for the given CommandPermission
		Predicate<CommandSender> senderCheck = platform.getPermissionCheck(permission);

		// Merge with requirements (unless its always true, which wouldn't add anything)
		if (requirements != CommandPermission.TRUE()) senderCheck = senderCheck.and(requirements);

		// Short circuit if the final test is always true or false
		if (senderCheck == CommandPermission.TRUE()) return CommandPermission.TRUE();
		if (senderCheck == CommandPermission.FALSE()) return CommandPermission.FALSE();

		// Otherwise, convert Brigadier Source to CommandSender and run the check
		final Predicate<CommandSender> finalSenderCheck = senderCheck;
		return source -> {
			CommandSender sender = platform.getCommandSenderFromCommandSource(source);
			return finalSenderCheck.test(sender);
		};
	}

	/////////////////////////////
	// SECTION: Unregistration //
	/////////////////////////////
	/**
	 * Unregisters a command
	 *
	 * @param command the name of the command to unregister
	 * @param unregisterNamespaces whether the unregistration system should attempt to remove versions of the
	 *                                command that start with a namespace. E.g. `minecraft:command`, `bukkit:command`,
	 *                                or `plugin:command`. If true, these namespaced versions of a command are also
	 *                                unregistered.
	 */
	public void unregister(String command, boolean unregisterNamespaces) {
		// Remove command from `registeredCommands`
		registeredCommands.remove(command);
		if (unregisterNamespaces) removeCommandNamespace(registeredCommands, command, e -> true);

		// Platform-specific unregistration logic
		platform.unregister(command, unregisterNamespaces);

		// Update the dispatcher file
		writeDispatcherToFile();
	}


	protected void removeBrigadierCommands(RootCommandNode<Source> root, String commandName,
										   boolean unregisterNamespaces, Predicate<CommandNode<Source>> extraCheck) {
		Map<String, CommandNode<Source>> children = CommandAPIHandler.getCommandNodeChildren(root);
		Map<String, LiteralCommandNode<Source>> literals = CommandAPIHandler.getCommandNodeLiterals(root);
		Map<String, ArgumentCommandNode<Source, ?>> arguments = CommandAPIHandler.getCommandNodeArguments(root);

		removeCommandFromMapIfCheckPasses(children, commandName, extraCheck);
		if (literals != null) removeCommandFromMapIfCheckPasses(literals, commandName, extraCheck);
		// Commands should really only be represented as literals, but it is technically possible
		// to put an ArgumentCommandNode in the root, so we'll check
		removeCommandFromMapIfCheckPasses(arguments, commandName, extraCheck);

		if (unregisterNamespaces) {
			removeCommandNamespace(children, commandName, extraCheck);
			if (literals != null) removeCommandNamespace(literals, commandName, extraCheck);
			removeCommandNamespace(arguments, commandName, extraCheck);
		}
	}

	protected static <T> void removeCommandNamespace(Map<String, ? extends T> map, String commandName, Predicate<T> extraCheck) {
		for (String key : new HashSet<>(map.keySet())) {
			if (!isThisTheCommandButNamespaced(commandName, key)) continue;

			removeCommandFromMapIfCheckPasses(map, key, extraCheck);
		}
	}

	protected static <T> void removeCommandFromMapIfCheckPasses(Map<String, ? extends T> map, String key, Predicate<T> extraCheck) {
		T element = map.get(key);
		if (element == null) return;
		if (extraCheck.test(element)) map.remove(key);
	}

	protected static boolean isThisTheCommandButNamespaced(String commandName, String key) {
		if (!key.contains(":")) return false;
		String[] split = key.split(":");
		if (split.length < 2) return false;
		return split[1].equalsIgnoreCase(commandName);
	}

	////////////////////////////////
	// SECTION: Brigadier Helpers //
	////////////////////////////////

	public void writeDispatcherToFile() {
		File file = CommandAPI.getConfiguration().getDispatcherFile();
		if (file != null) {
			try {
				// Make sure the file exists
				file.getParentFile().mkdirs();
				file.createNewFile();
			} catch (IOException e) {
				CommandAPI.logError("Failed to create the required directories for " + file.getName() + ": " + e.getMessage());
				return;
			}

			try {
				// Write the dispatcher json
				writeDispatcherToFile(file, platform.getBrigadierDispatcher());
			} catch (IOException e) {
				CommandAPI.logError("Failed to write command registration info to " + file.getName() + ": " + e.getMessage());
			}
		}
	}

	/**
	 * Creates a JSON file that describes the hierarchical structure of the commands
	 * that have been registered by the server.
	 *
	 * @param file       The JSON file to write to
	 * @param dispatcher The Brigadier CommandDispatcher
	 * @throws IOException When the file fails to be written to
	 */
	public void writeDispatcherToFile(File file, CommandDispatcher<Source> dispatcher) throws IOException {
		Files.asCharSink(file, StandardCharsets.UTF_8).write(new GsonBuilder().setPrettyPrinting().create()
			.toJson(serializeNodeToJson(dispatcher.getRoot())));
	}

	private record Node<Source>(CommandNode<Source> commandNode, Consumer<JsonElement> resultConsumer, JsonArray path) {
	}

	public JsonObject serializeNodeToJson(CommandNode<Source> rootNode) {
		// We preform a breadth-first traversal of the node tree to find the shortest path to each node.
		//  We prefer that the first path found would not go through a redirect, so nodes found from redirects
		//  are put in their own lower priority queue. It may be necessary to fully process these nodes though
		//  in case a node is only accessible via redirects, which may happen for example when the main alias
		//  of a command is removed.
		Queue<Node<Source>> nodesToProcess = new LinkedList<>();
		Queue<Node<Source>> redirectsToProcess = new LinkedList<>();
		Map<CommandNode<Source>, JsonArray> shortestPath = new IdentityHashMap<>();

		// Extract serialization of the rootNode as our result
		JsonObject resultHolder = new JsonObject();
		redirectsToProcess.offer(new Node<>(rootNode, result -> resultHolder.add("result", result), new JsonArray()));

		Node<Source> node;
		while ((node = redirectsToProcess.poll()) != null) {
			nodesToProcess.offer(node);

			while ((node = nodesToProcess.poll()) != null) {
				CommandNode<Source> commandNode = node.commandNode;

				// Add information to parent
				JsonArray path = shortestPath.get(commandNode);
				if (path != null) {
					// Represent this node with the shortest path
					node.resultConsumer.accept(path);
					continue;
				}

				// This is the first time finding this node
				shortestPath.put(commandNode, node.path);

				// Represent this node with a new object
				JsonObject output = new JsonObject();
				node.resultConsumer.accept(output);

				// Node type
				NodeTypeSerializer.addTypeInformation(output, commandNode);

				// Children
				Collection<CommandNode<Source>> children = commandNode.getChildren();
				if (!children.isEmpty()) {
					JsonObject childrenHolder = new JsonObject();
					output.add("children", childrenHolder);

					for (CommandNode<Source> child : children) {
						String name = child.getName();

						JsonArray newPath = new JsonArray();
						newPath.addAll(node.path);
						newPath.add(name);

						nodesToProcess.offer(new Node<>(child, result -> childrenHolder.add(name, result), newPath));
					}
				}

				// Command
				if (commandNode.getCommand() != null) {
					output.addProperty("executable", true);
				}

				// Redirect
				CommandNode<Source> redirect = commandNode.getRedirect();
				if (redirect != null) {
					JsonArray newPath = new JsonArray();
					newPath.addAll(node.path);
					newPath.add("redirect " + redirect.getName());

					redirectsToProcess.offer(new Node<>(redirect, result -> output.add("redirect", result), newPath));
				}
			}
		}

		return resultHolder.getAsJsonObject("result");
	}

	public LiteralCommandNode<Source> namespaceNode(LiteralCommandNode<Source> original, String namespace) {
		// Adapted from a section of `CraftServer#syncCommands`
		LiteralCommandNode<Source> clone = new LiteralCommandNode<>(
			namespace + ":" + original.getLiteral(),
			original.getCommand(),
			original.getRequirement(),
			original.getRedirect(),
			original.getRedirectModifier(),
			original.isFork()
		);

		for (CommandNode<Source> child : original.getChildren()) {
			clone.addChild(child);
		}
		return clone;
	}

	public static <Source> Map<String, CommandNode<Source>> getCommandNodeChildren(CommandNode<Source> target) {
		return (Map<String, CommandNode<Source>>) commandNodeChildren.get(target);
	}

	public static <Source> Map<String, LiteralCommandNode<Source>> getCommandNodeLiterals(CommandNode<Source> target) {
		if (commandNodeLiterals == null) return null;
		return (Map<String, LiteralCommandNode<Source>>) commandNodeLiterals.get(target);
	}

	public static <Source> Map<String, ArgumentCommandNode<Source, ?>> getCommandNodeArguments(CommandNode<Source> target) {
		return (Map<String, ArgumentCommandNode<Source, ?>>) commandNodeArguments.get(target);
	}

	////////////////////////////////
	// SECTION: Parsing arguments //
	////////////////////////////////

	/**
	 * Returns the raw input for an argument for a given command context and its
	 * key. This effectively returns the string value that is currently typed for
	 * this argument
	 *
	 * @param <CommandSource> the command source type
	 * @param cmdCtx          the command context which is used to run this
	 *                        command
	 * @param key             the node name for the argument
	 * @return the raw input string for this argument
	 */
	public static <CommandSource> String getRawArgumentInput(CommandContext<CommandSource> cmdCtx, String key) {
		final ParsedArgument<?, ?> parsedArgument = commandContextArguments.get(cmdCtx).get(key);

		// Sanity check: See https://github.com/JorelAli/CommandAPI/wiki/Implementation-details#chatcomponentargument-raw-arguments
		StringRange range = parsedArgument.getRange();
		if (range.getEnd() > cmdCtx.getInput().length()) {
			range = StringRange.between(range.getStart(), cmdCtx.getInput().length());
		}
		return range.get(cmdCtx.getInput());
	}

	/**
	 * Converts the List&lt;Argument> into a {@link CommandArguments} for command execution
	 *
	 * @param cmdCtx the command context that will execute this command
	 * @param args   the map of strings to arguments
	 * @return an CommandArguments object which can be used in (sender, args) ->
	 * @throws CommandSyntaxException If an argument is improperly formatted and cannot be parsed
	 */
	public CommandArguments argsToCommandArgs(CommandContext<Source> cmdCtx, List<Argument> args) throws CommandSyntaxException {
		// https://github.com/JorelAli/CommandAPI/issues/310 & https://github.com/Mojang/brigadier/issues/137
		//  If a command goes through a redirect, Brigadier puts all context after the redirect into the child
		//  CommandContext. When executing a command, Brigadier will pass us the child context. However, when
		//  suggesting a command, Brigadier passes us the root context for some reason. Assuming the behavior
		//  when executing a command is correct, when suggesting commands we should use the last child.
		cmdCtx = cmdCtx.getLastChild();

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
	public Object parseArgument(CommandContext<Source> cmdCtx, String key, Argument value, CommandArguments previousArgs) throws CommandSyntaxException {
		if (value.isListed()) {
			return value.parseArgument(cmdCtx, key, previousArgs);
		} else {
			return null;
		}
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
	 * @param clazz            the class where the field is declared
	 * @param name             the name of the field
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

	/**
	 * Class to store cached methods and fields
	 * <p>
	 * This is required because each key is made up of a class and a field or method
	 * name
	 */
	private record ClassCache(Class<?> clazz, String name, String mojangMappedName) {
	}
}
