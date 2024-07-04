/*******************************************************************************
 * Copyright 2018, 2021 Jorel Ali (Skepter) - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package dev.jorel.commandapi.wrappers;

import dev.jorel.commandapi.CommandAPIBukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ProxiedCommandSender;

/**
 * A simple representation of Minecraft's CommandListenerWrapper, in the form of
 * Bukkit's ProxiedCommandSender
 */
public interface NativeProxyCommandSender extends ProxiedCommandSender {
	/**
	 * Constructs a NativeProxyCommandSender, which is basically Minecraft's CommandListenerWrapper
	 *
	 * @param caller   the command sender that actually sent the command
	 * @param callee   the command sender that will be executing the command
	 * @param location the proxied location that the command will be run at
	 * @param world    the proxied world that the command will be run in
	 */
	static NativeProxyCommandSender from(CommandSender caller, CommandSender callee, Location location, World world) {
		return CommandAPIBukkit.get().createNativeProxyCommandSender(caller, callee, location, world);
	}

	/**
	 * Returns the location that this native command sender represents
	 *
	 * @return the location that this native command sender represents
	 */
	Location getLocation();

	/**
	 * Returns the world that this native command sender represents
	 *
	 * @return the world that this native command sender represents
	 */
	World getWorld();
}
