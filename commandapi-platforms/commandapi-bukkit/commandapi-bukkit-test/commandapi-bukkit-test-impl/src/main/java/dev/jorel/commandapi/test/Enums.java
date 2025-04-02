package dev.jorel.commandapi.test;

import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.loot.LootTables;
import org.bukkit.potion.PotionEffectType;

/**
 * Bukkit "enum" lists. These change for each version, so the methods are abstract.
 * This is in a separate class because we have to manually define a bunch of long lists,
 * so it's easier to read stuff when they are moved elsewhere.
 */
public interface Enums {
	Enchantment[] getEnchantments();

	EntityType[] getEntityTypes();

	LootTables[] getLootTables();

	PotionEffectType[] getPotionEffects();

	Sound[] getSounds();

	Biome[] getBiomes();
}