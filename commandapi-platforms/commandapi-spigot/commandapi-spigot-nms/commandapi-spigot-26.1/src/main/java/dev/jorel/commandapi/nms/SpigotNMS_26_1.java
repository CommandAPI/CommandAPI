package dev.jorel.commandapi.nms;

import com.mojang.brigadier.context.CommandContext;
import dev.jorel.commandapi.InternalSpigotConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ColorArgument;
import org.bukkit.ChatColor;

public class SpigotNMS_26_1 extends SpigotNMS_26_Common {

	private NMS_26_1 bukkitNMS;

	public SpigotNMS_26_1(InternalSpigotConfig config) {
		super(config);
	}

	@Override
	public ChatColor getChatColor(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return ChatColor.getByChar(ColorArgument.getColor(cmdCtx, key).getChar());
	}

	@Override
	public NMS_26_Common bukkitNMS() {
		if (bukkitNMS == null) {
			this.bukkitNMS = new NMS_26_1(() -> COMMAND_BUILD_CONTEXT);
		}
		return bukkitNMS;
	}
}
