package dev.jorel.commandapi.test;

import dev.jorel.commandapi.nms.NMS;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.loot.LootTables;
import org.bukkit.potion.PotionEffectType;

/**
 * The Bukkit "enum" lists. Unlike the implementations for older versions, these aren't listed by hand. Bukkit's
 * enchantments, potion effects, sounds and biomes are backed by the Minecraft registries since 1.20.5, so they
 * contain exactly what {@link MockRegistries} contains.
 */
public abstract class Enums extends ArgumentNMS {

	protected Enums(NMS<?> baseNMS) {
		super(baseNMS);
	}

	@Override
	public final Enchantment[] getEnchantments() {
		return Registry.ENCHANTMENT.stream().toArray(Enchantment[]::new);
	}

	@Override
	public final EntityType[] getEntityTypes() {
		return EntityType.values();
	}

	@Override
	public final LootTables[] getLootTables() {
		return LootTables.values();
	}

	@Override
	public final PotionEffectType[] getPotionEffects() {
		return Registry.EFFECT.stream().toArray(PotionEffectType[]::new);
	}

	@Override
	public Sound[] getSounds() {
		return Registry.SOUNDS.stream().toArray(Sound[]::new);
	}

	@Override
	public Biome[] getBiomes() {
		return Registry.BIOME.stream().toArray(Biome[]::new);
	}
}
