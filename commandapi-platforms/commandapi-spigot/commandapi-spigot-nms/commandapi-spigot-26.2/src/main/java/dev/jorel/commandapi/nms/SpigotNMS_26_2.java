package dev.jorel.commandapi.nms;

import com.mojang.brigadier.context.CommandContext;
import dev.jorel.commandapi.InternalSpigotConfig;
import dev.jorel.commandapi.preprocessor.Differs;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.TeamColorArgument;
import org.bukkit.ChatColor;


public class SpigotNMS_26_2 extends SpigotNMS_26_Common {

	private NMS_26_2 bukkitNMS;

	public SpigotNMS_26_2(InternalSpigotConfig config) {
		super(config);
	}

	@Differs(from = "26.1", by = "ColorArgument -> TeamColorArgument")
	@Override
	public ChatColor getChatColor(CommandContext<CommandSourceStack> cmdCtx, String key) {
		// Relies on the fact that the enums have identical names for the colors
		return ChatColor.valueOf(TeamColorArgument.getTeamColor(cmdCtx, key).name());
	}

	@Override
	public NMS_26_Common bukkitNMS() {
		if (bukkitNMS == null) {
			this.bukkitNMS = new NMS_26_2(() -> COMMAND_BUILD_CONTEXT);
		}
		return bukkitNMS;
	}
}
