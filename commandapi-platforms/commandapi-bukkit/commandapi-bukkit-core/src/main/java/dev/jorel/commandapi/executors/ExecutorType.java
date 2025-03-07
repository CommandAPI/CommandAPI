/*******************************************************************************
 * Copyright 2018, 2020 Jorel Ali (Skepter) - MIT License
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
package dev.jorel.commandapi.executors;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;

/**
 * An enum representing the type of an executor
 */
public enum ExecutorType {

	/**
	 * An executor where the CommandSender is any {@link CommandSender}
	 */
	ALL,

	/**
	 * An executor where the CommandSender is a {@link Player}
	 */
	PLAYER,

	/**
	 * An executor where the CommandSender is an {@link Entity}
	 */
	ENTITY,

	/**
	 * An executor where the CommandSender is a {@link ConsoleCommandSender}
	 */
	CONSOLE,

	/**
	 * An executor where the CommandSender is a {@link BlockCommandSender}
	 */
	BLOCK,

	/**
	 * An executor where the CommandSender is a {@link ProxiedCommandSender}
	 */
	PROXY,

	/**
	 * An executor where the CommandSender is (always) a {@link NativeProxyCommandSender}
	 */
	NATIVE,

	/**
	 * An executor where the CommandSender is a {@link RemoteConsoleCommandSender}
	 */
	REMOTE,

	/**
	 * An executor where the CommandSender is a {@code io.papermc.paper.commands.FeedbackForwardingSender}
	 */
	FEEDBACK_FORWARDING;
}
