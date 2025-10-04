/*******************************************************************************
 * Copyright 2018, 2020 Jorel Ali (Skepter) - MIT License
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

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.wrappers.ComplexRecipeImpl;
import org.bukkit.Keyed;
import org.bukkit.inventory.Recipe;

/**
 * An argument that represents the Bukkit Recipe object
 * 
 * @since 2.1
 */
public class RecipeArgument extends SafeOverrideableArgument<Recipe, Recipe> implements CustomProvidedArgument {

	/**
	 * A Recipe argument. Represents a Bukkit Recipe or ComplexRecipe
	 * @param nodeName the name of the node for this argument
	 */
	public RecipeArgument(String nodeName) {
		super(nodeName, CommandAPIBukkit.get().getNMS()._ArgumentRecipe(), fromKey((Recipe r) -> ((Keyed) r).getKey()));
	}

	@Override
	public Class<Recipe> getPrimitiveType() {
		return Recipe.class;
	}

	@Override
	public SuggestionProviders getSuggestionProvider() {
		return SuggestionProviders.RECIPES;
	}

	@Override
	public CommandAPIArgumentType getArgumentType() {
		return CommandAPIArgumentType.RECIPE;
	}	
	
	@Override
	public <CommandSourceStack> Recipe parseArgument(CommandContext<CommandSourceStack> cmdCtx, String key, CommandArguments previousArgs) throws CommandSyntaxException {
		return CommandAPIBukkit.<CommandSourceStack>get().getNMS().getRecipe(cmdCtx, key, ComplexRecipeImpl::new);
	}
	
}
