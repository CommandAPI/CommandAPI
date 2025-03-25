package dev.jorel.commandapi.nms;

import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_21_R4.command.ProxiedNativeCommandSender;

public class NativeProxyCommandSender_1_21_R4 extends ProxiedNativeCommandSender implements NativeProxyCommandSender {
	private final World world;
	private final Location location;

	public NativeProxyCommandSender_1_21_R4(CommandSourceStack css, CommandSender caller, CommandSender callee) {
		super(css, caller, callee);

		Vec3 pos = css.getPosition();
		Vec2 rot = css.getRotation();
		this.world = CommandAPIBukkit.get().getWorldForCSS(css);
		this.location = new Location(this.world, pos.x(), pos.y(), pos.z(), rot.y, rot.x);
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public World getWorld() {
		return world;
	}
}
