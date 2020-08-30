package dev.jorel.commandapi;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.IGreedyArgument;
import dev.jorel.commandapi.exceptions.EmptyExecutorException;
import dev.jorel.commandapi.exceptions.GreedyArgumentException;
import dev.jorel.commandapi.exceptions.InvalidCommandNameException;
import dev.jorel.commandapi.executors.CommandBlockCommandExecutor;
import dev.jorel.commandapi.executors.CommandBlockResultingCommandExecutor;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.jorel.commandapi.executors.ConsoleCommandExecutor;
import dev.jorel.commandapi.executors.ConsoleResultingCommandExecutor;
import dev.jorel.commandapi.executors.EntityCommandExecutor;
import dev.jorel.commandapi.executors.EntityResultingCommandExecutor;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import dev.jorel.commandapi.executors.PlayerResultingCommandExecutor;
import dev.jorel.commandapi.executors.ProxyCommandExecutor;
import dev.jorel.commandapi.executors.ProxyResultingCommandExecutor;
import dev.jorel.commandapi.executors.ResultingCommandExecutor;

/**
 * A builder used to create commands to be registered by the CommandAPI.
 */
public class CommandAPICommand {

	final String commandName;
	CommandPermission permission = CommandPermission.NONE;
	String[] aliases = new String[0];
	Predicate<CommandSender> requirements = s -> true;
	LinkedHashMap<String, Argument> args = new LinkedHashMap<>();
	CustomCommandExecutor executor = new CustomCommandExecutor();
	
	/**
	 * Creates a new command builder
	 * @param commandName The name of the command to create
	 */
	public CommandAPICommand(String commandName) {
		this.commandName = commandName;
	}
	
	/**
	 * Applies a permission to the current command builder
	 * @param permission The permission node required to execute this command
	 * @return this command builder
	 */
	public CommandAPICommand withPermission(CommandPermission permission) {
		this.permission = permission;
		return this;
	}
	
	/**
	 * Adds a requirement that has to be satisfied to use this command. This method
	 * can be used multiple times and each use of this method will AND its
	 * requirement with the previously declared ones
	 * 
	 * @param requirement the predicate that must be satisfied to use this command
	 * @return this command builder
	 */
	public CommandAPICommand withRequirement(Predicate<CommandSender> requirement) {
		this.requirements = this.requirements.and(requirement);
		return this;
	}
	
	/**
	 * Adds an array of aliases to the current command builder
	 * @param aliases An array of aliases which can be used to execute this command
	 * @return this command builder
	 */
	public CommandAPICommand withAliases(String... aliases) {
		this.aliases = aliases;
		return this;
	}
	
	/**
	 * Adds the mapping of arguments to the current command builder
	 * @param args A <code>LinkedHashMap</code> that represents the arguments that this command can accept
	 * @return this command builder
	 */
	public CommandAPICommand withArguments(LinkedHashMap<String, Argument> args) {
		this.args = args;
		return this;
	}
	
	// Regular command executor 
	
