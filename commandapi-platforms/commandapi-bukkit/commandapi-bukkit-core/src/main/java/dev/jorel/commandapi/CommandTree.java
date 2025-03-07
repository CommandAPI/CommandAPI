package dev.jorel.commandapi;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.help.BukkitHelpTopicWrapper;
import dev.jorel.commandapi.help.CommandAPIHelpTopic;

import org.bukkit.command.CommandSender;
import org.bukkit.help.HelpTopic;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandTree extends AbstractCommandTree<CommandTree, Argument<?>, CommandSender> implements BukkitExecutable<CommandTree> {
	/**
	 * Creates a main root node for a command tree with a given command name
	 *
	 * @param commandName The name of the command to create
	 */
	public CommandTree(String commandName) {
		super(commandName);
	}

	@Override
	public CommandTree instance() {
		return this;
	}
	
	/**
	 * Sets the {@link HelpTopic} for this command. Using this method will override
	 * any declared short description, full description or usage description provided
	 * via the following methods and similar overloads:
	 * <ul>
	 *   <li>{@link CommandTree#withShortDescription(String)}</li>
	 *   <li>{@link CommandTree#withFullDescription(String)}</li>
	 *   <li>{@link CommandTree#withUsage(String...)}</li>
	 *   <li>{@link CommandTree#withHelp(String, String)}</li>
	 * </ul>
	 * Further calls to these methods will also be ignored.
	 * See also {@link ExecutableCommand#withHelp(CommandAPIHelpTopic)}.
	 * 
	 * @param helpTopic the help topic to use for this command
	 * @return this command builder
	 */
	public CommandTree withHelp(HelpTopic helpTopic) {
		this.helpTopic = new BukkitHelpTopicWrapper(helpTopic);
		return instance();
	}

	/**
	 * Registers this command with the given namespace.
	 *
	 * @param namespace The namespace for this command. This cannot be null or empty, and can only contain 0-9, a-z, underscores, periods, and hyphens.
	 * @throws NullPointerException if the namespace is null.
	 */
	@Override
	public void register(String namespace) {
		super.register(namespace);
	}

	/**
	 * Registers this command with a given {@link JavaPlugin} instance
	 *
	 * @param plugin The plugin instance used to determine this command's namespace
	 */
	public void register(JavaPlugin plugin) {
		super.register(plugin.getName().toLowerCase());
	}

}
