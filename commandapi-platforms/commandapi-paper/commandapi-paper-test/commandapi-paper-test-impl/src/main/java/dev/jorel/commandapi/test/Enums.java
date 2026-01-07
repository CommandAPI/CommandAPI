package dev.jorel.commandapi.test;

import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.loot.LootTables;
import org.bukkit.potion.PotionEffectType;

/**
 * Bukkit "enum" lists
 */
public interface Enums {
	Enchantment[] getEnchantments();

	EntityType[] getEntityTypes();

	LootTables[] getLootTables();

	PotionEffectType[] getPotionEffects();

	Sound[] getSounds();

	Biome[] getBiomes();
}