	/**
	 * Adds an executor to the current command builder
	 * @param executor A lambda of type <code>(CommandSender, Object[]) -> ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	public CommandAPICommand executes(CommandExecutor executor) {
		this.executor.addNormalExecutor(executor);
		return this;
	}
	
	/**
	 * Adds an executor to the current command builder
	 * @param executor A lambda of type <code>(CommandSender, Object[]) -> int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	public CommandAPICommand executes(ResultingCommandExecutor executor) {
		this.executor.addResultingExecutor(executor);
		return this;
	}
	
	// Player command executor
	
	/**
	 * Adds an executor to the current command builder
	 * @param executor A lambda of type <code>(Player, Object[]) -> ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	public CommandAPICommand executesPlayer(PlayerCommandExecutor executor) {
		this.executor.addNormalExecutor(executor);
		return this;
	}
	
	/**
	 * Adds an executor to the current command builder
	 * @param executor A lambda of type <code>(Player, Object[]) -> int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	public CommandAPICommand executesPlayer(PlayerResultingCommandExecutor executor) {
		this.executor.addResultingExecutor(executor);
		return this;
	}
	
	// Entity command executor
	
	/**
	 * Adds an executor to the current command builder
	 * @param executor A lambda of type <code>(Entity, Object[]) -> ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	public CommandAPICommand executesEntity(EntityCommandExecutor executor) {
		this.executor.addNormalExecutor(executor);
		return this;
	}
	
	/**
	 * Adds an executor to the current command builder
	 * @param executor A lambda of type <code>(Entity, Object[]) -> int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	public CommandAPICommand executesEntity(EntityResultingCommandExecutor executor) {
		this.executor.addResultingExecutor(executor);
		return this;
	}
	
	// Proxy command executor
	
	/**
	 * Adds an executor to the current command builder
	 * @param executor A lambda of type <code>(Entity, Object[]) -> ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	public CommandAPICommand executesProxy(ProxyCommandExecutor executor) {
		this.executor.addNormalExecutor(executor);
		return this;
	}
	
	/**
	 * Adds an executor to the current command builder
	 * @param executor A lambda of type <code>(Entity, Object[]) -> int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	public CommandAPICommand executesProxy(ProxyResultingCommandExecutor executor) {
		this.executor.addResultingExecutor(executor);
		return this;
	}
	
	// Command block command sender
	
	/**
	 * Adds an executor to the current command builder
	 * @param executor A lambda of type <code>(BlockCommandSender, Object[]) -> ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	public CommandAPICommand executesCommandBlock(CommandBlockCommandExecutor executor) {
		this.executor.addNormalExecutor(executor);
		return this;
	}
	
	/**
	 * Adds an executor to the current command builder
	 * @param executor A lambda of type <code>(BlockCommandSender, Object[]) -> int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	public CommandAPICommand executesCommandBlock(CommandBlockResultingCommandExecutor executor) {
		this.executor.addResultingExecutor(executor);
		return this;
	}
	
	// Console command sender
	
	/**
	 * Adds an executor to the current command builder
	 * @param executor A lambda of type <code>(BlockCommandSender, Object[]) -> ()</code> that will be executed when the command is run
	 * @return this command builder
	 */
	public CommandAPICommand executesConsole(ConsoleCommandExecutor executor) {
		this.executor.addNormalExecutor(executor);
		return this;
	}
	
	/**
	 * Adds an executor to the current command builder
	 * @param executor A lambda of type <code>(BlockCommandSender, Object[]) -> int</code> that will be executed when the command is run
	 * @return this command builder
	 */
	public CommandAPICommand executesConsole(ConsoleResultingCommandExecutor executor) {
		this.executor.addResultingExecutor(executor);
		return this;
	}
	
	/**
	 * Registers the command
	 */
	public void register() {
		if(this.executor.isEmpty()) {
			throw new EmptyExecutorException();
		} else {
			if(!CommandAPI.canRegister()) {
				CommandAPI.getLog().severe("Cannot register command /" + commandName + ", because the server has finished loading!");
				return;
			}
			try {
				//Sanitize commandNames
				if(commandName == null || commandName.length() == 0) {
					throw new InvalidCommandNameException(commandName);
				}
				
				//Make a local copy of args to deal with
				@SuppressWarnings("unchecked")
				LinkedHashMap<String, Argument> copyOfArgs = args == null ? new LinkedHashMap<>() : (LinkedHashMap<String, Argument>) args.clone();
				
				//if args contains a GreedyString && args.getLast != GreedyString
				long numGreedyArgs = copyOfArgs.values().stream().filter(arg -> arg instanceof IGreedyArgument).count();
				if(numGreedyArgs >= 1) {
					//A GreedyString has been found
					if(!(copyOfArgs.values().toArray()[copyOfArgs.size() - 1] instanceof IGreedyArgument)) {
						throw new GreedyArgumentException();
					}
					
					if(numGreedyArgs > 1) {
						throw new GreedyArgumentException();
					}
				}
				
				//Reassign permissions to arguments if not declared
				for(Entry<String, Argument> entry : copyOfArgs.entrySet()) {
					if(entry.getValue().getArgumentPermission() == null) {
						entry.setValue(entry.getValue().withPermission(permission));
					}
				}
				
				CommandAPIHandler.register(commandName, permission, aliases, requirements, copyOfArgs, executor);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
