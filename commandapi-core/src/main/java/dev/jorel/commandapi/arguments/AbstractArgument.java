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
package dev.jorel.commandapi.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.AbstractArgumentTree;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.executors.CommandArguments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The core abstract class for Command API arguments
 * 
 * @param <T> The type of the underlying object that this argument casts to
 * @param <Impl> The class extending this class, used as the return type for chain calls
 * @param <Argument> The implementation of Argument used by the class extending this class
 * @param <CommandSender> The CommandSender class used by the class extending this class
 */
public abstract class AbstractArgument<T, Impl
/// @cond DOX
extends AbstractArgument<T, Impl, Argument, CommandSender>
/// @endcond
, Argument
/// @cond DOX
extends AbstractArgument<?, ?, Argument, CommandSender>
/// @endcond
, CommandSender> extends AbstractArgumentTree<Impl, Argument, CommandSender> {

	/**
	 * Returns the primitive type of the current Argument. After executing a
	 * command, this argument should yield an object of this returned class.
	 *
	 * @return the type that this argument yields when the command is run
	 */
	public abstract Class<T> getPrimitiveType();

	/**
	 * Returns the argument type for this argument.
	 *
	 * @return the argument type for this argument
	 */
	public abstract CommandAPIArgumentType getArgumentType();

	////////////////////////
	// Raw Argument Types //
	////////////////////////

	private final String nodeName;
	private final Supplier<ArgumentType<?>> rawType;

	/**
	 * Constructs an argument with a given NMS/brigadier type.
	 *
	 * @param nodeName the name to assign to this argument node
	 * @param rawType  the NMS or brigadier type to be used for this argument
	 */
	protected AbstractArgument(String nodeName, Supplier<ArgumentType<?>> rawType) {
		this.nodeName = nodeName;
		this.rawType = rawType;
	}

	/**
	 * Returns the NMS or brigadier type for this argument.
	 *
	 * @return the NMS or brigadier type for this argument
	 */
	public final ArgumentType<?> getRawType() {
		return this.rawType.get();
	}

	/**
	 * Returns the {@link Supplier} for the NMS or brigadier type for this argument.
	 *
	 * @return the {@link Supplier} for the NMS or brigadier type for this argument
	 */
	public final Supplier<ArgumentType<?>> getRawTypeSupplier() {
		return this.rawType;
	}

	/**
	 * Returns the name of this argument's node
	 *
	 * @return the name of this argument's node
	 */
	public final String getNodeName() {
		return this.nodeName;
	}

	/**
	 * Parses an argument, returning the specific Bukkit object that the argument
	 * represents. This is intended for use by the internals of the CommandAPI and
	 * isn't expected to be used outside the CommandAPI
	 *
	 * @param <Source>     the command source type
	 * @param cmdCtx       the context which ran this command
	 * @param key          the name of the argument node
	 * @param previousArgs a {@link CommandArguments} object holding previous parsed arguments
	 * @return the parsed object represented by this argument
	 * @throws CommandSyntaxException if parsing fails
	 */
	public abstract <Source> T parseArgument(CommandContext<Source> cmdCtx, String key, CommandArguments previousArgs) throws CommandSyntaxException;

	/////////////////
	// Suggestions //
	/////////////////

	private Optional<ArgumentSuggestions<CommandSender>> suggestions = Optional.empty();
	private Optional<ArgumentSuggestions<CommandSender>> addedSuggestions = Optional.empty();

	/**
	 * Include suggestions to add to the list of default suggestions represented by this argument.
	 *
	 * @param suggestions An {@link ArgumentSuggestions} object representing the suggestions. Use the
	 *                    Static methods on ArgumentSuggestions to create these.
	 * @return the current argument
	 */
	public Impl includeSuggestions(ArgumentSuggestions<CommandSender> suggestions) {
		this.addedSuggestions = Optional.of(suggestions);
		return instance();
	}

	/**
	 * Returns an optional function which produces an array of suggestions which should be added
	 * to existing suggestions.
	 *
	 * @return An Optional containing a function which generates suggestions
	 */
	public Optional<ArgumentSuggestions<CommandSender>> getIncludedSuggestions() {
		return addedSuggestions;
	}


	/**
	 * Replace the suggestions of this argument.
	 *
	 * @param suggestions An {@link ArgumentSuggestions} object representing the suggestions. Use the static methods in
	 *                    ArgumentSuggestions to create these.
	 * @return the current argument
	 */

	public Impl replaceSuggestions(ArgumentSuggestions<CommandSender> suggestions) {
		this.suggestions = Optional.of(suggestions);
		return instance();
	}

	/**
	 * Returns an optional function that maps the command sender to an IStringTooltip array of
	 * suggestions for the current command
	 *
	 * @return a function that provides suggestions, or <code>Optional.empty()</code> if there
	 * are no overridden suggestions.
	 */
	public final Optional<ArgumentSuggestions<CommandSender>> getOverriddenSuggestions() {
		return suggestions;
	}

	/////////////////
	// Permissions //
	/////////////////

	private CommandPermission permission = CommandPermission.NONE;

	/**
	 * Assigns the given permission as a requirement to execute this command.
	 *
	 * @param permission the permission required to execute this command
	 * @return this current argument
	 */
	public final Impl withPermission(CommandPermission permission) {
		this.permission = permission;
		return instance();
	}

	/**
	 * Assigns the given permission as a requirement to execute this command.
	 *
	 * @param permission the permission required to execute this command
	 * @return this current argument
	 */
	public final Impl withPermission(String permission) {
		this.permission = CommandPermission.fromString(permission);
		return instance();
	}

	/**
	 * Returns the permission required to run this command
	 *
	 * @return the permission required to run this command
	 */
	public final CommandPermission getArgumentPermission() {
		return permission;
	}

	//////////////////
	// Requirements //
	//////////////////

	private Predicate<CommandSender> requirements = s -> true;

	/**
	 * Returns the requirements required to run this command
	 *
	 * @return the requirements required to run this command
	 */
	public final Predicate<CommandSender> getRequirements() {
		return this.requirements;
	}

	/**
	 * Adds a requirement that has to be satisfied to use this argument. This method
	 * can be used multiple times and each use of this method will AND its
	 * requirement with the previously declared ones
	 *
	 * @param requirement the predicate that must be satisfied to use this argument
	 * @return this current argument
	 */
	public final Impl withRequirement(Predicate<CommandSender> requirement) {
		this.requirements = this.requirements.and(requirement);
		return instance();
	}

	/**
	 * Resets the requirements for this command
	 */
	void resetRequirements() {
		this.requirements = s -> true;
	}

	/////////////////
	// Listability //
	/////////////////

	private boolean isListed = true;

	/**
	 * Returns true if this argument will be listed in the Object args[] of the command executor
	 *
	 * @return true if this argument will be listed in the Object args[] of the command executor
	 */
	public boolean isListed() {
		return this.isListed;
	}

	/**
	 * Sets whether this argument will be listed in the Object args[] of the command executor
	 *
	 * @param listed if true, this argument will be included in the Object args[] of the command executor
	 * @return this current argument
	 */
	public Impl setListed(boolean listed) {
		this.isListed = listed;
		return instance();
	}

	/////////////////
	// Optionality //
	/////////////////

	private boolean isOptional = false;
	private final List<Argument> combinedArguments = new ArrayList<>();

	/**
	 * Returns true if this argument will be optional when executing the command this argument is included in
	 *
	 * @return true if this argument will be optional when executing the command this argument is included in
	 */
	public boolean isOptional() {
		return isOptional;
	}

	/**
	 * Sets whether this argument will be optional when executing the command this argument is included in
	 *
	 * @param optional if true, this argument will be optional when executing the command this argument is included in
	 * @return this current argument
	 */
	public Impl setOptional(boolean optional) {
		this.isOptional = optional;
		return instance();
	}

	/**
	 * Returns a list of arguments linked to this argument.
	 *
	 * @return A list of arguments linked to this argument
	 */
	public List<Argument> getCombinedArguments() {
		return combinedArguments;
	}

	/**
	 * Returns true if this argument has linked arguments.
	 *
	 * @return true if this argument has linked arguments
	 */
	public boolean hasCombinedArguments() {
		return !combinedArguments.isEmpty();
	}

	/**
	 * Adds combined arguments to this argument. Combined arguments are used to have required arguments after optional arguments
	 * by ignoring they exist until they are added to the arguments array for registration.
	 *
	 * This method also causes permissions and requirements from this argument to be copied over to the arguments you want to combine
	 * this argument with. Their permissions and requirements will be ignored.
	 *
	 * @param combinedArguments The arguments to combine to this argument
	 * @return this current argument
	 */
	@SafeVarargs
	public final Impl combineWith(Argument... combinedArguments) {
		for (Argument argument : combinedArguments) {
			this.combinedArguments.add(argument);
		}
		return instance();
	}

	///////////
	// Other //
	///////////

	/**
	 * Gets a list of entity names for the current provided argument. This is
	 * expected to be implemented by {@code EntitySelectorArgument} in Bukkit, see
	 * {@code EntitySelectorArgument#getEntityNames(Object)}
	 *
	 * @param argument a parsed (Bukkit) object representing the entity selector
	 *                 type. This is either a List, an Entity or a Player
	 * @return a list of strings representing the names of the entity or entities
	 * from {@code argument}
	 */
	public List<String> getEntityNames(Object argument) {
		return Collections.singletonList(null);
	}

	/**
	 * Copies permissions and requirements from the provided argument to this argument
	 * This also resets additional permissions and requirements.
	 *
	 * @param argument The argument to copy permissions and requirements from
	 */
	public void copyPermissionsAndRequirements(Argument argument) {
		this.resetRequirements();
		this.withRequirement(argument.getRequirements());
		this.withPermission(argument.getArgumentPermission());
	}

	public String getHelpString() {
		return "<" + this.getNodeName() + ">";
	}

	@Override
	public String toString() {
		return this.getNodeName() + "<" + this.getClass().getSimpleName() + ">";
	}
}