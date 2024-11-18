package dev.jorel.commandapi.wrappers;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public class MockNativeProxyCommandSender implements NativeProxyCommandSender {

	private final CommandSender caller;
	private final CommandSender callee;
	private final Location location;
	private final World world;

	/**
	 * Constructs a NativeProxyCommandSender, which is basically Minecraft's CommandListenerWrapper
	 * @param caller the command sender that actually sent the command
	 * @param callee the command sender that will be executing the command
	 * @param location the proxied location that the command will be run at
	 * @param world the proxied world that the command will be run in
	 */
	public MockNativeProxyCommandSender(CommandSender caller, CommandSender callee, Location location, World world) {
		this.caller = caller;
		this.callee = callee == null ? caller : callee;
		this.location = location;
		this.world = world;
	}

	// NativeProxyCommandSenderMethods
	@Override
	public Location getLocation() {
		return this.location;
	}

	@Override
	public World getWorld() {
		return this.world;
	}

	// ProxiedCommandSender methods
	@Override
	public @NotNull CommandSender getCaller() {
		return this.caller;
	}

	@Override
	public @NotNull CommandSender getCallee() {
		return this.callee;
	}

	// CommandSender methods
	@Override
	public void sendMessage(@NotNull String s) {
		this.caller.sendMessage(s);
	}

	@Override
	public void sendMessage(@NotNull String... strings) {
		this.caller.sendMessage(strings);
	}

	@Override
	public void sendMessage(@Nullable UUID uuid, @NotNull String s) {
		this.caller.sendMessage(uuid, s);
	}

	@Override
	public void sendMessage(@Nullable UUID uuid, @NotNull String... strings) {
		this.caller.sendMessage(uuid, strings);
	}

	@Override
	public @NotNull Server getServer() {
		return this.callee.getServer();
	}

	@Override
	public @NotNull String getName() {
		return this.callee.getName();
	}

	@Override
	public @NotNull Spigot spigot() {
		return this.caller.spigot();
	}

	@Override
	public @NotNull Component name() {
		return this.callee.name();
	}

	@Override
	public boolean isPermissionSet(@NotNull String s) {
		return this.caller.isPermissionSet(s);
	}

	@Override
	public boolean isPermissionSet(@NotNull Permission permission) {
		return this.caller.isPermissionSet(permission);
	}

	@Override
	public boolean hasPermission(@NotNull String s) {
		return this.caller.hasPermission(s);
	}

	@Override
	public boolean hasPermission(@NotNull Permission permission) {
		return this.caller.hasPermission(permission);
	}

	@Override
	public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String s, boolean b) {
		return this.caller.addAttachment(plugin, s, b);
	}

	@Override
	public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin) {
		return this.caller.addAttachment(plugin);
	}

	@Override
	public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String s, boolean b, int i) {
		return this.caller.addAttachment(plugin, s, b, i);
	}

	@Override
	public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, int i) {
		return this.caller.addAttachment(plugin, i);
	}

	@Override
	public void removeAttachment(@NotNull PermissionAttachment permissionAttachment) {
		this.caller.removeAttachment(permissionAttachment);
	}

	@Override
	public void recalculatePermissions() {
		this.caller.recalculatePermissions();
	}

	@Override
	public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
		return this.caller.getEffectivePermissions();
	}

	@Override
	public boolean isOp() {
		return this.caller.isOp();
	}

	@Override
	public void setOp(boolean b) {
		this.caller.setOp(b);
	}
}