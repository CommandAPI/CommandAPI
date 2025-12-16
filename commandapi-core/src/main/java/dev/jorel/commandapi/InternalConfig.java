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

/**
 * Configuration wrapper class. The config.yml file used by the CommandAPI is
 * only ever read from, nothing is ever written to it. That's why there's only
 * getter methods.
 */
public class InternalConfig {

	// Output registering and unregistering of commands
	private final boolean verboseOutput;

	// Whether we should suppress all logs
	private final boolean silentLogs;

	// The message to display when an executor implementation is missing
	private final String messageMissingExecutorImplementation;

	// Create a command_registration.json file
	private final File dispatcherFile;

	// The default command namespace
	private final String namespace;

	// Whether we should turn on networking
	private final boolean enableNetworking;

	// Whether we should log networking exceptions as warnings
	private final boolean makeNetworkingExceptionsWarnings;

	/**
	 * Creates an {@link InternalConfig} from a {@link CommandAPIConfig}
	 * 
	 * @param config The configuration to use to set up this internal configuration
	 */
	public InternalConfig(CommandAPIConfig<?> config) {
		this.verboseOutput = config.verboseOutput;
		this.silentLogs = config.silentLogs;
		this.messageMissingExecutorImplementation = config.missingExecutorImplementationMessage;
		this.dispatcherFile = config.dispatcherFile;
		this.namespace = config.namespace;
		this.enableNetworking = config.enableNetworking;
		this.makeNetworkingExceptionsWarnings = config.makeNetworkingExceptionsWarnings;
	}

	/**
	 * @return Whether verbose output is enabled
	 */
	public boolean hasVerboseOutput() {
		return this.verboseOutput;
	}

	/**
	 * @return Whether silent logs is enabled
	 */
	public boolean hasSilentLogs() {
		return this.silentLogs;
	}

	/**
	 * @return The message to display if a command executor does not have an
	 *         implementation for a given type
	 */
	public String getMissingImplementationMessage() {
		return this.messageMissingExecutorImplementation;
	}

	/**
	 * @return The {@link File} which should be used to create a JSON representation
	 *         of Brigadier's command tree
	 */
	public File getDispatcherFile() {
		return this.dispatcherFile;
	}

	/**
	 * @return The default namespace used to register commands
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * @return Whether networking should be enabled
	 */
	public boolean isNetworkingEnabled() {
		return enableNetworking;
	}

	/**
	 * @return Whether networking exceptions are logged as warnings
	 */
	public boolean makeNetworkingExceptionsWarnings() {
		return this.makeNetworkingExceptionsWarnings;
	}
}