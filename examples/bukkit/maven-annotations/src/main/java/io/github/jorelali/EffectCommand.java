package io.github.jorelali;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.arguments.AEntitySelectorArgument;
import dev.jorel.commandapi.annotations.arguments.APotionEffectArgument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@Command("myeffect")
public class EffectCommand {
	@Default
	public static void applyEffect(Player player, @APotionEffectArgument PotionEffectType effectType) {
		player.addPotionEffect(new PotionEffect(effectType, 300 * 20, 1));
	}

	@Default
	public static void applyEffect(CommandSender sender, @APotionEffectArgument PotionEffectType effectType, @AEntitySelectorArgument.OnePlayer Player player) {
		player.addPotionEffect(new PotionEffect(effectType, 300 * 20, 1));
	}
}
