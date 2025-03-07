package dev.jorel.commandapi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import dev.jorel.commandapi.exceptions.InvalidCommandNameException;
import dev.jorel.commandapi.help.CommandAPIHelpTopic;
import dev.jorel.commandapi.help.EditableHelpTopic;
import dev.jorel.commandapi.help.FullDescriptionGenerator;
import dev.jorel.commandapi.help.ShortDescriptionGenerator;
import dev.jorel.commandapi.help.UsageGenerator;

/**
 * This is a base class for {@link AbstractCommandAPICommand} and {@link AbstractCommandTree} command definitions
 *
 * @param <Impl> The class extending this class, used as the return type for chain calls
 * @param <CommandSender> The CommandSender class used by the class extending this class
 */
public abstract class ExecutableCommand<Impl
/// @cond DOX
extends ExecutableCommand<Impl, CommandSender>
/// @endcond
, CommandSender> extends Executable<Impl, CommandSender> {
	// Command names
	/**
	 * The command's name
	 */
	protected final String name;
	/**
	 * The command's aliases
	 */
	protected String[] aliases = new String[0];

	// Command requirements
	/**
	 * The command's permission
	 */
	protected CommandPermission permission = CommandPermission.NONE;
	/**
	 * A predicate that the comamand sender must pass in order to execute the command
	 */
	protected Predicate<CommandSender> requirements = CommandPermission.TRUE();

	// Command help
	protected CommandAPIHelpTopic<CommandSender> helpTopic = new EditableHelpTopic<>();

	ExecutableCommand(final String name) {
		if (name == null || name.isEmpty() || name.contains(" ")) {
			throw new InvalidCommandNameException(name);
		}

		this.name = name;
	}

	/////////////////////
	// Builder methods //
	/////////////////////

	/**
	 * Adds an array of aliases to the current command builder
	 *
	 * @param aliases An array of aliases which can be used to execute this command
	 * @return this command builder
	 */
	public Impl withAliases(String... aliases) {
		this.aliases = aliases;
		return instance();
	}

	/**
	 * Applies a permission to the current command builder
	 *
	 * @param permission The permission node required to execute this command
	 * @return this command builder
	 */
	public Impl withPermission(CommandPermission permission) {
		this.permission = permission;
		return instance();
	}

	/**
	 * Applies a permission to the current command builder
	 *
	 * @param permission The permission node required to execute this command
	 * @return this command builder
	 */
	public Impl withPermission(String permission) {
		this.permission = CommandPermission.fromString(permission);
		return instance();
	}

	/**
	 * Applies a permission to the current command builder
	 *
	 * @param permission The permission node required to execute this command
	 * @return this command builder
	 */
	public Impl withoutPermission(CommandPermission permission) {
		this.permission = permission.negate();
		return instance();
	}

	/**
	 * Applies a permission to the current command builder
	 *
	 * @param permission The permission node required to execute this command
	 * @return this command builder
	 */
	public Impl withoutPermission(String permission) {
		this.permission = CommandPermission.fromString(permission).negate();
		return instance();
	}

	/**
	 * Adds a requirement that has to be satisfied to use this command. This method
	 * can be used multiple times and each use of this method will AND its
	 * requirement with the previously declared ones
	 *
	 * @param requirement the predicate that must be satisfied to use this command
	 * @return this command builder
	 */
	public Impl withRequirement(Predicate<CommandSender> requirement) {
		this.requirements = this.requirements.and(requirement);
		return instance();
	}

	/**
	 * Sets the {@link CommandAPIHelpTopic} for this command. Using this method will override
	 * any declared short description, full description or usage description provided
	 * via the following methods and similar overloads:
	 * <ul>
	 *   <li>{@link ExecutableCommand#withShortDescription(String)}</li>
	 *   <li>{@link ExecutableCommand#withFullDescription(String)}</li>
	 *   <li>{@link ExecutableCommand#withUsage(String...)}</li>
	 *   <li>{@link ExecutableCommand#withHelp(String, String)}</li>
	 * </ul>
	 * Further calls to these methods will also be ignored.
	 *
	 * @param helpTopic the help topic to use for this command
	 * @return this command builder
	 */
	public Impl withHelp(CommandAPIHelpTopic<CommandSender> helpTopic) {
		this.helpTopic = helpTopic;
		return instance();
	}

	/**
	 * Sets the short description for this command. This is the help which is
	 * shown in the main /help menu.
	 *
	 * @param description the short description for this command
	 * @return this command builder
	 */
	public Impl withShortDescription(String description) {
		if (helpTopic instanceof EditableHelpTopic<CommandSender> editableHelpTopic) {
			helpTopic = editableHelpTopic.withShortDescription(description);
		}
		return instance();
	}

	/**
	 * Sets the full description for this command. This is the help which is
	 * shown in the specific /help page for this command (e.g. /help mycommand).
	 *
	 * @param description the full description for this command
	 * @return this command builder
	 */
	public Impl withFullDescription(String description) {
		if (helpTopic instanceof EditableHelpTopic<CommandSender> editableHelpTopic) {
			helpTopic = editableHelpTopic.withFullDescription(description);
		}
		return instance();
	}

	/**
	 * Sets the short and full description for this command. This is a short-hand
	 * for the {@link ExecutableCommand#withShortDescription} and
	 * {@link ExecutableCommand#withFullDescription} methods.
	 *
	 * @param shortDescription the short description for this command
	 * @param fullDescription  the full description for this command
	 * @return this command builder
	 */
	public Impl withHelp(String shortDescription, String fullDescription) {
		if (helpTopic instanceof EditableHelpTopic<CommandSender> editableHelpTopic) {
			helpTopic = editableHelpTopic.withHelp(shortDescription, fullDescription);
		}
		return instance();
	}

	/**
	 * Sets the full usage for this command. This is the usage which is
	 * shown in the specific /help page for this command (e.g. /help mycommand).
	 *
	 * @param usage the full usage for this command
	 * @return this command builder
	 */
	public Impl withUsage(String... usage) {
		if (helpTopic instanceof EditableHelpTopic<CommandSender> editableHelpTopic) {
			helpTopic = editableHelpTopic.withUsage(usage);
		}
		return instance();
	}

	/**
	 * Sets the short description of this command to be generated using the given {@link ShortDescriptionGenerator}.
	 * This is the help which is shown in the main /help menu.
	 *
	 * @param description The {@link ShortDescriptionGenerator} to use to generate the short description.
	 * @return this command builder
	 */
	public Impl withShortDescription(ShortDescriptionGenerator description) {
		if (helpTopic instanceof EditableHelpTopic<CommandSender> editableHelpTopic) {
			helpTopic = editableHelpTopic.withShortDescription(description);
		}
		return instance();
	}

	/**
	 * Sets the full description of this command to be generated using the given {@link FullDescriptionGenerator}.
	 * This is the help which is shown in the specific /help page for this command (e.g. /help mycommand).
	 *
	 * @param description The {@link FullDescriptionGenerator} to use to generate the full description.
	 * @return this command builder
	 */
	public Impl withFullDescription(FullDescriptionGenerator<CommandSender> description) {
		if (helpTopic instanceof EditableHelpTopic<CommandSender> editableHelpTopic) {
			helpTopic = editableHelpTopic.withFullDescription(description);
		}
		return instance();
	}

	/**
	 * Sets the usage of this command to be generated using the given {@link UsageGenerator}.
	 * This is the usage which is shown in the specific /help page for this command (e.g. /help mycommand).
	 *
	 * @param usage The {@link UsageGenerator} to use to generate the usage.
	 * @return this command builder
	 */
	public Impl withUsage(UsageGenerator<CommandSender> usage) {
		if (helpTopic instanceof EditableHelpTopic<CommandSender> editableHelpTopic) {
			helpTopic = editableHelpTopic.withUsage(usage);
		}
		return instance();
	}

	/////////////////////////
	// Getters and setters //
	/////////////////////////

	/**
	 * Returns the name of this command
	 *
	 * @return the name of this command
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the aliases for this command
	 *
	 * @param aliases the aliases for this command
	 */
	public void setAliases(String[] aliases) {
		this.aliases = aliases;
	}

	/**
	 * Returns an array of aliases that can be used to run this command
	 *
	 * @return an array of aliases that can be used to run this command
	 */
	public String[] getAliases() {
		return aliases;
	}

	/**
	 * Returns the permission associated with this command
	 *
	 * @return the permission associated with this command
	 */
	public CommandPermission getPermission() {
		return this.permission;
	}

	/**
	 * Sets the permission required to run this command
	 *
	 * @param permission the permission required to run this command
	 */
	public void setPermission(CommandPermission permission) {
		this.permission = permission;
	}

	/**
	 * Returns the requirements that must be satisfied to run this command
	 *
	 * @return the requirements that must be satisfied to run this command
	 */
	public Predicate<CommandSender> getRequirements() {
		return this.requirements;
	}

	/**
	 * Sets the requirements that must be satisfied to run this command
	 *
	 * @param requirements the requirements that must be satisfied to run this command
	 */
	public void setRequirements(Predicate<CommandSender> requirements) {
		this.requirements = requirements;
	}

	/**
	 * Returns the {@link CommandAPIHelpTopic} for this command
	 *
	 * @return the {@link CommandAPIHelpTopic} for this command
	 */
	public CommandAPIHelpTopic<CommandSender> getHelpTopic() {
		return helpTopic;
	}

	/**
	 * Returns the short description for this command
	 *
	 * @return the short description for this command
	 */
	public String getShortDescription() {
		return this.helpTopic.getShortDescription().orElse(null);
	}

	/**
	 * Returns the full description for this command
	 *
	 * @return the full description for this command
	 */
	public String getFullDescription() {
		return this.helpTopic.getFullDescription(null).orElse(null);
	}

	/**
	 * Returns the usage for this command
	 *
	 * @return the usage for this command
	 */
	public String[] getUsage() {
		return this.helpTopic.getUsage(null, null).orElse(null);
	}

	//////////////////
	// Registration //
	//////////////////

	/**
	 * Overrides a command. Effectively the same as unregistering the command using
	 * CommandAPI.unregister() and then registering the command using .register()
	 */
	public void override() {
		CommandAPI.unregister(this.name, true);
		register();
	}

	/**
	 * Registers this command with the default namespace
	 */
	public void register() {
		register(CommandAPI.getConfiguration().getNamespace());
	}

	/**
	 * Registers the command with the given namespace.
	 *
	 * @param namespace The namespace for this command. This cannot be null, and each platform may impose additional requirements.
	 *                  See {@link CommandAPIPlatform#validateNamespace(ExecutableCommand, String)}.
	 * @throws NullPointerException if the namespace is null.
	 */
	public void register(String namespace) {
		((CommandAPIHandler<?, CommandSender, ?>) CommandAPIHandler.getInstance()).registerCommand(this, namespace);
	}

	protected static record CommandInformation<CommandSender, Source>(
		LiteralCommandNode<Source> rootNode,
		List<LiteralCommandNode<Source>> aliasNodes,
		RegisteredCommand<CommandSender> command
	) {
	}

	protected <Source> CommandInformation<CommandSender, Source> createCommandInformation(String namespace) {
		checkPreconditions();

		// Create rootNode
		LiteralCommandNode<Source> rootNode = this.<Source>createCommandNodeBuilder(name).build();

		List<RegisteredCommand.Node<CommandSender>> children = createArgumentNodes(rootNode);

		// Create aliaseNodes
		List<LiteralCommandNode<Source>> aliasNodes = createAliasNodes(rootNode);

		// Create command information
		RegisteredCommand<CommandSender> command = new RegisteredCommand<>(
			name, aliases, namespace, helpTopic,
			new RegisteredCommand.Node<>(name, getClass().getSimpleName(), name, isRootExecutable(), permission, requirements, children)
		);

		return new CommandInformation<>(rootNode, aliasNodes, command);
	}

	protected <Source> LiteralArgumentBuilder<Source> createCommandNodeBuilder(String nodeName) {
		CommandAPIHandler<?, CommandSender, Source> handler = CommandAPIHandler.getInstance();

		// Create node
		LiteralArgumentBuilder<Source> rootBuilder = LiteralArgumentBuilder.literal(nodeName);

		// Add permission and requirements
		rootBuilder.requires(handler.generateBrigadierRequirements(permission, requirements));

		// Add the executor
		if (isRootExecutable()) {
			rootBuilder.executes(handler.generateBrigadierCommand(List.of(), executor));
		}

		return rootBuilder;
	}

	protected <Source> List<LiteralCommandNode<Source>> createAliasNodes(LiteralCommandNode<Source> rootNode) {
		List<LiteralCommandNode<Source>> aliasNodes = new ArrayList<>();
		for (String alias : aliases) {
			// Create node
			LiteralArgumentBuilder<Source> aliasBuilder = createCommandNodeBuilder(alias);

			// Redirect to rootNode so all its arguments come after this node
			aliasBuilder.redirect(rootNode);

			// Register alias node
			aliasNodes.add(aliasBuilder.build());
		}

		return aliasNodes;
	}

	protected abstract void checkPreconditions();

	protected abstract boolean isRootExecutable();

	protected abstract <Source> List<RegisteredCommand.Node<CommandSender>> createArgumentNodes(LiteralCommandNode<Source> rootNode);
}
