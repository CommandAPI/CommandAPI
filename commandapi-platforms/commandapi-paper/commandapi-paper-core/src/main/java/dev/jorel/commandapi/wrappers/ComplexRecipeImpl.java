package dev.jorel.commandapi.wrappers;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ComplexRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;

/**
 * Creates a ComplexRecipe based on a NamespacedKey and a Recipe object.
 * @param key the namespaced key
 * @param recipe the recipe
 */
public record ComplexRecipeImpl(NamespacedKey key, Recipe recipe) implements ComplexRecipe {

	/**
     * Get the result of this recipe.
     *
     * @return The result stack
     */
	@Override
	public @NotNull ItemStack getResult() {
		return recipe.getResult();
	}

	/**
     * Return the namespaced identifier for this object.
     *
     * @return this object's key
     */
	@Override
	public @NotNull NamespacedKey getKey() {
		return key;
	}

}
