package io.github.jorelali;

import de.tr7zw.changeme.nbtapi.NBTContainer;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import dev.jorel.commandapi.CommandAPICommand;

public class MyCommands {

	// Plugin reference in case we need to access anything about the plugin,
	// such as its config.yml or something
	private Plugin plugin;

	public MyCommands(Plugin plugin) {
		this.plugin = plugin;
	}

	public void registerAllCommands() {
		// /break <location>
		// Breaks a block at <location>. Can only be executed by a player
		new CommandAPICommand("break")
			// We want to target blocks in particular, so use BLOCK_POSITION
			.withArguments(new LocationArgument("block", LocationType.BLOCK_POSITION))
			.executesPlayer((player, args) -> {
				((Location) args.get(0)).getBlock().breakNaturally();
			})
			.register();

		// /myeffect <player> <potion effect>
		// Applies a potion effect to a player. Basically a simple version of
		// the /effect command. The potion effect with be a level 1 potion
		// effect and the duration will be 5 minutes (300 seconds x 20 ticks)
		new CommandAPICommand("myeffect")
			.withArguments(new EntitySelectorArgument.OnePlayer("target"))
			.withArguments(new PotionEffectArgument("potion"))
			.executes((sender, args) -> {
				Player target = (Player) args.get(0);
				PotionEffectType potionEffectType = (PotionEffectType) args.get(1);
				target.addPotionEffect(new PotionEffect(potionEffectType, 300 * 20, 1));
			})
			.register();

		// An example of the NBTCompoundArgument
		// We need to give CommandAPI the NBT API we're using during
		// CommandAPI.onLoad(CommandAPIConfig) to use this Argument
		new CommandAPICommand("nbt")
			.withArguments(new NBTCompoundArgument<>("nbt"))
			.executes(((sender, args) -> {
				NBTContainer nbt = (NBTContainer) args.get(0);

				sender.sendMessage(nbt.toString());
			}))
			.register();
	}

	public void registerAllCommandTrees() {
		// This is a different method of registering commands
		// Just for demonstration purposes I will use the same commands
		// that have been registered in registerAllCommands()

		// /break <location>
		new CommandTree("break")
			.then(new LocationArgument("block", LocationType.BLOCK_POSITION)
				.executesPlayer((player, args) -> {
					((Location) args.get(0)).getBlock().breakNaturally();
				}))
			.register();

		// /myeffect
		// This command will be changed a bit to demonstrate
		// a way of optional arguments because it is not possible to
		// add optional arguments using the CommandAPICommand method
		new CommandTree("myeffect")
			.then(new PotionEffectArgument("potion")
				.executesPlayer((player, args) -> {
					// Register /myeffect <potion effect>
					// This command just adds the potion effect to the player that
					// executes the command
					PotionEffectType potionEffectType = (PotionEffectType) args.get(0);
					player.addPotionEffect(new PotionEffect(potionEffectType, 300 * 20, 1));
				})
				.then(new EntitySelectorArgument.OnePlayer("target")
					.executes((sender, args) -> {
						// Register /myeffect <potion effect> <player>
						// This command works exactly the same as the example
						// shown in registerAllCommands()
						PotionEffectType potionEffectType = (PotionEffectType) args.get(0);
						Player target = (Player) args.get(1);
						target.addPotionEffect(new PotionEffect(potionEffectType, 300 * 20, 1));
					})))
			.register();
	}

}
