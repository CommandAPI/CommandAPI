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
package dev.jorel.commandapi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A class to contain information about how to configure the CommandAPI during its loading step.
 * You shouldn't use this class directly. Instead, use an appropriate subclass that corresponds to
 * the platform you are developing for.
 */
public abstract class CommandAPIConfig<Impl
/// @cond DOX
extends CommandAPIConfig<Impl>
/// @endcond
> implements ChainableBuilder<Impl> {
	// The default configuration. This should mirror the commandapi-plugin config.yml file.
	boolean verboseOutput = false;
	boolean silentLogs = false;
	String missingExecutorImplementationMessage = "This command has no implementations for %s";

	File dispatcherFile = null;

	boolean usePluginNamespace = false;
	String namespace = null;

	boolean reportFailedPacketSends;

	/**
	 * Sets verbose output logging for the CommandAPI if true.
	 *
	 * @param value whether verbose output should be enabled
	 * @return this CommandAPIConfig
	 */
	public Impl verboseOutput(boolean value) {
		this.verboseOutput = value;
		return instance();
	}

	/**
	 * Silences all logs (including warnings, but not errors) for the CommandAPI if
	 * true.
	 *
	 * @param value whether logging suppression should be enabled
	 * @return this CommandAPIConfig
	 */
	public Impl silentLogs(boolean value) {
		this.silentLogs = value;
		return instance();
	}

	/**
	 * Sets the message to display to users when a command has no executor.
	 * Available formatting parameters are:
	 *
	 * <ul>
	 * <li>%s - the executor class (lowercase)</li>
	 * <li>%S - the executor class (normal case)</li>
	 * </ul>
	 *
	 * @param value the message to display when a command has no executor
	 * @return this CommandAPIConfig
	 */
	public Impl missingExecutorImplementationMessage(String value) {
		this.missingExecutorImplementationMessage = value;
		return instance();
	}

	/**
	 * Specifies the location for the CommandAPI to store the internal
	 * representation of Brigadier's command tree.
	 *
	 * @param file a file pointing to where to store Brigadier's JSON command
	 *             dispatcher, for example
	 *             {@code new File(getDataFolder(), "command_registration.json")}.
	 *             If this argument is {@code null}, this file will not be created.
	 * @return this CommandAPIConfig
	 */
	public Impl dispatcherFile(File file) {
		this.dispatcherFile = file;
		return instance();
	}

	/**
	 * Sets the default namespace to use when register commands
	 *
	 * @param namespace the namespace to use when register commands
	 * @return this CommandAPIConfig
	 */
	public Impl setNamespace(String namespace) {
		if (!usePluginNamespace) {
			this.namespace = namespace;
		}
		return instance();
	}

	/**
	 * Sets whether the CommandAPI should throw exceptions if it cannot send a packet.
	 *
	 * @param value Whether an exception should be thrown when a packet cannot be sent
	 * @return this CommandAPIConfig
	 */
	public Impl reportFailedPacketSends(boolean value) {
		this.reportFailedPacketSends = value;
		return instance();
	}
}
